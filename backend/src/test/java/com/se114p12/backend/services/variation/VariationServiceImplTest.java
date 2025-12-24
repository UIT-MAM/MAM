package com.se114p12.backend.services.variation;

import com.se114p12.backend.dtos.variation.VariationRequestDTO;
import com.se114p12.backend.dtos.variation.VariationResponseDTO;
import com.se114p12.backend.entities.product.Product;
import com.se114p12.backend.entities.variation.Variation;
import com.se114p12.backend.exceptions.ResourceNotFoundException;
import com.se114p12.backend.mappers.variation.VariationMapper;
import com.se114p12.backend.repositories.product.ProductRepository;
import com.se114p12.backend.repositories.variation.VariationRepository;
import com.se114p12.backend.vo.PageVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VariationServiceImplTest {

    private VariationRepository variationRepository;
    private ProductRepository productRepository;
    private VariationMapper variationMapper;
    private VariationServiceImpl service;

    @BeforeEach
    void setUp() {
        variationRepository = mock(VariationRepository.class);
        productRepository = mock(ProductRepository.class);
        variationMapper = mock(VariationMapper.class);

        service = new VariationServiceImpl(variationRepository, productRepository, variationMapper);
    }

    // Helper methods to create test data
    private Product createProduct(Long id) {
        Product product = new Product();
        product.setId(id);
        product.setName("Test Product");
        product.setCreatedAt(Instant.now());
        product.setUpdatedAt(Instant.now());
        return product;
    }

    private Variation createVariation(Long id, String name, Boolean isMultipleChoice, Product product) {
        Variation variation = new Variation();
        variation.setId(id);
        variation.setName(name);
        variation.setIsMultipleChoice(isMultipleChoice);
        variation.setProduct(product);
        variation.setCreatedAt(Instant.now());
        variation.setUpdatedAt(Instant.now());
        return variation;
    }

    private VariationRequestDTO createRequestDTO(String name, Boolean isMultipleChoice, Long productId) {
        VariationRequestDTO dto = new VariationRequestDTO();
        dto.setName(name);
        dto.setIsMultipleChoice(isMultipleChoice);
        dto.setProductId(productId);
        return dto;
    }

    private VariationResponseDTO createResponseDTO(Long id, String name, Boolean isMultipleChoice, Long productId) {
        VariationResponseDTO dto = new VariationResponseDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setIsMultipleChoice(isMultipleChoice);
        dto.setProductId(productId);
        dto.setCreatedAt(Instant.now());
        dto.setUpdatedAt(Instant.now());
        return dto;
    }

    // ==================== getVariationsByProductId Tests ====================

    @Test
    @DisplayName("getVariationsByProductId - should return paginated variations without name filter")
    void getVariationsByProductId_withoutNameFilter_success() {
        // Given
        Long productId = 1L;
        String nameFilter = null;
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));

        Product product = createProduct(productId);
        Variation variation1 = createVariation(1L, "Size", true, product);
        Variation variation2 = createVariation(2L, "Color", false, product);

        List<Variation> variations = List.of(variation1, variation2);
        Page<Variation> page = new PageImpl<>(variations, pageable, 2);

        VariationResponseDTO dto1 = createResponseDTO(1L, "Size", true, productId);
        VariationResponseDTO dto2 = createResponseDTO(2L, "Color", false, productId);

        when(variationRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(variationMapper.toDTO(variation1)).thenReturn(dto1);
        when(variationMapper.toDTO(variation2)).thenReturn(dto2);

        // When
        PageVO<VariationResponseDTO> result = service.getVariationsByProductId(productId, nameFilter, pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(2, result.getNumberOfElements());
        assertEquals(2, result.getContent().size());
        assertEquals("Size", result.getContent().get(0).getName());
        assertEquals("Color", result.getContent().get(1).getName());

        verify(variationRepository, times(1)).findAll(any(Specification.class), eq(pageable));
        verify(variationMapper, times(1)).toDTO(variation1);
        verify(variationMapper, times(1)).toDTO(variation2);
    }

    @Test
    @DisplayName("getVariationsByProductId - should filter by name when nameFilter is provided")
    void getVariationsByProductId_withNameFilter_success() {
        // Given
        Long productId = 1L;
        String nameFilter = "size";
        Pageable pageable = PageRequest.of(0, 10);

        Product product = createProduct(productId);
        Variation variation1 = createVariation(1L, "Size", true, product);

        List<Variation> variations = List.of(variation1);
        Page<Variation> page = new PageImpl<>(variations, pageable, 1);

        VariationResponseDTO dto1 = createResponseDTO(1L, "Size", true, productId);

        when(variationRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(variationMapper.toDTO(variation1)).thenReturn(dto1);

        // When
        PageVO<VariationResponseDTO> result = service.getVariationsByProductId(productId, nameFilter, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("Size", result.getContent().get(0).getName());

        // Verify that the specification was called with correct parameters
        ArgumentCaptor<Specification<Variation>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(variationRepository).findAll(specCaptor.capture(), eq(pageable));

        verify(variationMapper, times(1)).toDTO(variation1);
    }

    @Test
    @DisplayName("getVariationsByProductId - should return empty page when no variations found")
    void getVariationsByProductId_noVariationsFound_returnsEmptyPage() {
        // Given
        Long productId = 1L;
        String nameFilter = null;
        Pageable pageable = PageRequest.of(0, 10);

        Page<Variation> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(variationRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

        // When
        PageVO<VariationResponseDTO> result = service.getVariationsByProductId(productId, nameFilter, pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getContent().size());

        verify(variationRepository, times(1)).findAll(any(Specification.class), eq(pageable));
        verify(variationMapper, never()).toDTO(any());
    }

    @Test
    @DisplayName("getVariationsByProductId - should handle blank name filter as no filter")
    void getVariationsByProductId_withBlankNameFilter_success() {
        // Given
        Long productId = 1L;
        String nameFilter = "   "; // Blank string
        Pageable pageable = PageRequest.of(0, 10);

        Product product = createProduct(productId);
        Variation variation1 = createVariation(1L, "Size", true, product);

        List<Variation> variations = List.of(variation1);
        Page<Variation> page = new PageImpl<>(variations, pageable, 1);

        VariationResponseDTO dto1 = createResponseDTO(1L, "Size", true, productId);

        when(variationRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(variationMapper.toDTO(variation1)).thenReturn(dto1);

        // When
        PageVO<VariationResponseDTO> result = service.getVariationsByProductId(productId, nameFilter, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(variationRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    // ==================== create Tests ====================

    @Test
    @DisplayName("create - should successfully create variation when product exists")
    void create_productExists_success() {
        // Given
        Long productId = 1L;
        VariationRequestDTO requestDTO = createRequestDTO("Size", true, productId);

        Product product = createProduct(productId);
        Variation variation = createVariation(null, "Size", true, product);
        Variation savedVariation = createVariation(1L, "Size", true, product);
        VariationResponseDTO responseDTO = createResponseDTO(1L, "Size", true, productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(variationMapper.toEntity(requestDTO)).thenReturn(variation);
        when(variationRepository.save(any(Variation.class))).thenReturn(savedVariation);
        when(variationMapper.toDTO(savedVariation)).thenReturn(responseDTO);

        // When
        VariationResponseDTO result = service.create(requestDTO);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Size", result.getName());
        assertEquals(true, result.getIsMultipleChoice());
        assertEquals(productId, result.getProductId());

        verify(productRepository, times(1)).findById(productId);
        verify(variationMapper, times(1)).toEntity(requestDTO);
        verify(variationRepository, times(1)).save(any(Variation.class));
        verify(variationMapper, times(1)).toDTO(savedVariation);
    }

    @Test
    @DisplayName("create - should throw ResourceNotFoundException when product not found")
    void create_productNotFound_throwsException() {
        // Given
        Long productId = 999L;
        VariationRequestDTO requestDTO = createRequestDTO("Size", true, productId);

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> service.create(requestDTO)
        );

        assertEquals("Product not found.", exception.getMessage());

        verify(productRepository, times(1)).findById(productId);
        verify(variationMapper, never()).toEntity(any());
        verify(variationRepository, never()).save(any());
    }

    @Test
    @DisplayName("create - should create variation with isMultipleChoice false")
    void create_isMultipleChoiceFalse_success() {
        // Given
        Long productId = 1L;
        VariationRequestDTO requestDTO = createRequestDTO("Color", false, productId);

        Product product = createProduct(productId);
        Variation variation = createVariation(null, "Color", false, product);
        Variation savedVariation = createVariation(2L, "Color", false, product);
        VariationResponseDTO responseDTO = createResponseDTO(2L, "Color", false, productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(variationMapper.toEntity(requestDTO)).thenReturn(variation);
        when(variationRepository.save(any(Variation.class))).thenReturn(savedVariation);
        when(variationMapper.toDTO(savedVariation)).thenReturn(responseDTO);

        // When
        VariationResponseDTO result = service.create(requestDTO);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("Color", result.getName());
        assertEquals(false, result.getIsMultipleChoice());

        verify(productRepository, times(1)).findById(productId);
        verify(variationRepository, times(1)).save(any(Variation.class));
    }

    // ==================== update Tests ====================

    @Test
    @DisplayName("update - should successfully update variation with new product")
    void update_withNewProduct_success() {
        // Given
        Long variationId = 1L;
        Long oldProductId = 1L;
        Long newProductId = 2L;

        VariationRequestDTO requestDTO = createRequestDTO("Updated Size", true, newProductId);

        Product oldProduct = createProduct(oldProductId);
        Product newProduct = createProduct(newProductId);
        Variation existingVariation = createVariation(variationId, "Size", false, oldProduct);
        Variation updatedVariation = createVariation(variationId, "Updated Size", true, newProduct);
        VariationResponseDTO responseDTO = createResponseDTO(variationId, "Updated Size", true, newProductId);

        when(variationRepository.findById(variationId)).thenReturn(Optional.of(existingVariation));
        when(productRepository.findById(newProductId)).thenReturn(Optional.of(newProduct));
        when(variationRepository.save(any(Variation.class))).thenReturn(updatedVariation);
        when(variationMapper.toDTO(updatedVariation)).thenReturn(responseDTO);

        // When
        VariationResponseDTO result = service.update(variationId, requestDTO);

        // Then
        assertNotNull(result);
        assertEquals(variationId, result.getId());
        assertEquals("Updated Size", result.getName());
        assertEquals(true, result.getIsMultipleChoice());
        assertEquals(newProductId, result.getProductId());

        verify(variationRepository, times(1)).findById(variationId);
        verify(productRepository, times(1)).findById(newProductId);
        verify(variationMapper, times(1)).updateEntityFromDTO(requestDTO, existingVariation);
        verify(variationRepository, times(1)).save(any(Variation.class));
        verify(variationMapper, times(1)).toDTO(updatedVariation);
    }

    @Test
    @DisplayName("update - should successfully update variation without changing product")
    void update_withoutNewProduct_success() {
        // Given
        Long variationId = 1L;
        Long productId = 1L;

        VariationRequestDTO requestDTO = createRequestDTO("Updated Size", true, null);

        Product product = createProduct(productId);
        Variation existingVariation = createVariation(variationId, "Size", false, product);
        Variation updatedVariation = createVariation(variationId, "Updated Size", true, product);
        VariationResponseDTO responseDTO = createResponseDTO(variationId, "Updated Size", true, productId);

        when(variationRepository.findById(variationId)).thenReturn(Optional.of(existingVariation));
        when(variationRepository.save(any(Variation.class))).thenReturn(updatedVariation);
        when(variationMapper.toDTO(updatedVariation)).thenReturn(responseDTO);

        // When
        VariationResponseDTO result = service.update(variationId, requestDTO);

        // Then
        assertNotNull(result);
        assertEquals(variationId, result.getId());
        assertEquals("Updated Size", result.getName());

        verify(variationRepository, times(1)).findById(variationId);
        verify(productRepository, never()).findById(any());
        verify(variationMapper, times(1)).updateEntityFromDTO(requestDTO, existingVariation);
        verify(variationRepository, times(1)).save(any(Variation.class));
        verify(variationMapper, times(1)).toDTO(updatedVariation);
    }

    @Test
    @DisplayName("update - should throw ResourceNotFoundException when variation not found")
    void update_variationNotFound_throwsException() {
        // Given
        Long variationId = 999L;
        VariationRequestDTO requestDTO = createRequestDTO("Size", true, 1L);

        when(variationRepository.findById(variationId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> service.update(variationId, requestDTO)
        );

        assertEquals("Variation not found.", exception.getMessage());

        verify(variationRepository, times(1)).findById(variationId);
        verify(productRepository, never()).findById(any());
        verify(variationRepository, never()).save(any());
    }

    @Test
    @DisplayName("update - should throw ResourceNotFoundException when new product not found")
    void update_newProductNotFound_throwsException() {
        // Given
        Long variationId = 1L;
        Long oldProductId = 1L;
        Long newProductId = 999L;

        VariationRequestDTO requestDTO = createRequestDTO("Size", true, newProductId);

        Product oldProduct = createProduct(oldProductId);
        Variation existingVariation = createVariation(variationId, "Size", false, oldProduct);

        when(variationRepository.findById(variationId)).thenReturn(Optional.of(existingVariation));
        when(productRepository.findById(newProductId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> service.update(variationId, requestDTO)
        );

        assertEquals("Product not found.", exception.getMessage());

        verify(variationRepository, times(1)).findById(variationId);
        verify(productRepository, times(1)).findById(newProductId);
        verify(variationRepository, never()).save(any());
    }

    @Test
    @DisplayName("update - should update only name keeping same product")
    void update_updateNameOnly_success() {
        // Given
        Long variationId = 1L;
        Long productId = 1L;

        VariationRequestDTO requestDTO = createRequestDTO("New Name", false, null);

        Product product = createProduct(productId);
        Variation existingVariation = createVariation(variationId, "Old Name", true, product);
        Variation updatedVariation = createVariation(variationId, "New Name", false, product);
        VariationResponseDTO responseDTO = createResponseDTO(variationId, "New Name", false, productId);

        when(variationRepository.findById(variationId)).thenReturn(Optional.of(existingVariation));
        when(variationRepository.save(any(Variation.class))).thenReturn(updatedVariation);
        when(variationMapper.toDTO(updatedVariation)).thenReturn(responseDTO);

        // When
        VariationResponseDTO result = service.update(variationId, requestDTO);

        // Then
        assertNotNull(result);
        assertEquals("New Name", result.getName());
        assertEquals(false, result.getIsMultipleChoice());

        verify(variationMapper, times(1)).updateEntityFromDTO(requestDTO, existingVariation);
    }

    // ==================== delete Tests ====================

    @Test
    @DisplayName("delete - should successfully delete variation when it exists")
    void delete_variationExists_success() {
        // Given
        Long variationId = 1L;

        when(variationRepository.existsById(variationId)).thenReturn(true);
        doNothing().when(variationRepository).deleteById(variationId);

        // When
        service.delete(variationId);

        // Then
        verify(variationRepository, times(1)).existsById(variationId);
        verify(variationRepository, times(1)).deleteById(variationId);
    }

    @Test
    @DisplayName("delete - should throw ResourceNotFoundException when variation not found")
    void delete_variationNotFound_throwsException() {
        // Given
        Long variationId = 999L;

        when(variationRepository.existsById(variationId)).thenReturn(false);

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> service.delete(variationId)
        );

        assertEquals("Variation not found.", exception.getMessage());

        verify(variationRepository, times(1)).existsById(variationId);
        verify(variationRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete - should delete variation with id 0")
    void delete_variationIdZero_success() {
        // Given
        Long variationId = 0L;

        when(variationRepository.existsById(variationId)).thenReturn(true);
        doNothing().when(variationRepository).deleteById(variationId);

        // When
        service.delete(variationId);

        // Then
        verify(variationRepository, times(1)).existsById(variationId);
        verify(variationRepository, times(1)).deleteById(variationId);
    }
}

