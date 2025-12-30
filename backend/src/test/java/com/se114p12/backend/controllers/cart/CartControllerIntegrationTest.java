package com.se114p12.backend.controllers.cart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.se114p12.backend.constants.AppConstant;
import com.se114p12.backend.entities.authentication.Role;
import com.se114p12.backend.entities.cart.Cart;
import com.se114p12.backend.entities.cart.CartItem;
import com.se114p12.backend.entities.product.Product;
import com.se114p12.backend.entities.product.ProductCategory;
import com.se114p12.backend.entities.user.User;
import com.se114p12.backend.enums.LoginProvider;
import com.se114p12.backend.enums.UserStatus;
import com.se114p12.backend.repositories.authentication.RoleRepository;
import com.se114p12.backend.repositories.authentication.UserRepository;
import com.se114p12.backend.repositories.cart.CartItemRepository;
import com.se114p12.backend.repositories.cart.CartRepository;
import com.se114p12.backend.repositories.product.ProductCategoryRepository;
import com.se114p12.backend.repositories.product.ProductRepository;
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
import java.util.ArrayList;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CartControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private JwtUtil jwtUtil;

    private static final String BASE_URL = AppConstant.API_BASE_PATH + "/carts";

    private Role userRole;
    private User testUser;
    private User anotherUser;
    private Cart testCart;
    private ProductCategory testCategory;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        // Clear existing data
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        userRepository.deleteAll();

        // Get or create user role
        userRole = roleRepository.findByName("USER").orElseGet(() -> {
            Role role = new Role();
            role.setName("USER");
            role.setDescription("Regular user role");
            role.setActive(true);
            return roleRepository.save(role);
        });

        // Create test user
        testUser = new User();
        testUser.setFullname("Test User");
        testUser.setUsername("testuser");
        testUser.setEmail("testuser@example.com");
        testUser.setPhone("+84987654321");
        testUser.setPassword("password123");
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setLoginProvider(LoginProvider.LOCAL);
        testUser.setRole(userRole);
        testUser = userRepository.save(testUser);

        // Create another user for isolation tests
        anotherUser = new User();
        anotherUser.setFullname("Another User");
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("anotheruser@example.com");
        anotherUser.setPhone("+84987654322");
        anotherUser.setPassword("password123");
        anotherUser.setStatus(UserStatus.ACTIVE);
        anotherUser.setLoginProvider(LoginProvider.LOCAL);
        anotherUser.setRole(userRole);
        anotherUser = userRepository.save(anotherUser);

        // Create test category
        testCategory = productCategoryRepository.findAll().stream().findFirst().orElseGet(() -> {
            ProductCategory category = new ProductCategory();
            category.setName("Test Category");
            category.setDescription("Test category description");
            return productCategoryRepository.save(category);
        });

        // Create test product
        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setShortDescription("Short desc");
        testProduct.setDetailDescription("Detailed description");
        testProduct.setOriginalPrice(new BigDecimal("100.00"));
        testProduct.setIsAvailable(true);
        testProduct.setDeleted(false);
        testProduct.setCategory(testCategory);
        testProduct = productRepository.save(testProduct);

        // Create cart for test user
        testCart = new Cart();
        testCart.setUser(testUser);
        testCart.setCartItems(new ArrayList<>());
        testCart = cartRepository.save(testCart);

        // Update user's cart reference
        testUser.setCart(testCart);
        testUser = userRepository.save(testUser);

        // Configure JwtUtil mock to return the test user's ID by default
        when(jwtUtil.getCurrentUserId()).thenReturn(testUser.getId());
    }

    // ==================== GET /carts/me Tests ====================

    @Nested
    @DisplayName("GET /carts/me - Get Current User's Cart")
    class GetMyCartTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 200 with cart when user has a cart")
        void getMyCart_UserHasCart_ReturnsCart() throws Exception {
            mockMvc.perform(get(BASE_URL + "/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(testCart.getId().intValue())))
                    .andExpect(jsonPath("$.userId", is(testUser.getId().intValue())))
                    .andExpect(jsonPath("$.cartItems", hasSize(0)));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return cart with items when cart has items")
        void getMyCart_CartHasItems_ReturnsCartWithItems() throws Exception {
            // Add cart item
            CartItem cartItem = new CartItem();
            cartItem.setCart(testCart);
            cartItem.setProduct(testProduct);
            cartItem.setQuantity(2L);
            cartItem.setPrice(new BigDecimal("200.00"));
            cartItem.setAvailable(true);
            cartItemRepository.save(cartItem);

            entityManager.flush();
            entityManager.clear();

            mockMvc.perform(get(BASE_URL + "/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(testCart.getId().intValue())))
                    .andExpect(jsonPath("$.userId", is(testUser.getId().intValue())))
                    .andExpect(jsonPath("$.cartItems", hasSize(1)))
                    .andExpect(jsonPath("$.cartItems[0].productId", is(testProduct.getId().intValue())))
                    .andExpect(jsonPath("$.cartItems[0].quantity", is(2)))
                    .andExpect(jsonPath("$.cartItems[0].available", is(true)));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 404 when user has no cart")
        void getMyCart_UserHasNoCart_Returns404() throws Exception {
            // Delete the test cart
            testUser.setCart(null);
            userRepository.save(testUser);
            cartRepository.delete(testCart);

            when(jwtUtil.getCurrentUserId()).thenReturn(testUser.getId());

            mockMvc.perform(get(BASE_URL + "/me"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void getMyCart_Unauthenticated_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(BASE_URL + "/me"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== GET /carts/{id} Tests ====================

    @Nested
    @DisplayName("GET /carts/{id} - Get Cart By ID")
    class GetCartByIdTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 200 with cart when cart exists")
        void getCartById_CartExists_ReturnsCart() throws Exception {
            mockMvc.perform(get(BASE_URL + "/" + testCart.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(testCart.getId().intValue())))
                    .andExpect(jsonPath("$.userId", is(testUser.getId().intValue())));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 404 when cart does not exist")
        void getCartById_CartNotExists_Returns404() throws Exception {
            mockMvc.perform(get(BASE_URL + "/99999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return cart with items")
        void getCartById_CartWithItems_ReturnsCartWithItems() throws Exception {
            // Add cart item
            CartItem cartItem = new CartItem();
            cartItem.setCart(testCart);
            cartItem.setProduct(testProduct);
            cartItem.setQuantity(3L);
            cartItem.setPrice(new BigDecimal("300.00"));
            cartItem.setAvailable(true);
            cartItemRepository.save(cartItem);

            entityManager.flush();
            entityManager.clear();

            mockMvc.perform(get(BASE_URL + "/" + testCart.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItems", hasSize(1)))
                    .andExpect(jsonPath("$.cartItems[0].quantity", is(3)));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void getCartById_Unauthenticated_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(BASE_URL + "/" + testCart.getId()))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== POST /carts Tests ====================

    @Nested
    @DisplayName("POST /carts - Create New Cart")
    class CreateCartTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 200 with created cart when user has no cart")
        void createCart_UserHasNoCart_ReturnsCreatedCart() throws Exception {
            // Remove existing cart from another user
            when(jwtUtil.getCurrentUserId()).thenReturn(anotherUser.getId());

            Cart newCart = new Cart();
            newCart.setUser(anotherUser);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newCart)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", notNullValue()));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void createCart_Unauthenticated_ReturnsUnauthorized() throws Exception {
            Cart newCart = new Cart();

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newCart)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== DELETE /carts/{id} Tests ====================

    @Nested
    @DisplayName("DELETE /carts/{id} - Delete Cart")
    class DeleteCartTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 204 when cart is deleted successfully")
        void deleteCart_OwnCart_ReturnsNoContent() throws Exception {
            Long cartId = testCart.getId();

            mockMvc.perform(delete(BASE_URL + "/" + cartId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 404 when trying to delete another user's cart")
        void deleteCart_OtherUsersCart_Returns404() throws Exception {
            // Create cart for another user
            Cart anotherCart = new Cart();
            anotherCart.setUser(anotherUser);
            anotherCart.setCartItems(new ArrayList<>());
            anotherCart = cartRepository.save(anotherCart);

            mockMvc.perform(delete(BASE_URL + "/" + anotherCart.getId()))
                    .andExpect(status().isNotFound());

            // Verify cart still exists
            assertTrue(cartRepository.existsById(anotherCart.getId()));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 404 when cart does not exist")
        void deleteCart_CartNotExists_Returns404() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/99999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should delete cart with items successfully")
        void deleteCart_CartWithItems_DeletesCartAndItems() throws Exception {
            // Add cart item
            CartItem cartItem = new CartItem();
            cartItem.setCart(testCart);
            cartItem.setProduct(testProduct);
            cartItem.setQuantity(1L);
            cartItem.setPrice(new BigDecimal("100.00"));
            cartItem.setAvailable(true);
            CartItem savedItem = cartItemRepository.save(cartItem);

            Long cartId = testCart.getId();
            Long itemId = savedItem.getId();

            mockMvc.perform(delete(BASE_URL + "/" + cartId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void deleteCart_Unauthenticated_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/" + testCart.getId()))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== GET /carts/me/count Tests ====================

    @Nested
    @DisplayName("GET /carts/me/count - Count Cart Items")
    class CountCartItemsTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 0 when cart has no items")
        void countCartItems_EmptyCart_ReturnsZero() throws Exception {
            mockMvc.perform(get(BASE_URL + "/me/count"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("0"));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return correct count when cart has items")
        void countCartItems_CartWithItems_ReturnsCorrectCount() throws Exception {
            // Add first cart item
            CartItem cartItem1 = new CartItem();
            cartItem1.setCart(testCart);
            cartItem1.setProduct(testProduct);
            cartItem1.setQuantity(2L);
            cartItem1.setPrice(new BigDecimal("200.00"));
            cartItem1.setAvailable(true);
            cartItemRepository.save(cartItem1);

            // Create another product
            Product anotherProduct = new Product();
            anotherProduct.setName("Another Product");
            anotherProduct.setShortDescription("Short");
            anotherProduct.setDetailDescription("Detail");
            anotherProduct.setOriginalPrice(new BigDecimal("50.00"));
            anotherProduct.setIsAvailable(true);
            anotherProduct.setDeleted(false);
            anotherProduct.setCategory(testCategory);
            anotherProduct = productRepository.save(anotherProduct);

            // Add second cart item
            CartItem cartItem2 = new CartItem();
            cartItem2.setCart(testCart);
            cartItem2.setProduct(anotherProduct);
            cartItem2.setQuantity(1L);
            cartItem2.setPrice(new BigDecimal("50.00"));
            cartItem2.setAvailable(true);
            cartItemRepository.save(cartItem2);

            entityManager.flush();
            entityManager.clear();

            mockMvc.perform(get(BASE_URL + "/me/count"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("2"));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 0 when user has no cart")
        void countCartItems_NoCart_ReturnsZero() throws Exception {
            // Delete the test cart
            testUser.setCart(null);
            userRepository.save(testUser);
            cartRepository.delete(testCart);

            mockMvc.perform(get(BASE_URL + "/me/count"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("0"));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void countCartItems_Unauthenticated_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(BASE_URL + "/me/count"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== Response Structure Tests ====================

    @Nested
    @DisplayName("Response Structure Validation")
    class ResponseStructureTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Cart response should have correct structure")
        void cartResponse_HasCorrectStructure() throws Exception {
            // Add cart item for a complete response
            CartItem cartItem = new CartItem();
            cartItem.setCart(testCart);
            cartItem.setProduct(testProduct);
            cartItem.setQuantity(1L);
            cartItem.setPrice(new BigDecimal("100.00"));
            cartItem.setAvailable(true);
            cartItemRepository.save(cartItem);

            entityManager.flush();
            entityManager.clear();

            mockMvc.perform(get(BASE_URL + "/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.userId").exists())
                    .andExpect(jsonPath("$.cartItems").isArray())
                    .andExpect(jsonPath("$.cartItems[0].cartId").exists())
                    .andExpect(jsonPath("$.cartItems[0].productId").exists())
                    .andExpect(jsonPath("$.cartItems[0].productName").exists())
                    .andExpect(jsonPath("$.cartItems[0].quantity").exists())
                    .andExpect(jsonPath("$.cartItems[0].price").exists())
                    .andExpect(jsonPath("$.cartItems[0].available").exists());
        }
    }

    // ==================== Edge Cases Tests ====================

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should handle cart with many items")
        void getMyCart_ManyItems_ReturnsAllItems() throws Exception {
            // Add multiple cart items
            for (int i = 0; i < 5; i++) {
                Product product = new Product();
                product.setName("Product " + i);
                product.setShortDescription("Short " + i);
                product.setDetailDescription("Detail " + i);
                product.setOriginalPrice(new BigDecimal("10.00"));
                product.setIsAvailable(true);
                product.setDeleted(false);
                product.setCategory(testCategory);
                product = productRepository.save(product);

                CartItem item = new CartItem();
                item.setCart(testCart);
                item.setProduct(product);
                item.setQuantity((long) (i + 1));
                item.setPrice(new BigDecimal(10 * (i + 1)));
                item.setAvailable(true);
                cartItemRepository.save(item);
            }

            entityManager.flush();
            entityManager.clear();

            mockMvc.perform(get(BASE_URL + "/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItems", hasSize(5)));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should handle cart with unavailable items")
        void getMyCart_UnavailableItems_ReturnsItemsWithAvailabilityFlag() throws Exception {
            CartItem cartItem = new CartItem();
            cartItem.setCart(testCart);
            cartItem.setProduct(testProduct);
            cartItem.setQuantity(1L);
            cartItem.setPrice(new BigDecimal("100.00"));
            cartItem.setAvailable(false); // Unavailable item
            cartItemRepository.save(cartItem);

            entityManager.flush();
            entityManager.clear();

            mockMvc.perform(get(BASE_URL + "/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItems[0].available", is(false)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Admin should be able to access cart endpoints")
        void adminAccess_CanAccessCartEndpoints() throws Exception {
            mockMvc.perform(get(BASE_URL + "/me"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should handle negative cart ID gracefully")
        void getCartById_NegativeId_Returns404() throws Exception {
            mockMvc.perform(get(BASE_URL + "/-1"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should handle zero cart ID gracefully")
        void getCartById_ZeroId_Returns404() throws Exception {
            mockMvc.perform(get(BASE_URL + "/0"))
                    .andExpect(status().isNotFound());
        }
    }

    // ==================== Security Tests ====================
}

