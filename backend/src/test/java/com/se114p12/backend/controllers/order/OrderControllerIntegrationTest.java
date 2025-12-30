package com.se114p12.backend.controllers.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.se114p12.backend.constants.AppConstant;
import com.se114p12.backend.dtos.order.OrderRequestDTO;
import com.se114p12.backend.entities.authentication.Role;
import com.se114p12.backend.entities.cart.Cart;
import com.se114p12.backend.entities.cart.CartItem;
import com.se114p12.backend.entities.order.Order;
import com.se114p12.backend.entities.order.OrderDetail;
import com.se114p12.backend.entities.product.Product;
import com.se114p12.backend.entities.product.ProductCategory;
import com.se114p12.backend.entities.user.User;
import com.se114p12.backend.enums.*;
import com.se114p12.backend.neo4j.repositories.ProductNeo4jRepository;
import com.se114p12.backend.neo4j.repositories.UserNeo4jRepository;
import com.se114p12.backend.repositories.authentication.RoleRepository;
import com.se114p12.backend.repositories.authentication.UserRepository;
import com.se114p12.backend.repositories.cart.CartItemRepository;
import com.se114p12.backend.repositories.cart.CartRepository;
import com.se114p12.backend.repositories.order.OrderDetailRepository;
import com.se114p12.backend.repositories.order.OrderRepository;
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
import java.time.Instant;
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
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

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

    @MockitoBean
    private ProductNeo4jRepository productNeo4jRepository;

    @MockitoBean
    private UserNeo4jRepository userNeo4jRepository;

    private static final String BASE_URL = AppConstant.API_BASE_PATH + "/orders";

    private User testUser;
    private User anotherUser;
    private ProductCategory testCategory;
    private Product testProduct;
    private Product anotherProduct;
    private Order testOrder;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        // Clear existing data
        orderDetailRepository.deleteAll();
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
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
        Role adminRole = roleRepository.findByName("ADMIN").orElseGet(() -> {
            Role role = new Role();
            role.setName("ADMIN");
            role.setDescription("Admin user role");
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

        // Create admin user (kept for potential future tests)
        User adminUser = new User();
        adminUser.setFullname("Admin User");
        adminUser.setUsername("adminuser");
        adminUser.setEmail("adminuser@example.com");
        adminUser.setPhone("+84987654323");
        adminUser.setPassword("password123");
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser.setLoginProvider(LoginProvider.LOCAL);
        adminUser.setRole(adminRole);
        userRepository.save(adminUser);

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

        // Create another product
        anotherProduct = new Product();
        anotherProduct.setName("Another Product");
        anotherProduct.setShortDescription("Another short desc");
        anotherProduct.setDetailDescription("Another detailed description");
        anotherProduct.setOriginalPrice(new BigDecimal("50.00"));
        anotherProduct.setIsAvailable(true);
        anotherProduct.setDeleted(false);
        anotherProduct.setCategory(testCategory);
        anotherProduct = productRepository.save(anotherProduct);

        // Create cart for test user
        testCart = new Cart();
        testCart.setUser(testUser);
        testCart.setCartItems(new ArrayList<>());
        testCart = cartRepository.save(testCart);

        // Update user's cart reference
        testUser.setCart(testCart);
        testUser = userRepository.save(testUser);

        // Create a test order
        testOrder = new Order();
        testOrder.setDestinationLatitude(10.762622);
        testOrder.setDestinationLongitude(106.660172);
        testOrder.setShippingAddress("123 Test Street, District 1, HCMC");
        testOrder.setTotalPrice(new BigDecimal("200.00"));
        testOrder.setNote("Test order note");
        testOrder.setExpectedDeliveryTime(Instant.now().plusSeconds(3600));
        testOrder.setOrderStatus(OrderStatus.PENDING);
        testOrder.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        testOrder.setPaymentStatus(PaymentStatus.PENDING);
        testOrder.setTxnRef("ORD-TEST-001");
        testOrder.setUser(testUser);
        testOrder.setOrderDetails(new ArrayList<>());
        testOrder = orderRepository.save(testOrder);

        // Add order details
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrder(testOrder);
        orderDetail.setProductId(testProduct.getId());
        orderDetail.setProductName(testProduct.getName());
        orderDetail.setCategoryId(testCategory.getId());
        orderDetail.setCategoryName(testCategory.getName());
        orderDetail.setQuantity(2L);
        orderDetail.setPrice(new BigDecimal("100.00"));
        orderDetailRepository.save(orderDetail);

        testOrder.getOrderDetails().add(orderDetail);
        testOrder = orderRepository.save(testOrder);

        // Configure JwtUtil mock to return the test user's ID by default
        when(jwtUtil.getCurrentUserId()).thenReturn(testUser.getId());
    }

    // ==================== GET /orders/{id} Tests ====================

    @Nested
    @DisplayName("GET /orders/{id} - Get Order By ID")
    class GetOrderByIdTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 200 with order when order exists")
        void getOrderById_OrderExists_ReturnsOrder() throws Exception {
            mockMvc.perform(get(BASE_URL + "/" + testOrder.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(testOrder.getId().intValue())))
                    .andExpect(jsonPath("$.userId", is(testUser.getId().intValue())))
                    .andExpect(jsonPath("$.shippingAddress", is("123 Test Street, District 1, HCMC")))
                    .andExpect(jsonPath("$.orderStatus", is("PENDING")))
                    .andExpect(jsonPath("$.paymentMethod", is("CASH_ON_DELIVERY")));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return order with order details")
        void getOrderById_OrderWithDetails_ReturnsOrderWithDetails() throws Exception {
            mockMvc.perform(get(BASE_URL + "/" + testOrder.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderDetails", hasSize(1)))
                    .andExpect(jsonPath("$.orderDetails[0].productId", is(testProduct.getId().intValue())))
                    .andExpect(jsonPath("$.orderDetails[0].productName", is("Test Product")))
                    .andExpect(jsonPath("$.orderDetails[0].quantity", is(2)));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 404 when order does not exist")
        void getOrderById_OrderNotExists_Returns404() throws Exception {
            mockMvc.perform(get(BASE_URL + "/99999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void getOrderById_Unauthenticated_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(BASE_URL + "/" + testOrder.getId()))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== GET /orders/me Tests ====================

    @Nested
    @DisplayName("GET /orders/me - Get Current User's Orders")
    class GetMyOrdersTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 200 with orders when user has orders")
        void getMyOrders_UserHasOrders_ReturnsOrders() throws Exception {
            mockMvc.perform(get(BASE_URL + "/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id", is(testOrder.getId().intValue())))
                    .andExpect(jsonPath("$.content[0].userId", is(testUser.getId().intValue())));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return empty content when user has no orders")
        void getMyOrders_UserHasNoOrders_ReturnsEmptyContent() throws Exception {
            when(jwtUtil.getCurrentUserId()).thenReturn(anotherUser.getId());

            mockMvc.perform(get(BASE_URL + "/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return paginated results")
        void getMyOrders_WithPagination_ReturnsPaginatedResults() throws Exception {
            // Create additional orders for the test user
            for (int i = 0; i < 5; i++) {
                Order order = new Order();
                order.setDestinationLatitude(10.762622);
                order.setDestinationLongitude(106.660172);
                order.setShippingAddress("Address " + i);
                order.setTotalPrice(new BigDecimal("100.00"));
                order.setOrderStatus(OrderStatus.PENDING);
                order.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
                order.setPaymentStatus(PaymentStatus.PENDING);
                order.setTxnRef("ORD-TEST-00" + (i + 2));
                order.setUser(testUser);
                order.setOrderDetails(new ArrayList<>());
                orderRepository.save(order);
            }

            entityManager.flush();
            entityManager.clear();

            mockMvc.perform(get(BASE_URL + "/me")
                            .param("page", "0")
                            .param("size", "3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(3)))
                    .andExpect(jsonPath("$.totalElements", is(6)))
                    .andExpect(jsonPath("$.totalPages", is(2)));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void getMyOrders_Unauthenticated_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(BASE_URL + "/me"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== GET /orders Tests (Admin Only) ====================

    @Nested
    @DisplayName("GET /orders - Get All Orders (Admin Only)")
    class GetAllOrdersTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 200 with all orders for admin")
        void getAllOrders_AsAdmin_ReturnsAllOrders() throws Exception {
            // Create orders for another user
            Order anotherOrder = new Order();
            anotherOrder.setDestinationLatitude(10.762622);
            anotherOrder.setDestinationLongitude(106.660172);
            anotherOrder.setShippingAddress("Another Address");
            anotherOrder.setTotalPrice(new BigDecimal("150.00"));
            anotherOrder.setOrderStatus(OrderStatus.CONFIRMED);
            anotherOrder.setPaymentMethod(PaymentMethod.VNPAY);
            anotherOrder.setPaymentStatus(PaymentStatus.COMPLETED);
            anotherOrder.setTxnRef("ORD-TEST-002");
            anotherOrder.setUser(anotherUser);
            anotherOrder.setOrderDetails(new ArrayList<>());
            orderRepository.save(anotherOrder);

            entityManager.flush();
            entityManager.clear();

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return paginated results for admin")
        void getAllOrders_WithPagination_ReturnsPaginatedResults() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page", is(0)))
                    .andExpect(jsonPath("$.size", is(10)));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void getAllOrders_Unauthenticated_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== POST /orders/create Tests ====================

    @Nested
    @DisplayName("POST /orders/create - Create Order")
    class CreateOrderTests {

        @BeforeEach
        void setUpCart() {
            // Add cart item for creation tests
            CartItem cartItem = new CartItem();
            cartItem.setCart(testCart);
            cartItem.setProduct(testProduct);
            cartItem.setQuantity(2L);
            cartItem.setPrice(new BigDecimal("200.00"));
            cartItem.setAvailable(true);
            cartItemRepository.save(cartItem);

            testCart.getCartItems().add(cartItem);
            cartRepository.save(testCart);

            entityManager.flush();
            entityManager.clear();
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 200 with created order when valid request")
        void createOrder_ValidRequest_ReturnsCreatedOrder() throws Exception {
            OrderRequestDTO request = new OrderRequestDTO();
            request.setDestinationLatitude(10.762622);
            request.setDestinationLongitude(106.660172);
            request.setShippingAddress("New Shipping Address");
            request.setNote("New order note");
            request.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

            mockMvc.perform(post(BASE_URL + "/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.shippingAddress", is("New Shipping Address")))
                    .andExpect(jsonPath("$.note", is("New order note")))
                    .andExpect(jsonPath("$.orderStatus", is("PENDING")))
                    .andExpect(jsonPath("$.paymentMethod", is("CASH_ON_DELIVERY")))
                    .andExpect(jsonPath("$.paymentStatus", is("PENDING")));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 400 when cart is empty")
        void createOrder_EmptyCart_ReturnsBadRequest() throws Exception {
            // Clear the cart
            cartItemRepository.deleteAll();
            testCart.getCartItems().clear();
            cartRepository.save(testCart);

            entityManager.flush();
            entityManager.clear();

            OrderRequestDTO request = new OrderRequestDTO();
            request.setDestinationLatitude(10.762622);
            request.setDestinationLongitude(106.660172);
            request.setShippingAddress("New Shipping Address");
            request.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

            mockMvc.perform(post(BASE_URL + "/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 422 when required fields are missing")
        void createOrder_MissingRequiredFields_ReturnsBadRequest() throws Exception {
            OrderRequestDTO request = new OrderRequestDTO();
            // Missing required fields

            mockMvc.perform(post(BASE_URL + "/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is(422));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should create order with VNPAY payment method")
        void createOrder_WithVNPay_ReturnsCreatedOrder() throws Exception {
            OrderRequestDTO request = new OrderRequestDTO();
            request.setDestinationLatitude(10.762622);
            request.setDestinationLongitude(106.660172);
            request.setShippingAddress("VNPay Address");
            request.setPaymentMethod(PaymentMethod.VNPAY);

            mockMvc.perform(post(BASE_URL + "/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.paymentMethod", is("VNPAY")));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void createOrder_Unauthenticated_ReturnsUnauthorized() throws Exception {
            OrderRequestDTO request = new OrderRequestDTO();
            request.setDestinationLatitude(10.762622);
            request.setDestinationLongitude(106.660172);
            request.setShippingAddress("New Shipping Address");
            request.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

            mockMvc.perform(post(BASE_URL + "/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== PUT /orders/update/{id} Tests ====================

    @Nested
    @DisplayName("PUT /orders/update/{id} - Update Order")
    class UpdateOrderTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 200 with updated order when valid request")
        void updateOrder_ValidRequest_ReturnsUpdatedOrder() throws Exception {
            OrderRequestDTO request = new OrderRequestDTO();
            request.setDestinationLatitude(10.800000);
            request.setDestinationLongitude(106.700000);
            request.setShippingAddress("Updated Shipping Address");
            request.setNote("Updated note");
            request.setPaymentMethod(PaymentMethod.VNPAY);

            mockMvc.perform(put(BASE_URL + "/update/" + testOrder.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(testOrder.getId().intValue())))
                    .andExpect(jsonPath("$.note", is("Updated note")))
                    .andExpect(jsonPath("$.paymentMethod", is("VNPAY")));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 404 when order does not exist")
        void updateOrder_OrderNotExists_Returns404() throws Exception {
            OrderRequestDTO request = new OrderRequestDTO();
            request.setDestinationLatitude(10.800000);
            request.setDestinationLongitude(106.700000);
            request.setShippingAddress("Updated Address");
            request.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

            mockMvc.perform(put(BASE_URL + "/update/99999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void updateOrder_Unauthenticated_ReturnsUnauthorized() throws Exception {
            OrderRequestDTO request = new OrderRequestDTO();
            request.setDestinationLatitude(10.800000);
            request.setDestinationLongitude(106.700000);
            request.setShippingAddress("Updated Address");
            request.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

            mockMvc.perform(put(BASE_URL + "/update/" + testOrder.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== DELETE /orders/delete/{id} Tests ====================

    @Nested
    @DisplayName("DELETE /orders/delete/{id} - Delete Order")
    class DeleteOrderTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 204 when order is deleted successfully")
        void deleteOrder_OrderExists_ReturnsNoContent() throws Exception {
            Long orderId = testOrder.getId();

            mockMvc.perform(delete(BASE_URL + "/delete/" + orderId))
                    .andExpect(status().isNoContent());

            // Verify order was deleted
            assertFalse(orderRepository.existsById(orderId));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 404 when order does not exist")
        void deleteOrder_OrderNotExists_Returns404() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/delete/99999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void deleteOrder_Unauthenticated_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/delete/" + testOrder.getId()))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== POST /orders/cancel/{id} Tests ====================

    @Nested
    @DisplayName("POST /orders/cancel/{id} - Cancel Order")
    class CancelOrderTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 204 when order is cancelled successfully")
        void cancelOrder_PendingOrder_ReturnsNoContent() throws Exception {
            mockMvc.perform(post(BASE_URL + "/cancel/" + testOrder.getId()))
                    .andExpect(status().isNoContent());

            // Verify order status was updated
            entityManager.flush();
            entityManager.clear();
            Order updatedOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
            assertEquals(OrderStatus.CANCELED, updatedOrder.getOrderStatus());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 400 when trying to cancel confirmed order")
        void cancelOrder_ConfirmedOrder_ReturnsBadRequest() throws Exception {
            // Update order status to CONFIRMED (which has code > 1)
            testOrder.setOrderStatus(OrderStatus.CONFIRMED);
            orderRepository.save(testOrder);

            entityManager.flush();
            entityManager.clear();

            mockMvc.perform(post(BASE_URL + "/cancel/" + testOrder.getId()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 404 when order does not exist")
        void cancelOrder_OrderNotExists_Returns404() throws Exception {
            mockMvc.perform(post(BASE_URL + "/cancel/99999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void cancelOrder_Unauthenticated_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(post(BASE_URL + "/cancel/" + testOrder.getId()))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== PUT /orders/{orderId}/delivered Tests ====================

    @Nested
    @DisplayName("PUT /orders/{orderId}/delivered - Mark Order as Delivered")
    class MarkOrderAsDeliveredTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 200 when order is marked as delivered")
        void markAsDelivered_ShippingOrder_ReturnsOk() throws Exception {
            // Set order status to SHIPPING
            testOrder.setOrderStatus(OrderStatus.SHIPPING);
            orderRepository.save(testOrder);

            entityManager.flush();
            entityManager.clear();

            mockMvc.perform(put(BASE_URL + "/" + testOrder.getId() + "/delivered"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Order marked as delivered."));

            // Verify order status was updated
            Order updatedOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
            assertEquals(OrderStatus.COMPLETED, updatedOrder.getOrderStatus());
            assertNotNull(updatedOrder.getActualDeliveryTime());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 500 when order is not in SHIPPING status")
        void markAsDelivered_NotShippingOrder_ReturnsError() throws Exception {
            // Order is in PENDING status, not SHIPPING
            mockMvc.perform(put(BASE_URL + "/" + testOrder.getId() + "/delivered"))
                    .andExpect(status().is5xxServerError());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should set payment status to COMPLETED for COD orders")
        void markAsDelivered_CODOrder_SetsPaymentCompleted() throws Exception {
            testOrder.setOrderStatus(OrderStatus.SHIPPING);
            testOrder.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
            orderRepository.save(testOrder);

            entityManager.flush();
            entityManager.clear();

            mockMvc.perform(put(BASE_URL + "/" + testOrder.getId() + "/delivered"))
                    .andExpect(status().isOk());

            Order updatedOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
            assertEquals(PaymentStatus.COMPLETED, updatedOrder.getPaymentStatus());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void markAsDelivered_Unauthenticated_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(put(BASE_URL + "/" + testOrder.getId() + "/delivered"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== GET /orders/{orderId}/status Tests (Admin Only) ====================

    @Nested
    @DisplayName("GET /orders/{orderId}/status - Update Order Status (Admin Only)")
    class UpdateOrderStatusTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 204 when status is updated successfully")
        void updateStatus_ValidStatusTransition_ReturnsNoContent() throws Exception {
            mockMvc.perform(get(BASE_URL + "/" + testOrder.getId() + "/status")
                            .param("status", "CONFIRMED"))
                    .andExpect(status().isNoContent());

            // Verify order status was updated
            entityManager.flush();
            entityManager.clear();
            Order updatedOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
            assertEquals(OrderStatus.CONFIRMED, updatedOrder.getOrderStatus());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 400 for invalid status transition")
        void updateStatus_InvalidStatusTransition_ReturnsBadRequest() throws Exception {
            // Try to skip from PENDING to SHIPPING (skipping CONFIRMED and PROCESSING)
            mockMvc.perform(get(BASE_URL + "/" + testOrder.getId() + "/status")
                            .param("status", "SHIPPING"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should allow updating to CANCELED status")
        void updateStatus_ToCanceled_ReturnsNoContent() throws Exception {
            mockMvc.perform(get(BASE_URL + "/" + testOrder.getId() + "/status")
                            .param("status", "CANCELED"))
                    .andExpect(status().isNoContent());

            Order updatedOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
            assertEquals(OrderStatus.CANCELED, updatedOrder.getOrderStatus());
        }


        @Test
        @DisplayName("Should return 401 when not authenticated")
        void updateStatus_Unauthenticated_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(BASE_URL + "/" + testOrder.getId() + "/status")
                            .param("status", "CONFIRMED"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== Response Structure Tests ====================

    @Nested
    @DisplayName("Response Structure Validation")
    class ResponseStructureTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Order response should have correct structure")
        void orderResponse_HasCorrectStructure() throws Exception {
            mockMvc.perform(get(BASE_URL + "/" + testOrder.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.destinationLatitude").exists())
                    .andExpect(jsonPath("$.destinationLongitude").exists())
                    .andExpect(jsonPath("$.shippingAddress").exists())
                    .andExpect(jsonPath("$.totalPrice").exists())
                    .andExpect(jsonPath("$.orderStatus").exists())
                    .andExpect(jsonPath("$.paymentMethod").exists())
                    .andExpect(jsonPath("$.paymentStatus").exists())
                    .andExpect(jsonPath("$.userId").exists())
                    .andExpect(jsonPath("$.orderDetails").isArray());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Order detail response should have correct structure")
        void orderDetailResponse_HasCorrectStructure() throws Exception {
            mockMvc.perform(get(BASE_URL + "/" + testOrder.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderDetails[0].productId").exists())
                    .andExpect(jsonPath("$.orderDetails[0].productName").exists())
                    .andExpect(jsonPath("$.orderDetails[0].quantity").exists())
                    .andExpect(jsonPath("$.orderDetails[0].price").exists());
        }
    }

    // ==================== Edge Cases Tests ====================

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should handle negative order ID gracefully")
        void getOrderById_NegativeId_Returns404() throws Exception {
            mockMvc.perform(get(BASE_URL + "/-1"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should handle zero order ID gracefully")
        void getOrderById_ZeroId_Returns404() throws Exception {
            mockMvc.perform(get(BASE_URL + "/0"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should handle order with multiple order details")
        void getOrderById_MultipleOrderDetails_ReturnsAllDetails() throws Exception {
            // Add more order details
            OrderDetail detail2 = new OrderDetail();
            detail2.setOrder(testOrder);
            detail2.setProductId(anotherProduct.getId());
            detail2.setProductName(anotherProduct.getName());
            detail2.setCategoryId(testCategory.getId());
            detail2.setCategoryName(testCategory.getName());
            detail2.setQuantity(3L);
            detail2.setPrice(new BigDecimal("50.00"));
            orderDetailRepository.save(detail2);

            entityManager.flush();
            entityManager.clear();

            mockMvc.perform(get(BASE_URL + "/" + testOrder.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderDetails", hasSize(2)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Admin should be able to access all endpoints")
        void adminAccess_CanAccessAllEndpoints() throws Exception {
            mockMvc.perform(get(BASE_URL + "/" + testOrder.getId()))
                    .andExpect(status().isOk());

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should handle orders with different statuses")
        void getMyOrders_DifferentStatuses_ReturnsAllOrders() throws Exception {
            // Create orders with different statuses
            OrderStatus[] statuses = {OrderStatus.CONFIRMED, OrderStatus.PROCESSING, OrderStatus.COMPLETED};
            for (int i = 0; i < statuses.length; i++) {
                Order order = new Order();
                order.setDestinationLatitude(10.762622);
                order.setDestinationLongitude(106.660172);
                order.setShippingAddress("Address " + i);
                order.setTotalPrice(new BigDecimal("100.00"));
                order.setOrderStatus(statuses[i]);
                order.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
                order.setPaymentStatus(PaymentStatus.PENDING);
                order.setTxnRef("ORD-STATUS-" + i);
                order.setUser(testUser);
                order.setOrderDetails(new ArrayList<>());
                orderRepository.save(order);
            }

            entityManager.flush();
            entityManager.clear();

            mockMvc.perform(get(BASE_URL + "/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(4))); // 1 original + 3 new
        }
    }

    // ==================== Order Lifecycle Tests ====================

    @Nested
    @DisplayName("Order Lifecycle")
    class OrderLifecycleTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should progress through order statuses correctly")
        void orderLifecycle_ProgressThroughStatuses() throws Exception {
            // PENDING -> CONFIRMED
            mockMvc.perform(get(BASE_URL + "/" + testOrder.getId() + "/status")
                            .param("status", "CONFIRMED"))
                    .andExpect(status().isNoContent());

            // CONFIRMED -> PROCESSING
            mockMvc.perform(get(BASE_URL + "/" + testOrder.getId() + "/status")
                            .param("status", "PROCESSING"))
                    .andExpect(status().isNoContent());

            entityManager.flush();
            entityManager.clear();

            Order order = orderRepository.findById(testOrder.getId()).orElseThrow();
            assertEquals(OrderStatus.PROCESSING, order.getOrderStatus());
        }
    }
}

