package com.se114p12.backend.controllers.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.se114p12.backend.constants.AppConstant;
import com.se114p12.backend.dtos.product.ProductRequestDTO;
import com.se114p12.backend.dtos.product.ProductResponseDTO;
import com.se114p12.backend.services.product.ProductService;
import com.se114p12.backend.vo.PageVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProductControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private ProductService productService;

    private String basePath() {
        return AppConstant.API_BASE_PATH + "/products";
    }

    @BeforeEach
    void setUp() {
        productService = mock(ProductService.class);
        ProductController controller = new ProductController(productService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(), new SpecificationArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("GET /products returns 200 with page result")
    void getAllProducts_returns200() throws Exception {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(1L);
        dto.setName("Phone");
        dto.setShortDescription("Short");
        dto.setDetailDescription("Detail");
        dto.setOriginalPrice(new BigDecimal("99.99"));
        dto.setImageUrl("/img.png");
        dto.setIsAvailable(true);
        dto.setCategoryId(5L);
        dto.setCategoryName("Electronics");

        PageVO<ProductResponseDTO> page = PageVO.<ProductResponseDTO>builder()
                .page(0)
                .size(10)
                .totalElements(1L)
                .totalPages(1)
                .numberOfElements(1)
                .content(List.of(dto))
                .build();

        when(productService.getAllProducts(any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get(basePath()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].name", is("Phone")));

        verify(productService).getAllProducts(isNull(), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /products/category/{id} returns 200 with page result for that category")
    void getProductsByCategory_returns200() throws Exception {
        PageVO<ProductResponseDTO> page = PageVO.<ProductResponseDTO>builder()
                .page(0).size(20).totalElements(0L).totalPages(0).numberOfElements(0)
                .content(List.of())
                .build();
        when(productService.getProductsByCategory(eq(9L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get(basePath() + "/category/{id}", 9))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));

        verify(productService).getProductsByCategory(eq(9L), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /products/{id} returns 200 with product body")
    void getProductById_returns200() throws Exception {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(12L);
        dto.setName("Laptop");
        when(productService.getProductById(12L)).thenReturn(dto);

        mockMvc.perform(get(basePath() + "/{id}", 12))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(12)))
                .andExpect(jsonPath("$.name", is("Laptop")));

        verify(productService).getProductById(12L);
    }

    @Test
    @DisplayName("POST /products (multipart) binds model and returns 200 with created product")
    void createProduct_multipart_returns200() throws Exception {
        ProductResponseDTO response = new ProductResponseDTO();
        response.setId(100L);
        response.setName("Created");
        when(productService.create(any(ProductRequestDTO.class))).thenReturn(response);

        MockMultipartFile file = new MockMultipartFile("image", "a.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1,2,3});

        mockMvc.perform(multipart(basePath())
                        .file(file)
                        .param("name", "Created")
                        .param("shortDescription", "s")
                        .param("detailDescription", "d")
                        .param("originalPrice", "123.45")
                        .param("categoryId", "7")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(100)))
                .andExpect(jsonPath("$.name", is("Created")));

        ArgumentCaptor<ProductRequestDTO> captor = ArgumentCaptor.forClass(ProductRequestDTO.class);
        verify(productService).create(captor.capture());
        ProductRequestDTO captured = captor.getValue();
        // Basic binding assertions
        assert captured.getName().equals("Created");
        assert captured.getCategoryId() == 7L;
        assert new BigDecimal("123.45").compareTo(captured.getOriginalPrice()) == 0;
        assert captured.getImage() != null;
    }

    @Test
    @DisplayName("PUT /products/{id} (multipart) updates and returns 200")
    void updateProduct_multipart_returns200() throws Exception {
        ProductResponseDTO response = new ProductResponseDTO();
        response.setId(200L);
        response.setName("Updated");
        when(productService.update(eq(55L), any(ProductRequestDTO.class))).thenReturn(response);

        MockMultipartFile file = new MockMultipartFile("image", "b.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{9,8});

        mockMvc.perform(MockMvcRequestBuilders.multipart(basePath() + "/{id}", 55)
                        .file(file)
                        .param("name", "Updated")
                        .param("shortDescription", "ss")
                        .param("detailDescription", "dd")
                        .param("originalPrice", "9.99")
                        .param("categoryId", "3")
                        .with(request -> { request.setMethod("PUT"); return request; })
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(200)))
                .andExpect(jsonPath("$.name", is("Updated")));

        verify(productService).update(eq(55L), any(ProductRequestDTO.class));
    }

    @Test
    @DisplayName("DELETE /products/{id} returns 204 and calls service")
    void deleteProduct_returns204() throws Exception {
        doNothing().when(productService).delete(88L);

        mockMvc.perform(delete(basePath() + "/{id}", 88))
                .andExpect(status().isNoContent());

        verify(productService).delete(88L);
    }

    @Test
    @DisplayName("GET /products/recommended returns 200 with list")
    void getRecommendedProducts_returns200() throws Exception {
        ProductResponseDTO a = new ProductResponseDTO(); a.setId(1L); a.setName("A");
        ProductResponseDTO b = new ProductResponseDTO(); b.setId(2L); b.setName("B");
        when(productService.getRecommendedProducts()).thenReturn(List.of(a, b));

        mockMvc.perform(get(basePath() + "/recommended"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("A")))
                .andExpect(jsonPath("$[1].name", is("B")));

        verify(productService).getRecommendedProducts();
    }
}

// Simple resolver to bypass @Filter Specification binding in standalone MockMvc
class SpecificationArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Specification.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        return null; // let controller receive null Specification
    }
}
