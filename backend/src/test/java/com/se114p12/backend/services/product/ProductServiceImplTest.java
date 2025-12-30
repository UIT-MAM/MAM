package com.se114p12.backend.services.product;

import com.se114p12.backend.constants.AppConstant;
import com.se114p12.backend.dtos.product.ProductRequestDTO;
import com.se114p12.backend.dtos.product.ProductResponseDTO;
import com.se114p12.backend.entities.cart.CartItem;
import com.se114p12.backend.entities.product.Product;
import com.se114p12.backend.entities.product.ProductCategory;
import com.se114p12.backend.exceptions.ResourceNotFoundException;
import com.se114p12.backend.mappers.product.ProductMapper;
import com.se114p12.backend.neo4j.entities.CategoryNode;
import com.se114p12.backend.neo4j.entities.ProductNode;
import com.se114p12.backend.neo4j.repositories.ProductNeo4jRepository;
import com.se114p12.backend.neo4j.services.RecommendService;
import com.se114p12.backend.repositories.product.ProductCategoryRepository;
import com.se114p12.backend.repositories.product.ProductRepository;
import com.se114p12.backend.services.general.StorageService;
import com.se114p12.backend.vo.PageVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProductServiceImplTest {

    private ProductRepository productRepository;
    private ProductCategoryRepository productCategoryRepository;
    private StorageService storageService;
    private ProductMapper productMapper;
    private RecommendService recommendService;
    private ProductNeo4jRepository productNeo4jRepository;

    private ProductServiceImpl service;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        productCategoryRepository = mock(ProductCategoryRepository.class);
        storageService = mock(StorageService.class);
        productMapper = mock(ProductMapper.class);
        recommendService = mock(RecommendService.class);
        productNeo4jRepository = mock(ProductNeo4jRepository.class);

        service = new ProductServiceImpl(
                productRepository,
                productCategoryRepository,
                storageService,
                productMapper,
                recommendService,
                productNeo4jRepository);
    }

    @Test
    @DisplayName("getAllProducts applies deleted=false filter, maps page and metadata")
    void getAllProducts_success() {
        // given
        Specification<Product> spec = Specification.where((root, query, cb) -> cb.conjunction());
        Pageable pageable = PageRequest.of(1, 3, Sort.by("name"));

        Product p1 = baseProduct(1L);
        Product p2 = baseProduct(2L);
        List<Product> products = List.of(p1, p2);
        Page<Product> page = new PageImpl<>(products, pageable, 7);

        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(productMapper.entityToResponse(p1)).thenReturn(dtoFrom(p1));
        when(productMapper.entityToResponse(p2)).thenReturn(dtoFrom(p2));

        // when
        PageVO<ProductResponseDTO> result = service.getAllProducts(spec, pageable);

        // then
        assertEquals(2, result.getContent().size());
        assertEquals(1, result.getPage());
        assertEquals(3, result.getSize());
        assertEquals(7, result.getTotalElements());
        assertEquals(page.getTotalPages(), result.getTotalPages());
        assertEquals(page.getNumberOfElements(), result.getNumberOfElements());
        verify(productRepository).findAll(any(Specification.class), eq(pageable));
        verify(productMapper, times(2)).entityToResponse(any(Product.class));
    }

    @Test
    @DisplayName("getProductsByCategory returns mapped PageVO")
    void getProductsByCategory_success() {
        Pageable pageable = PageRequest.of(0, 2);
        Product p1 = baseProduct(10L);
        Product p2 = baseProduct(11L);
        Page<Product> page = new PageImpl<>(List.of(p1, p2), pageable, 2);

        when(productRepository.findByCategory_IdAndDeletedFalse(9L, pageable)).thenReturn(page);
        when(productMapper.entityToResponse(p1)).thenReturn(dtoFrom(p1));
        when(productMapper.entityToResponse(p2)).thenReturn(dtoFrom(p2));

        PageVO<ProductResponseDTO> vo = service.getProductsByCategory(9L, pageable);
        assertEquals(2, vo.getContent().size());
        assertEquals(0, vo.getPage());
        assertEquals(2, vo.getSize());
        verify(productRepository).findByCategory_IdAndDeletedFalse(9L, pageable);
    }

    @Test
    @DisplayName("create saves product, uploads image when provided, saves to Neo4j and maps")
    void create_withImage_success() {
        ProductRequestDTO dto = new ProductRequestDTO();
        dto.setCategoryId(3L);
        dto.setName("New Product");
        dto.setShortDescription("Short");
        dto.setDetailDescription("Detail");
        dto.setOriginalPrice(new BigDecimal("12.34"));
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        dto.setImage(file);

        ProductCategory category = new ProductCategory();
        category.setId(3L);
        when(productCategoryRepository.findById(3L)).thenReturn(Optional.of(category));
        when(storageService.store(file, AppConstant.PRODUCT_FOLDER)).thenReturn("/images/p.png");

        Product saved = baseProduct(100L);
        saved.setCategory(category);
        saved.setName(dto.getName());
        saved.setShortDescription(dto.getShortDescription());
        saved.setDetailDescription(dto.getDetailDescription());
        saved.setOriginalPrice(dto.getOriginalPrice());
        saved.setIsAvailable(true);
        saved.setDeleted(false);
        saved.setImageUrl("/images/p.png");

        when(productRepository.save(any(Product.class))).thenReturn(saved);
        ProductResponseDTO responseDTO = dtoFrom(saved);
        when(productMapper.entityToResponse(saved)).thenReturn(responseDTO);

        ProductResponseDTO result = service.create(dto);

        assertSame(responseDTO, result);
        verify(productCategoryRepository).findById(3L);
        verify(storageService).store(file, AppConstant.PRODUCT_FOLDER);
        verify(productRepository).save(any(Product.class));
        ArgumentCaptor<ProductNode> nodeCaptor = ArgumentCaptor.forClass(ProductNode.class);
        verify(productNeo4jRepository).save(nodeCaptor.capture());
        ProductNode node = nodeCaptor.getValue();
        assertNotNull(node);
        assertEquals(100L, node.getId());
        CategoryNode categoryNode = node.getCategory();
        assertNotNull(categoryNode);
        assertEquals(3L, categoryNode.getId());
        verify(productMapper).entityToResponse(saved);
    }

    @Test
    @DisplayName("create without image doesn't call storage")
    void create_withoutImage_success() {
        ProductRequestDTO dto = new ProductRequestDTO();
        dto.setCategoryId(2L);
        dto.setName("NP");
        dto.setShortDescription("S");
        dto.setDetailDescription("D");
        dto.setOriginalPrice(new BigDecimal("20.00"));
        dto.setImage(null);

        ProductCategory category = new ProductCategory();
        category.setId(2L);
        when(productCategoryRepository.findById(2L)).thenReturn(Optional.of(category));
        Product saved = baseProduct(200L);
        saved.setCategory(category);
        when(productRepository.save(any(Product.class))).thenReturn(saved);
        when(productMapper.entityToResponse(saved)).thenReturn(dtoFrom(saved));

        ProductResponseDTO out = service.create(dto);
        assertNotNull(out);
        verify(storageService, never()).store(any(), anyString());
    }

    @Test
    @DisplayName("create throws when category not found")
    void create_categoryNotFound() {
        ProductRequestDTO dto = new ProductRequestDTO();
        dto.setCategoryId(99L);
        when(productCategoryRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.create(dto));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("update throws when product not found")
    void update_productNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.update(1L, new ProductRequestDTO()));
    }

    @Test
    @DisplayName("update changes fields, category (when !=0), uploads image when provided")
    void update_changeCategoryAndImage() {
        Product existing = baseProduct(10L);
        existing.setName("Old");
        existing.setShortDescription("OldS");
        existing.setDetailDescription("OldD");
        existing.setOriginalPrice(new BigDecimal("9.99"));
        ProductCategory oldCat = new ProductCategory();
        oldCat.setId(1L);
        existing.setCategory(oldCat);

        when(productRepository.findById(10L)).thenReturn(Optional.of(existing));

        ProductRequestDTO dto = new ProductRequestDTO();
        dto.setName("New");
        dto.setShortDescription("NewS");
        dto.setDetailDescription("NewD");
        dto.setOriginalPrice(new BigDecimal("19.99"));
        dto.setCategoryId(5L);
        MultipartFile newImage = mock(MultipartFile.class);
        when(newImage.isEmpty()).thenReturn(false);
        dto.setImage(newImage);

        ProductCategory newCat = new ProductCategory();
        newCat.setId(5L);
        when(productCategoryRepository.findById(5L)).thenReturn(Optional.of(newCat));
        when(storageService.store(newImage, AppConstant.PRODUCT_FOLDER)).thenReturn("/img/new.png");

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponseDTO mapped = new ProductResponseDTO();
        when(productMapper.entityToResponse(any(Product.class))).thenReturn(mapped);

        ProductResponseDTO out = service.update(10L, dto);
        assertSame(mapped, out);

        verify(productCategoryRepository).findById(5L);
        verify(storageService).store(newImage, AppConstant.PRODUCT_FOLDER);
        verify(productRepository).save(productCaptor.capture());
        Product saved = productCaptor.getValue();
        assertEquals("New", saved.getName());
        assertEquals("NewS", saved.getShortDescription());
        assertEquals("NewD", saved.getDetailDescription());
        assertEquals(new BigDecimal("19.99"), saved.getOriginalPrice());
        assertEquals(5L, saved.getCategory().getId());
        assertEquals("/img/new.png", saved.getImageUrl());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    @DisplayName("update keeps existing fields when dto has null/empty and doesn't change category when categoryId == 0; no image upload when empty")
    void update_keepExisting_noCategoryChange_noImage() {
        Product existing = baseProduct(20L);
        existing.setName("Keep");
        existing.setShortDescription("KeepS");
        existing.setDetailDescription("KeepD");
        existing.setOriginalPrice(new BigDecimal("5.55"));
        ProductCategory cat = new ProductCategory();
        cat.setId(7L);
        existing.setCategory(cat);

        when(productRepository.findById(20L)).thenReturn(Optional.of(existing));

        ProductRequestDTO dto = new ProductRequestDTO();
        dto.setName("");
        dto.setShortDescription(null);
        dto.setDetailDescription("");
        dto.setOriginalPrice(null);
        dto.setCategoryId(0L);
        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(true);
        dto.setImage(image);

        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productMapper.entityToResponse(any(Product.class))).thenReturn(new ProductResponseDTO());

        ProductResponseDTO out = service.update(20L, dto);
        assertNotNull(out);

        verify(productCategoryRepository, never()).findById(anyLong());
        verify(storageService, never()).store(any(), anyString());
        verify(productRepository).save(any(Product.class));

        assertEquals("Keep", existing.getName());
        assertEquals("KeepS", existing.getShortDescription());
        assertEquals("KeepD", existing.getDetailDescription());
        assertEquals(new BigDecimal("5.55"), existing.getOriginalPrice());
        assertEquals(7L, existing.getCategory().getId());
    }

    @Test
    @DisplayName("delete throws when not found")
    void delete_notFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.delete(1L));
    }

    @Test
    @DisplayName("delete throws when already deleted")
    void delete_alreadyDeleted() {
        Product p = baseProduct(2L);
        p.setDeleted(true);
        when(productRepository.findById(2L)).thenReturn(Optional.of(p));
        assertThrows(ResourceNotFoundException.class, () -> service.delete(2L));
    }

    @Test
    @DisplayName("delete marks product deleted and sets cart items unavailable, then saves")
    void delete_success() {
        Product p = baseProduct(3L);
        p.setDeleted(false);
        CartItem ci1 = new CartItem();
        ci1.setAvailable(true);
        CartItem ci2 = new CartItem();
        ci2.setAvailable(true);
        p.setCartItems(new ArrayList<>(List.of(ci1, ci2)));

        when(productRepository.findById(3L)).thenReturn(Optional.of(p));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        service.delete(3L);

        assertTrue(p.getDeleted());
        assertFalse(ci1.getAvailable());
        assertFalse(ci2.getAvailable());
        verify(productRepository).save(p);
    }

    @Test
    @DisplayName("getProductById maps when found")
    void getProductById_success() {
        Product p = baseProduct(9L);
        when(productRepository.findById(9L)).thenReturn(Optional.of(p));
        ProductResponseDTO dto = dtoFrom(p);
        when(productMapper.entityToResponse(p)).thenReturn(dto);
        ProductResponseDTO out = service.getProductById(9L);
        assertSame(dto, out);
    }

    @Test
    @DisplayName("getProductById throws when not found")
    void getProductById_notFound() {
        when(productRepository.findById(8L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getProductById(8L));
    }

    @Test
    @DisplayName("getRecommendedProducts maps in recommended-id order and doesn't fallback when at least 10 items")
    void getRecommendedProducts_enough() {
        // 10 ids to avoid fallback
        List<Long> ids = List.of(100L, 50L, 200L, 1L, 2L, 3L, 4L, 5L, 6L, 7L);
        when(recommendService.getRecommendProductIds()).thenReturn(ids);

        List<Product> products = ids.stream().map(this::baseProduct).collect(Collectors.toList());
        when(productRepository.findAllById(ids)).thenReturn(products);

        // mapper returns DTO with name to check order
        for (Product p : products) {
            when(productMapper.entityToResponse(p)).thenReturn(namedDto("P" + p.getId()));
        }

        List<ProductResponseDTO> out = service.getRecommendedProducts();
        List<String> names = out.stream().map(ProductResponseDTO::getName).collect(Collectors.toList());
        List<String> expected = ids.stream().map(id -> "P" + id).collect(Collectors.toList());
        assertEquals(expected, names);

        verify(productRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("getRecommendedProducts falls back to newest when less than 10 results and filters duplicates")
    void getRecommendedProducts_fallback() {
        List<Long> ids = List.of(10L); // only one id -> will fallback
        when(recommendService.getRecommendProductIds()).thenReturn(ids);

        Product p10 = baseProduct(10L);
        when(productRepository.findAllById(ids)).thenReturn(List.of(p10));

        // additional newest products 6..25 (20 products), include 10 to ensure filtering of duplicates
        Pageable expectedPageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Product> additional = IntStream.rangeClosed(6, 25)
                .mapToObj(this::baseProduct)
                .collect(Collectors.toList());
        Page<Product> additionalPage = new PageImpl<>(additional, expectedPageable, additional.size());
        when(productRepository.findAll(any(Pageable.class))).thenReturn(additionalPage);

        // mapper returns simple named DTOs
        when(productMapper.entityToResponse(any(Product.class))).thenAnswer(inv -> {
            Product prod = inv.getArgument(0);
            return namedDto("P" + prod.getId());
        });

        List<ProductResponseDTO> out = service.getRecommendedProducts();
        // Expect size 10: 1 original + 9 additional unique, excluding id 10 if present in additional
        assertEquals(10, out.size());
        // First should be the original id order (10)
        assertEquals("P10", out.get(0).getName());
        // Next 9 should be from additional excluding 10
        Set<String> rest = out.subList(1, out.size()).stream().map(ProductResponseDTO::getName).collect(Collectors.toSet());
        assertFalse(rest.contains("P10"));
        assertEquals(9, rest.size());

        verify(productRepository).findAll(any(Pageable.class));
    }

    // Helpers
    private Product baseProduct(long id) {
        Product p = new Product();
        p.setId(id);
        p.setName("Product" + id);
        p.setShortDescription("S" + id);
        p.setDetailDescription("D" + id);
        p.setOriginalPrice(new BigDecimal("10.00"));
        p.setIsAvailable(true);
        p.setDeleted(false);
        p.setCreatedAt(Instant.now());
        return p;
    }

    private ProductResponseDTO dtoFrom(Product p) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setShortDescription(p.getShortDescription());
        dto.setDetailDescription(p.getDetailDescription());
        dto.setOriginalPrice(p.getOriginalPrice());
        dto.setIsAvailable(p.getIsAvailable());
        return dto;
    }

    private ProductResponseDTO namedDto(String name) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setName(name);
        return dto;
    }
}
