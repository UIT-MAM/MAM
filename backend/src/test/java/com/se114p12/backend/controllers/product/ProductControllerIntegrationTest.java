package com.se114p12.backend.controllers.product;

import com.se114p12.backend.constants.AppConstant;
import com.se114p12.backend.entities.authentication.Role;
import com.se114p12.backend.entities.product.Product;
import com.se114p12.backend.entities.product.ProductCategory;
import com.se114p12.backend.entities.user.User;
import com.se114p12.backend.enums.LoginProvider;
import com.se114p12.backend.enums.UserStatus;
import com.se114p12.backend.neo4j.repositories.ProductNeo4jRepository;
import com.se114p12.backend.neo4j.repositories.UserNeo4jRepository;
import com.se114p12.backend.repositories.authentication.RoleRepository;
import com.se114p12.backend.repositories.authentication.UserRepository;
import com.se114p12.backend.repositories.product.ProductCategoryRepository;
import com.se114p12.backend.repositories.product.ProductRepository;
import com.se114p12.backend.services.general.StorageService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private ProductNeo4jRepository productNeo4jRepository;

    @MockitoBean
    private UserNeo4jRepository userNeo4jRepository;

    @MockitoBean
    private StorageService storageService;

    private static final String BASE_URL = AppConstant.API_BASE_PATH + "/products";

    private ProductCategory testCategory;
    private ProductCategory anotherCategory;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        // Clear existing data
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

        // Get or create admin role (needed for security context)
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
        testCategory = new ProductCategory();
        testCategory.setName("Test Category");
        testCategory.setDescription("Test category description");
        testCategory = productCategoryRepository.save(testCategory);

        // Create another category
        anotherCategory = new ProductCategory();
        anotherCategory.setName("Another Category");
        anotherCategory.setDescription("Another category description");
        anotherCategory = productCategoryRepository.save(anotherCategory);

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
        productRepository.save(anotherProduct);

        // Configure JwtUtil mock
        when(jwtUtil.getCurrentUserId()).thenReturn(testUser.getId());

        // Configure StorageService mock
        when(storageService.store(any(), anyString())).thenReturn("http://example.com/uploaded-image.jpg");
    }

    // ==================== GET /products Tests ====================

    @Nested
    @DisplayName("GET /products - Get All Products")
    class GetAllProductsTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 200 with all products")
        void getAllProducts_ReturnsAllProducts() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[*].name", containsInAnyOrder("Test Product", "Another Product")));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return paginated results")
        void getAllProducts_WithPagination_ReturnsPaginatedResults() throws Exception {
            mockMvc.perform(get(BASE_URL)
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
        @DisplayName("Should return empty content when no products exist")
        void getAllProducts_NoProducts_ReturnsEmptyContent() throws Exception {
            productRepository.deleteAll();

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements", is(0)));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should not return deleted products")
        void getAllProducts_ExcludesDeletedProducts() throws Exception {
            // Mark one product as deleted
            testProduct.setDeleted(true);
            productRepository.save(testProduct);

            entityManager.flush();
            entityManager.clear();

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].name", is("Another Product")));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void getAllProducts_Unauthenticated_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== GET /products/{id} Tests ====================

    @Nested
    @DisplayName("GET /products/{id} - Get Product By ID")
    class GetProductByIdTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 200 with product when product exists")
        void getProductById_ProductExists_ReturnsProduct() throws Exception {
            mockMvc.perform(get(BASE_URL + "/" + testProduct.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(testProduct.getId().intValue())))
                    .andExpect(jsonPath("$.name", is("Test Product")))
                    .andExpect(jsonPath("$.shortDescription", is("Short description for test product")))
                    .andExpect(jsonPath("$.detailDescription", is("Detailed description for test product")))
                    .andExpect(jsonPath("$.originalPrice", is(100.00)))
                    .andExpect(jsonPath("$.isAvailable", is(true)))
                    .andExpect(jsonPath("$.categoryId", is(testCategory.getId().intValue())));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 404 when product does not exist")
        void getProductById_ProductNotExists_Returns404() throws Exception {
            mockMvc.perform(get(BASE_URL + "/99999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should handle negative product ID")
        void getProductById_NegativeId_Returns404() throws Exception {
            mockMvc.perform(get(BASE_URL + "/-1"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should handle zero product ID")
        void getProductById_ZeroId_Returns404() throws Exception {
            mockMvc.perform(get(BASE_URL + "/0"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void getProductById_Unauthenticated_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(BASE_URL + "/" + testProduct.getId()))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== GET /products/category/{id} Tests ====================

    @Nested
    @DisplayName("GET /products/category/{id} - Get Products By Category")
    class GetProductsByCategoryTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 200 with products in category")
        void getProductsByCategory_CategoryHasProducts_ReturnsProducts() throws Exception {
            mockMvc.perform(get(BASE_URL + "/category/" + testCategory.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[*].categoryId", everyItem(is(testCategory.getId().intValue()))));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return empty content when category has no products")
        void getProductsByCategory_CategoryEmpty_ReturnsEmptyContent() throws Exception {
            mockMvc.perform(get(BASE_URL + "/category/" + anotherCategory.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return paginated results")
        void getProductsByCategory_WithPagination_ReturnsPaginatedResults() throws Exception {
            mockMvc.perform(get(BASE_URL + "/category/" + testCategory.getId())
                            .param("page", "0")
                            .param("size", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.totalElements", is(2)))
                    .andExpect(jsonPath("$.totalPages", is(2)));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should not return deleted products in category")
        void getProductsByCategory_ExcludesDeletedProducts() throws Exception {
            testProduct.setDeleted(true);
            productRepository.save(testProduct);

            entityManager.flush();
            entityManager.clear();

            mockMvc.perform(get(BASE_URL + "/category/" + testCategory.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].name", is("Another Product")));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return empty for non-existent category")
        void getProductsByCategory_CategoryNotExists_ReturnsEmpty() throws Exception {
            mockMvc.perform(get(BASE_URL + "/category/99999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void getProductsByCategory_Unauthenticated_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(BASE_URL + "/category/" + testCategory.getId()))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== POST /products Tests ====================

    @Nested
    @DisplayName("POST /products - Create Product")
    class CreateProductTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 200 with created product when valid request")
        void createProduct_ValidRequest_ReturnsCreatedProduct() throws Exception {
            MockMultipartFile image = new MockMultipartFile(
                    "image",
                    "test-image.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "test image content".getBytes()
            );

            mockMvc.perform(multipart(BASE_URL)
                            .file(image)
                            .param("categoryId", testCategory.getId().toString())
                            .param("name", "New Product")
                            .param("shortDescription", "New short description")
                            .param("detailDescription", "New detailed description")
                            .param("originalPrice", "150.00")
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.name", is("New Product")))
                    .andExpect(jsonPath("$.shortDescription", is("New short description")))
                    .andExpect(jsonPath("$.detailDescription", is("New detailed description")))
                    .andExpect(jsonPath("$.originalPrice", is(150.00)))
                    .andExpect(jsonPath("$.categoryId", is(testCategory.getId().intValue())));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should create product without image")
        void createProduct_WithoutImage_ReturnsCreatedProduct() throws Exception {
            mockMvc.perform(multipart(BASE_URL)
                            .param("categoryId", testCategory.getId().toString())
                            .param("name", "Product Without Image")
                            .param("shortDescription", "Short description")
                            .param("detailDescription", "Detail description")
                            .param("originalPrice", "75.00")
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Product Without Image")));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 404 when category does not exist")
        void createProduct_CategoryNotExists_Returns404() throws Exception {
            mockMvc.perform(multipart(BASE_URL)
                            .param("categoryId", "99999")
                            .param("name", "New Product")
                            .param("shortDescription", "Short description")
                            .param("detailDescription", "Detail description")
                            .param("originalPrice", "100.00")
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void createProduct_Unauthenticated_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(multipart(BASE_URL)
                            .param("categoryId", testCategory.getId().toString())
                            .param("name", "New Product")
                            .param("shortDescription", "Short description")
                            .param("detailDescription", "Detail description")
                            .param("originalPrice", "100.00")
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== PUT /products/{id} Tests ====================

    @Nested
    @DisplayName("PUT /products/{id} - Update Product")
    class UpdateProductTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 200 with updated product when valid request")
        void updateProduct_ValidRequest_ReturnsUpdatedProduct() throws Exception {
            MockMultipartFile image = new MockMultipartFile(
                    "image",
                    "updated-image.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "updated image content".getBytes()
            );

            mockMvc.perform(multipart(BASE_URL + "/" + testProduct.getId())
                            .file(image)
                            .param("categoryId", anotherCategory.getId().toString())
                            .param("name", "Updated Product Name")
                            .param("shortDescription", "Updated short description")
                            .param("detailDescription", "Updated detailed description")
                            .param("originalPrice", "200.00")
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .with(request -> {
                                request.setMethod("PUT");
                                return request;
                            }))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(testProduct.getId().intValue())))
                    .andExpect(jsonPath("$.name", is("Updated Product Name")))
                    .andExpect(jsonPath("$.shortDescription", is("Updated short description")))
                    .andExpect(jsonPath("$.detailDescription", is("Updated detailed description")))
                    .andExpect(jsonPath("$.originalPrice", is(200.00)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should update product without changing image")
        void updateProduct_WithoutImage_ReturnsUpdatedProduct() throws Exception {
            mockMvc.perform(multipart(BASE_URL + "/" + testProduct.getId())
                            .param("categoryId", testCategory.getId().toString())
                            .param("name", "Updated Name Only")
                            .param("shortDescription", "")
                            .param("detailDescription", "")
                            .param("originalPrice", "100.00")
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .with(request -> {
                                request.setMethod("PUT");
                                return request;
                            }))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Updated Name Only")));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 404 when product does not exist")
        void updateProduct_ProductNotExists_Returns404() throws Exception {
            mockMvc.perform(multipart(BASE_URL + "/99999")
                            .param("categoryId", testCategory.getId().toString())
                            .param("name", "Updated Product")
                            .param("shortDescription", "Short description")
                            .param("detailDescription", "Detail description")
                            .param("originalPrice", "100.00")
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .with(request -> {
                                request.setMethod("PUT");
                                return request;
                            }))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void updateProduct_Unauthenticated_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(multipart(BASE_URL + "/" + testProduct.getId())
                            .param("categoryId", testCategory.getId().toString())
                            .param("name", "Updated Product")
                            .param("shortDescription", "Short description")
                            .param("detailDescription", "Detail description")
                            .param("originalPrice", "100.00")
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .with(request -> {
                                request.setMethod("PUT");
                                return request;
                            }))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== DELETE /products/{id} Tests ====================

    @Nested
    @DisplayName("DELETE /products/{id} - Delete Product")
    class DeleteProductTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 204 when product is deleted successfully")
        void deleteProduct_ProductExists_ReturnsNoContent() throws Exception {
            Long productId = testProduct.getId();

            mockMvc.perform(delete(BASE_URL + "/" + productId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 404 when product does not exist")
        void deleteProduct_ProductNotExists_Returns404() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/99999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void deleteProduct_Unauthenticated_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/" + testProduct.getId()))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== GET /products/recommended Tests ====================

    @Nested
    @DisplayName("GET /products/recommended - Get Recommended Products")
    class GetRecommendedProductsTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 200 with recommended products")
        void getRecommendedProducts_ReturnsProducts() throws Exception {
            mockMvc.perform(get(BASE_URL + "/recommended"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void getRecommendedProducts_Unauthenticated_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(BASE_URL + "/recommended"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== Response Structure Tests ====================

    @Nested
    @DisplayName("Response Structure Validation")
    class ResponseStructureTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Product response should have correct structure")
        void productResponse_HasCorrectStructure() throws Exception {
            mockMvc.perform(get(BASE_URL + "/" + testProduct.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.name").exists())
                    .andExpect(jsonPath("$.shortDescription").exists())
                    .andExpect(jsonPath("$.detailDescription").exists())
                    .andExpect(jsonPath("$.originalPrice").exists())
                    .andExpect(jsonPath("$.imageUrl").exists())
                    .andExpect(jsonPath("$.isAvailable").exists())
                    .andExpect(jsonPath("$.categoryId").exists())
                    .andExpect(jsonPath("$.categoryName").exists());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Page response should have correct structure")
        void pageResponse_HasCorrectStructure() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.page").exists())
                    .andExpect(jsonPath("$.size").exists())
                    .andExpect(jsonPath("$.totalElements").exists())
                    .andExpect(jsonPath("$.totalPages").exists())
                    .andExpect(jsonPath("$.numberOfElements").exists());
        }
    }

    // ==================== Edge Cases Tests ====================

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should handle products with different price ranges")
        void getAllProducts_DifferentPriceRanges_ReturnsAllProducts() throws Exception {
            // Create products with various prices
            for (int i = 1; i <= 3; i++) {
                Product product = new Product();
                product.setName("Product Price Test " + i);
                product.setShortDescription("Short desc " + i);
                product.setDetailDescription("Detail desc " + i);
                product.setOriginalPrice(new BigDecimal(i * 100 + ".99"));
                product.setIsAvailable(true);
                product.setDeleted(false);
                product.setCategory(testCategory);
                productRepository.save(product);
            }

            entityManager.flush();
            entityManager.clear();

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(5))); // 2 original + 3 new
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should handle products with unavailable status")
        void getAllProducts_IncludesUnavailableProducts() throws Exception {
            testProduct.setIsAvailable(false);
            productRepository.save(testProduct);

            entityManager.flush();
            entityManager.clear();

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Admin should be able to access all endpoints")
        void adminAccess_CanAccessAllEndpoints() throws Exception {
            // GET all products
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk());

            // GET product by ID
            mockMvc.perform(get(BASE_URL + "/" + testProduct.getId()))
                    .andExpect(status().isOk());

            // GET products by category
            mockMvc.perform(get(BASE_URL + "/category/" + testCategory.getId()))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should handle pagination edge cases")
        void getAllProducts_LargePageNumber_ReturnsEmptyContent() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .param("page", "100")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should handle products with null optional fields")
        void getProductById_ProductWithNullOptionalFields_ReturnsProduct() throws Exception {
            Product productWithNulls = new Product();
            productWithNulls.setName("Minimal Product");
            productWithNulls.setOriginalPrice(new BigDecimal("25.00"));
            productWithNulls.setIsAvailable(true);
            productWithNulls.setDeleted(false);
            productWithNulls.setCategory(testCategory);
            // shortDescription, detailDescription, imageUrl are null
            productWithNulls = productRepository.save(productWithNulls);

            entityManager.flush();
            entityManager.clear();

            mockMvc.perform(get(BASE_URL + "/" + productWithNulls.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Minimal Product")))
                    .andExpect(jsonPath("$.originalPrice", is(25.00)));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should handle products with special characters in name")
        void getAllProducts_SpecialCharactersInName_ReturnsProducts() throws Exception {
            Product specialProduct = new Product();
            specialProduct.setName("Product with 'quotes' & special <chars>");
            specialProduct.setShortDescription("Special chars test");
            specialProduct.setDetailDescription("Testing special characters handling");
            specialProduct.setOriginalPrice(new BigDecimal("30.00"));
            specialProduct.setIsAvailable(true);
            specialProduct.setDeleted(false);
            specialProduct.setCategory(testCategory);
            productRepository.save(specialProduct);

            entityManager.flush();
            entityManager.clear();

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].name", hasItem("Product with 'quotes' & special <chars>")));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should handle multiple categories correctly")
        void getProductsByCategory_MultipleCategories_ReturnsCorrectProducts() throws Exception {
            // Create product in another category
            Product productInAnotherCategory = new Product();
            productInAnotherCategory.setName("Product In Another Category");
            productInAnotherCategory.setShortDescription("Short desc");
            productInAnotherCategory.setDetailDescription("Detail desc");
            productInAnotherCategory.setOriginalPrice(new BigDecimal("60.00"));
            productInAnotherCategory.setIsAvailable(true);
            productInAnotherCategory.setDeleted(false);
            productInAnotherCategory.setCategory(anotherCategory);
            productRepository.save(productInAnotherCategory);

            entityManager.flush();
            entityManager.clear();

            // Check first category
            mockMvc.perform(get(BASE_URL + "/category/" + testCategory.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)));

            // Check second category
            mockMvc.perform(get(BASE_URL + "/category/" + anotherCategory.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].name", is("Product In Another Category")));
        }
    }

    // ==================== Sorting Tests ====================

    @Nested
    @DisplayName("Sorting Tests")
    class SortingTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should sort products by name ascending")
        void getAllProducts_SortByNameAsc_ReturnsSortedProducts() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .param("sort", "name,asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].name", is("Another Product")))
                    .andExpect(jsonPath("$.content[1].name", is("Test Product")));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should sort products by price descending")
        void getAllProducts_SortByPriceDesc_ReturnsSortedProducts() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .param("sort", "originalPrice,desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].originalPrice", is(100.00)))
                    .andExpect(jsonPath("$.content[1].originalPrice", is(50.00)));
        }
    }
}

