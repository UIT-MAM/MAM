package com.se114p12.backend.controllers.variation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.se114p12.backend.constants.AppConstant;
import com.se114p12.backend.dtos.variation.VariationRequestDTO;
import com.se114p12.backend.entities.authentication.Role;
import com.se114p12.backend.entities.product.Product;
import com.se114p12.backend.entities.product.ProductCategory;
import com.se114p12.backend.entities.user.User;
import com.se114p12.backend.entities.variation.Variation;
import com.se114p12.backend.enums.LoginProvider;
import com.se114p12.backend.enums.UserStatus;
import com.se114p12.backend.neo4j.repositories.ProductNeo4jRepository;
import com.se114p12.backend.neo4j.repositories.UserNeo4jRepository;
import com.se114p12.backend.repositories.authentication.RoleRepository;
import com.se114p12.backend.repositories.authentication.UserRepository;
import com.se114p12.backend.repositories.product.ProductCategoryRepository;
import com.se114p12.backend.repositories.product.ProductRepository;
import com.se114p12.backend.repositories.variation.VariationRepository;
import com.se114p12.backend.util.JwtUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class VariationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private VariationRepository variationRepository;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private ProductNeo4jRepository productNeo4jRepository;

    @MockitoBean
    private UserNeo4jRepository userNeo4jRepository;

    private static final String BASE_URL = AppConstant.API_BASE_PATH + "/variations";

    private Product testProduct;
    private Variation testVariation;

    @BeforeEach
    void setUp() {
        // Clear existing data
        variationRepository.deleteAll();
        productRepository.deleteAll();
        productCategoryRepository.deleteAll();
        userRepository.deleteAll();

        // Get or create user role
        Role userRole = roleRepository.findByName("USER").orElseGet(() -> {
            Role role = new Role();
            role.setName("USER");
            role.setDescription("Regular user role");
            role.setActive(true);
            return roleRepository.save(role);
        });

        // Get or create admin role
        roleRepository.findByName("ADMIN").orElseGet(() -> {
            Role role = new Role();
            role.setName("ADMIN");
            role.setDescription("Admin user role");
            role.setActive(true);
            return roleRepository.save(role);
        });

        // Create test user
        User testUser = new User();
        testUser.setFullname("Test User");
        testUser.setUsername("testuser");
        testUser.setEmail("testuser@example.com");
        testUser.setPhone("+84987654321");
        testUser.setPassword("password123");
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setLoginProvider(LoginProvider.LOCAL);
        testUser.setRole(userRole);
        testUser = userRepository.save(testUser);

        // Create test category
        ProductCategory testCategory = new ProductCategory();
        testCategory.setName("Test Category");
        testCategory.setDescription("Test category description");
        testCategory = productCategoryRepository.save(testCategory);

        // Create test product
        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setShortDescription("Short description for test product");
        testProduct.setDetailDescription("Detailed description for test product");
        testProduct.setOriginalPrice(new BigDecimal("100.00"));
        testProduct.setImageUrl("http://example.com/image1.jpg");
        testProduct.setIsAvailable(true);
        testProduct.setDeleted(false);
        testProduct.setCategory(testCategory);
        testProduct = productRepository.save(testProduct);

        // Create another product
        Product anotherProduct = new Product();
        anotherProduct.setName("Another Product");
        anotherProduct.setShortDescription("Short description for another product");
        anotherProduct.setDetailDescription("Detailed description for another product");
        anotherProduct.setOriginalPrice(new BigDecimal("50.00"));
        anotherProduct.setImageUrl("http://example.com/image2.jpg");
        anotherProduct.setIsAvailable(true);
        anotherProduct.setDeleted(false);
        anotherProduct.setCategory(testCategory);
        anotherProduct = productRepository.save(anotherProduct);

        // Create test variation
        testVariation = new Variation();
        testVariation.setName("Color");
        testVariation.setIsMultipleChoice(false);
        testVariation.setProduct(testProduct);
        testVariation = variationRepository.save(testVariation);

        // Create another variation
        Variation anotherVariation = new Variation();
        anotherVariation.setName("Size");
        anotherVariation.setIsMultipleChoice(true);
        anotherVariation.setProduct(testProduct);
        variationRepository.save(anotherVariation);

        // Create variation for another product
        Variation variationForAnotherProduct = new Variation();
        variationForAnotherProduct.setName("Material");
        variationForAnotherProduct.setIsMultipleChoice(false);
        variationForAnotherProduct.setProduct(anotherProduct);
        variationRepository.save(variationForAnotherProduct);

        // Configure JwtUtil mock
        when(jwtUtil.getCurrentUserId()).thenReturn(testUser.getId());
    }

    // ==================== GET /variations Tests ====================

    @Nested
    @DisplayName("GET /variations - Get Variations by Product")
    class GetVariationsByProductTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 200 with variations for a specific product")
        void getVariationsByProduct_ReturnsVariationsForProduct() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .param("productId", testProduct.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[*].name", containsInAnyOrder("Color", "Size")))
                    .andExpect(jsonPath("$.content[*].productId", everyItem(is(testProduct.getId().intValue()))));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 200 with variations filtered by name")
        void getVariationsByProduct_FilteredByName_ReturnsMatchingVariations() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .param("productId", testProduct.getId().toString())
                            .param("name", "Color"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].name", is("Color")));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return paginated results")
        void getVariationsByProduct_WithPagination_ReturnsPaginatedResults() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .param("productId", testProduct.getId().toString())
                            .param("page", "0")
                            .param("size", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.totalElements", is(2)))
                    .andExpect(jsonPath("$.totalPages", is(2)))
                    .andExpect(jsonPath("$.page", is(0)))
                    .andExpect(jsonPath("$.size", is(1)));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return empty content when product has no variations")
        void getVariationsByProduct_NoVariations_ReturnsEmptyContent() throws Exception {
            // Create a product without variations
            ProductCategory category = productCategoryRepository.findAll().getFirst();
            Product productWithoutVariations = new Product();
            productWithoutVariations.setName("Product Without Variations");
            productWithoutVariations.setShortDescription("Short description");
            productWithoutVariations.setDetailDescription("Detail description");
            productWithoutVariations.setOriginalPrice(new BigDecimal("25.00"));
            productWithoutVariations.setIsAvailable(true);
            productWithoutVariations.setDeleted(false);
            productWithoutVariations.setCategory(category);
            productWithoutVariations = productRepository.save(productWithoutVariations);

            mockMvc.perform(get(BASE_URL)
                            .param("productId", productWithoutVariations.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements", is(0)));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return empty for non-existent product")
        void getVariationsByProduct_ProductNotExists_ReturnsEmpty() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .param("productId", "99999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void getVariationsByProduct_Unauthenticated_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .param("productId", testProduct.getId().toString()))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== POST /variations Tests ====================

    @Nested
    @DisplayName("POST /variations - Create Variation")
    class CreateVariationTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 200 with created variation when valid request")
        void createVariation_ValidRequest_ReturnsCreatedVariation() throws Exception {
            VariationRequestDTO request = new VariationRequestDTO();
            request.setName("Weight");
            request.setIsMultipleChoice(false);
            request.setProductId(testProduct.getId());

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.name", is("Weight")))
                    .andExpect(jsonPath("$.isMultipleChoice", is(false)))
                    .andExpect(jsonPath("$.productId", is(testProduct.getId().intValue())));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 200 with multiple choice variation")
        void createVariation_MultipleChoice_ReturnsCreatedVariation() throws Exception {
            VariationRequestDTO request = new VariationRequestDTO();
            request.setName("Toppings");
            request.setIsMultipleChoice(true);
            request.setProductId(testProduct.getId());

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.name", is("Toppings")))
                    .andExpect(jsonPath("$.isMultipleChoice", is(true)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 422 when name is blank")
        void createVariation_BlankName_ReturnsUnprocessableEntity() throws Exception {
            VariationRequestDTO request = new VariationRequestDTO();
            request.setName("");
            request.setIsMultipleChoice(false);
            request.setProductId(testProduct.getId());

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 422 when name is null")
        void createVariation_NullName_ReturnsUnprocessableEntity() throws Exception {
            VariationRequestDTO request = new VariationRequestDTO();
            request.setName(null);
            request.setIsMultipleChoice(false);
            request.setProductId(testProduct.getId());

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 422 when isMultipleChoice is null")
        void createVariation_NullIsMultipleChoice_ReturnsUnprocessableEntity() throws Exception {
            VariationRequestDTO request = new VariationRequestDTO();
            request.setName("Valid Name");
            request.setIsMultipleChoice(null);
            request.setProductId(testProduct.getId());

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 422 when productId is null")
        void createVariation_NullProductId_ReturnsUnprocessableEntity() throws Exception {
            VariationRequestDTO request = new VariationRequestDTO();
            request.setName("Valid Name");
            request.setIsMultipleChoice(false);
            request.setProductId(null);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should not allow non-admin user to create variation")
        void createVariation_NotAdmin_ReturnsError() throws Exception {
            VariationRequestDTO request = new VariationRequestDTO();
            request.setName("Weight");
            request.setIsMultipleChoice(false);
            request.setProductId(testProduct.getId());

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void createVariation_Unauthenticated_ReturnsUnauthorized() throws Exception {
            VariationRequestDTO request = new VariationRequestDTO();
            request.setName("Weight");
            request.setIsMultipleChoice(false);
            request.setProductId(testProduct.getId());

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== PUT /variations/{id} Tests ====================

    @Nested
    @DisplayName("PUT /variations/{id} - Update Variation")
    class UpdateVariationTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 200 with updated variation when valid request")
        void updateVariation_ValidRequest_ReturnsUpdatedVariation() throws Exception {
            VariationRequestDTO request = new VariationRequestDTO();
            request.setName("Updated Color");
            request.setIsMultipleChoice(true);
            request.setProductId(testProduct.getId());

            mockMvc.perform(put(BASE_URL + "/" + testVariation.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(testVariation.getId().intValue())))
                    .andExpect(jsonPath("$.name", is("Updated Color")))
                    .andExpect(jsonPath("$.isMultipleChoice", is(true)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 404 when variation does not exist")
        void updateVariation_VariationNotExists_Returns404() throws Exception {
            VariationRequestDTO request = new VariationRequestDTO();
            request.setName("Updated Name");
            request.setIsMultipleChoice(false);
            request.setProductId(testProduct.getId());

            mockMvc.perform(put(BASE_URL + "/99999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 422 when name is blank")
        void updateVariation_BlankName_ReturnsUnprocessableEntity() throws Exception {
            VariationRequestDTO request = new VariationRequestDTO();
            request.setName("");
            request.setIsMultipleChoice(false);
            request.setProductId(testProduct.getId());

            mockMvc.perform(put(BASE_URL + "/" + testVariation.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 422 when isMultipleChoice is null")
        void updateVariation_NullIsMultipleChoice_ReturnsUnprocessableEntity() throws Exception {
            VariationRequestDTO request = new VariationRequestDTO();
            request.setName("Valid Name");
            request.setIsMultipleChoice(null);
            request.setProductId(testProduct.getId());

            mockMvc.perform(put(BASE_URL + "/" + testVariation.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should not allow non-admin user to update variation")
        void updateVariation_NotAdmin_ReturnsError() throws Exception {
            VariationRequestDTO request = new VariationRequestDTO();
            request.setName("Updated Name");
            request.setIsMultipleChoice(false);
            request.setProductId(testProduct.getId());

            mockMvc.perform(put(BASE_URL + "/" + testVariation.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void updateVariation_Unauthenticated_ReturnsUnauthorized() throws Exception {
            VariationRequestDTO request = new VariationRequestDTO();
            request.setName("Updated Name");
            request.setIsMultipleChoice(false);
            request.setProductId(testProduct.getId());

            mockMvc.perform(put(BASE_URL + "/" + testVariation.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== DELETE /variations/{id} Tests ====================

    @Nested
    @DisplayName("DELETE /variations/{id} - Delete Variation")
    class DeleteVariationTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 204 when variation is deleted successfully")
        void deleteVariation_Success_ReturnsNoContent() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/" + testVariation.getId()))
                    .andExpect(status().isNoContent());

            // Verify the variation is deleted
            entityManager.flush();
            entityManager.clear();

            mockMvc.perform(get(BASE_URL)
                            .param("productId", testProduct.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].name", is("Size")));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 404 when variation does not exist")
        void deleteVariation_VariationNotExists_Returns404() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/99999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should not allow non-admin user to delete variation")
        void deleteVariation_NotAdmin_ReturnsError() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/" + testVariation.getId()))
                    .andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void deleteVariation_Unauthenticated_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/" + testVariation.getId()))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== Edge Cases Tests ====================

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle variation with very long name")
        void createVariation_LongName_Success() throws Exception {
            String longName = "A".repeat(255);
            VariationRequestDTO request = new VariationRequestDTO();
            request.setName(longName);
            request.setIsMultipleChoice(false);
            request.setProductId(testProduct.getId());

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is(longName)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle variation with special characters in name")
        void createVariation_SpecialCharactersInName_Success() throws Exception {
            String specialName = "Color & Size (Small/Medium/Large)";
            VariationRequestDTO request = new VariationRequestDTO();
            request.setName(specialName);
            request.setIsMultipleChoice(true);
            request.setProductId(testProduct.getId());

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is(specialName)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle variation with unicode characters in name")
        void createVariation_UnicodeCharactersInName_Success() throws Exception {
            String unicodeName = "Màu sắc 颜色 🎨";
            VariationRequestDTO request = new VariationRequestDTO();
            request.setName(unicodeName);
            request.setIsMultipleChoice(false);
            request.setProductId(testProduct.getId());

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is(unicodeName)));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should handle multiple variations for same product")
        void getVariationsByProduct_MultipleVariations_ReturnsAll() throws Exception {
            // Create additional variations
            for (int i = 0; i < 5; i++) {
                Variation variation = new Variation();
                variation.setName("Variation " + i);
                variation.setIsMultipleChoice(i % 2 == 0);
                variation.setProduct(testProduct);
                variationRepository.save(variation);
            }

            mockMvc.perform(get(BASE_URL)
                            .param("productId", testProduct.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(7))) // 2 existing + 5 new
                    .andExpect(jsonPath("$.totalElements", is(7)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle negative variation ID")
        void updateVariation_NegativeId_Returns404() throws Exception {
            VariationRequestDTO request = new VariationRequestDTO();
            request.setName("Updated Name");
            request.setIsMultipleChoice(false);
            request.setProductId(testProduct.getId());

            mockMvc.perform(put(BASE_URL + "/-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle zero variation ID")
        void updateVariation_ZeroId_Returns404() throws Exception {
            VariationRequestDTO request = new VariationRequestDTO();
            request.setName("Updated Name");
            request.setIsMultipleChoice(false);
            request.setProductId(testProduct.getId());

            mockMvc.perform(put(BASE_URL + "/0")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle delete with negative ID")
        void deleteVariation_NegativeId_Returns404() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/-1"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle create with non-existent product")
        void createVariation_NonExistentProduct_Returns404() throws Exception {
            VariationRequestDTO request = new VariationRequestDTO();
            request.setName("New Variation");
            request.setIsMultipleChoice(false);
            request.setProductId(99999L);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }
}

