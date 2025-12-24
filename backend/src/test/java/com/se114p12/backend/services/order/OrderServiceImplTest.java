package com.se114p12.backend.services.order;

import com.se114p12.backend.configs.ShopLocationConfig;
import com.se114p12.backend.dtos.delivery.DeliveryResponseDTO;
import com.se114p12.backend.dtos.order.OrderRequestDTO;
import com.se114p12.backend.dtos.order.OrderResponseDTO;
import com.se114p12.backend.entities.cart.Cart;
import com.se114p12.backend.entities.cart.CartItem;
import com.se114p12.backend.entities.order.Order;
import com.se114p12.backend.entities.order.OrderDetail;
import com.se114p12.backend.entities.product.Product;
import com.se114p12.backend.entities.product.ProductCategory;
import com.se114p12.backend.entities.shipper.Shipper;
import com.se114p12.backend.entities.user.User;
import com.se114p12.backend.entities.variation.Variation;
import com.se114p12.backend.entities.variation.VariationOption;
import com.se114p12.backend.enums.OrderStatus;
import com.se114p12.backend.enums.PaymentMethod;
import com.se114p12.backend.enums.PaymentStatus;
import com.se114p12.backend.exceptions.BadRequestException;
import com.se114p12.backend.exceptions.ResourceNotFoundException;
import com.se114p12.backend.mappers.order.OrderMapper;
import com.se114p12.backend.neo4j.entities.ProductNode;
import com.se114p12.backend.neo4j.repositories.ProductNeo4jRepository;
import com.se114p12.backend.neo4j.repositories.UserNeo4jRepository;
import com.se114p12.backend.repositories.authentication.UserRepository;
import com.se114p12.backend.repositories.cart.CartItemRepository;
import com.se114p12.backend.repositories.cart.CartRepository;
import com.se114p12.backend.repositories.order.OrderDetailRepository;
import com.se114p12.backend.repositories.order.OrderRepository;
import com.se114p12.backend.repositories.promotion.PromotionRepository;
import com.se114p12.backend.repositories.shipper.ShipperRepository;
import com.se114p12.backend.services.delivery.MapService;
import com.se114p12.backend.services.notification.NotificationService;
import com.se114p12.backend.services.promotion.UserPromotionService;
import com.se114p12.backend.util.JwtUtil;
import com.se114p12.backend.vo.PageVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrderServiceImplTest {

    private ShopLocationConfig shopLocationConfig;
    private OrderRepository orderRepository;
    private OrderDetailRepository orderDetailRepository;
    private OrderMapper orderMapper;
    private UserRepository userRepository;
    private JwtUtil jwtUtil;
    private CartRepository cartRepository;
    private CartItemRepository cartItemRepository;
    private ShipperRepository shipperRepository;
    private PromotionRepository promotionRepository; // not directly used but required in ctor
    private UserPromotionService userPromotionService;
    private UserNeo4jRepository userNeo4jRepository;
    private ProductNeo4jRepository productNeo4jRepository;
    private MapService mapService;
    private NotificationService notificationService;

    private OrderServiceImpl service;

    @BeforeEach
    void init() {
        shopLocationConfig = mock(ShopLocationConfig.class);
        orderRepository = mock(OrderRepository.class);
        orderDetailRepository = mock(OrderDetailRepository.class);
        orderMapper = mock(OrderMapper.class);
        userRepository = mock(UserRepository.class);
        jwtUtil = mock(JwtUtil.class);
        cartRepository = mock(CartRepository.class);
        cartItemRepository = mock(CartItemRepository.class);
        shipperRepository = mock(ShipperRepository.class);
        promotionRepository = mock(PromotionRepository.class);
        userPromotionService = mock(UserPromotionService.class);
        userNeo4jRepository = mock(UserNeo4jRepository.class);
        productNeo4jRepository = mock(ProductNeo4jRepository.class);
        mapService = mock(MapService.class);
        notificationService = mock(NotificationService.class);

        service = new OrderServiceImpl(
                shopLocationConfig,
                orderRepository,
                orderDetailRepository,
                orderMapper,
                userRepository,
                jwtUtil,
                cartRepository,
                cartItemRepository,
                shipperRepository,
                promotionRepository,
                userPromotionService,
                userNeo4jRepository,
                productNeo4jRepository,
                mapService,
                notificationService
        );
    }

    @Test
    @DisplayName("getAll maps page content and metadata")
    void getAll_success() {
        Order order = new Order();
        order.setId(1L);
        Page<Order> page = new PageImpl<>(List.of(order), PageRequest.of(0, 10), 1);
        when(orderRepository.findAll(nullable(Specification.class), any(Pageable.class))).thenReturn(page);
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(1L);
        when(orderMapper.entityToResponseDTO(order)).thenReturn(dto);

        PageVO<OrderResponseDTO> vo = service.getAll(null, PageRequest.of(0, 10));

        assertEquals(1, vo.getTotalElements());
        assertEquals(1, vo.getContent().size());
        assertEquals(1L, vo.getContent().get(0).getId());
    }

    @Test
    @DisplayName("getById returns mapped DTO or throws if not found")
    void getById_cases() {
        Order order = new Order(); order.setId(5L);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
        OrderResponseDTO dto = new OrderResponseDTO(); dto.setId(5L);
        when(orderMapper.entityToResponseDTO(order)).thenReturn(dto);

        OrderResponseDTO result = service.getById(5L);
        assertEquals(5L, result.getId());

        when(orderRepository.findById(6L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getById(6L));
    }

    @Test
    @DisplayName("getOrdersByUserId composes specification and maps page")
    void getOrdersByUserId_success() {
        Order order = new Order();
        order.setId(2L);
        Page<Order> page = new PageImpl<>(List.of(order), PageRequest.of(0, 10), 1);
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        OrderResponseDTO dto = new OrderResponseDTO(); dto.setId(2L);
        when(orderMapper.entityToResponseDTO(order)).thenReturn(dto);

        PageVO<OrderResponseDTO> vo = service.getOrdersByUserId(100L, null, PageRequest.of(0, 10));
        assertEquals(1, vo.getContent().size());
        assertEquals(2L, vo.getContent().get(0).getId());
    }

    private Cart buildCartWithItems(User user) {
        Cart cart = new Cart();
        cart.setId(99L);
        cart.setUser(user);

        ProductCategory cat = new ProductCategory();
        cat.setId(11L); cat.setName("Cat");

        Product p1 = new Product();
        p1.setId(1L);
        p1.setName("P1");
        p1.setOriginalPrice(new BigDecimal("10.00"));
        p1.setCategory(cat);
        p1.setIsAvailable(true);

        Variation v = new Variation();
        v.setId(1L); v.setName("Size");
        VariationOption option = new VariationOption();
        option.setVariation(v); option.setValue("M"); option.setAdditionalPrice(0.0);

        CartItem ci1 = new CartItem();
        ci1.setProduct(p1);
        ci1.setQuantity(2L);
        java.util.Set<VariationOption> optSet = new java.util.HashSet<>();
        optSet.add(option);
        ci1.setVariationOptions(optSet);

        cart.setCartItems(new ArrayList<>(List.of(ci1)));
        return cart;
    }

    private OrderRequestDTO buildOrderRequest() {
        OrderRequestDTO req = new OrderRequestDTO();
        req.setDestinationLatitude(10.1);
        req.setDestinationLongitude(106.2);
        req.setShippingAddress("Addr");
        req.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        return req;
    }

    @Test
    @DisplayName("create builds order from cart, clears cart, saves and notifies - no promotion")
    void create_success_noPromotion() {
        when(jwtUtil.getCurrentUserId()).thenReturn(7L);
        User user = new User(); user.setId(7L);
        Cart cart = buildCartWithItems(user);
        when(cartRepository.findByUserId(7L)).thenReturn(Optional.of(cart));
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        // Save order: capture to assert derived fields
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponseDTO dto = new OrderResponseDTO(); dto.setId(123L);
        when(orderMapper.entityToResponseDTO(any(Order.class))).thenReturn(dto);

        OrderResponseDTO result = service.create(buildOrderRequest());

        assertEquals(123L, result.getId());
        // verify cart cleared and items deleted
        verify(cartItemRepository).deleteAll(anyList());
        verify(cartRepository).deleteById(99L);
        // verify notification sent through NotificationService
        verify(notificationService, atLeastOnce()).pushNotification(any());
        // verify recommend path hits neo4j save methods (async private still calls repos synchronously)
        verify(userNeo4jRepository, atLeastOnce()).save(any());
        verify(productNeo4jRepository, atLeastOnce()).saveAll(anyCollection());

        verify(orderRepository, atLeastOnce()).save(orderCaptor.capture());
        Order saved = orderCaptor.getValue();
        assertNotNull(saved.getTxnRef());
        assertEquals(OrderStatus.PENDING, saved.getOrderStatus());
        assertEquals(PaymentStatus.PENDING, saved.getPaymentStatus());
        assertEquals(new BigDecimal("20.00"), saved.getTotalPrice());
        assertEquals(1, saved.getOrderDetails().size());
    }

    @Test
    @DisplayName("create applies promotion discount when provided")
    void create_withPromotion_appliesDiscount() {
        when(jwtUtil.getCurrentUserId()).thenReturn(7L);
        User user = new User(); user.setId(7L);
        Cart cart = buildCartWithItems(user);
        when(cartRepository.findByUserId(7L)).thenReturn(Optional.of(cart));
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        // Mock promotion application to subtract 5.00
        com.se114p12.backend.entities.promotion.Promotion promo = new com.se114p12.backend.entities.promotion.Promotion();
        promo.setDiscountValue(new BigDecimal("5.00"));
        when(userPromotionService.applyPromotion(eq(7L), eq(99L), any(BigDecimal.class))).thenReturn(promo);

        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderMapper.entityToResponseDTO(any(Order.class))).thenReturn(new OrderResponseDTO());

        OrderRequestDTO req = buildOrderRequest();
        req.setPromotionId(99L);
        OrderResponseDTO out = service.create(req);
        assertNotNull(out);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, atLeastOnce()).save(orderCaptor.capture());
        Order saved = orderCaptor.getValue();
        assertEquals(new BigDecimal("15.00"), saved.getTotalPrice());
    }

    @Test
    @DisplayName("create throws when cart missing or empty or user missing")
    void create_error_cases() {
        when(jwtUtil.getCurrentUserId()).thenReturn(1L);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.create(buildOrderRequest()));

        // empty cart
        Cart emptyCart = new Cart(); emptyCart.setCartItems(new ArrayList<>()); emptyCart.setId(1L);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(emptyCart));
        assertThrows(BadRequestException.class, () -> service.create(buildOrderRequest()));

        // user missing
        Cart cart = buildCartWithItems(new User());
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.create(buildOrderRequest()));
    }

    @Test
    @DisplayName("update updates provided fields and saves; throws when order not found")
    void update_cases() {
        Order order = new Order(); order.setId(10L);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderMapper.entityToResponseDTO(any(Order.class))).thenReturn(new OrderResponseDTO());

        OrderRequestDTO req = new OrderRequestDTO();
        req.setDestinationLatitude(1.0);
        req.setDestinationLongitude(2.0);
        req.setNote("n");
        req.setPaymentMethod(PaymentMethod.VNPAY);

        service.update(10L, req);
        assertEquals(1.0, order.getDestinationLatitude());
        assertEquals(2.0, order.getDestinationLongitude());
        assertEquals("n", order.getNote());
        assertEquals(PaymentMethod.VNPAY, order.getPaymentMethod());

        when(orderRepository.findById(11L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.update(11L, new OrderRequestDTO()));
    }

    @Test
    @DisplayName("cancelOrder sets status to CANCELED when allowed; else throws")
    void cancelOrder_cases() {
        Order pending = new Order(); pending.setId(1L); pending.setOrderStatus(OrderStatus.PENDING);
        User u1 = new User(); u1.setId(10L); pending.setUser(u1);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(pending));
        service.cancelOrder(1L);
        assertEquals(OrderStatus.CANCELED, pending.getOrderStatus());
        verify(notificationService).pushNotification(any());

        Order confirmed = new Order(); confirmed.setOrderStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findById(2L)).thenReturn(Optional.of(confirmed));
        assertThrows(BadRequestException.class, () -> service.cancelOrder(2L));

        when(orderRepository.findById(3L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.cancelOrder(3L));
    }

    @Test
    @DisplayName("delete removes order when exists; throws when not found")
    void delete_cases() {
        Order order = new Order();
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
        service.delete(5L);
        verify(orderRepository).delete(order);

        when(orderRepository.findById(6L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.delete(6L));
    }

    @Test
    @DisplayName("markOrderAsDelivered requires SHIPPING, updates status/time, COD completes payment, frees shipper")
    void markOrderAsDelivered_cases() {
        // wrong status
        Order wrong = new Order(); wrong.setOrderStatus(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(wrong));
        assertThrows(IllegalStateException.class, () -> service.markOrderAsDelivered(1L));

        // correct status and COD
        Shipper shipper = new Shipper(); shipper.setIsAvailable(false);
        Order shipping = new Order();
        shipping.setOrderStatus(OrderStatus.SHIPPING);
        shipping.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        shipping.setShipper(shipper);
        User u2 = new User(); u2.setId(20L); shipping.setUser(u2);
        when(orderRepository.findById(2L)).thenReturn(Optional.of(shipping));

        service.markOrderAsDelivered(2L);
        assertEquals(OrderStatus.COMPLETED, shipping.getOrderStatus());
        assertNotNull(shipping.getActualDeliveryTime());
        assertEquals(PaymentStatus.COMPLETED, shipping.getPaymentStatus());
        assertTrue(shipper.getIsAvailable());
        verify(shipperRepository).save(shipper);
        verify(notificationService, atLeastOnce()).pushNotification(any());
    }

    @Test
    @DisplayName("updateStatus enforces sequential status unless CANCELED; SHIPPING assigns shipper and expected time")
    void updateStatus_cases() {
        // invalid transition
        Order ord = new Order(); ord.setOrderStatus(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(ord));
        assertThrows(BadRequestException.class, () -> service.updateStatus(1L, OrderStatus.PROCESSING));

        // SHIPPING: requires available shipper and coords
        Order ord2 = new Order();
        ord2.setOrderStatus(OrderStatus.PROCESSING);
        ord2.setDestinationLatitude(10.0);
        ord2.setDestinationLongitude(106.0);
        User u3 = new User(); u3.setId(30L); ord2.setUser(u3);
        when(orderRepository.findById(2L)).thenReturn(Optional.of(ord2));

        Shipper s1 = new Shipper(); s1.setIsAvailable(true);
        when(shipperRepository.findByIsAvailableTrue()).thenReturn(List.of(s1));
        when(shopLocationConfig.getLat()).thenReturn(10.7);
        when(shopLocationConfig.getLng()).thenReturn(106.7);
        when(mapService.calculateExpectedDeliveryTime(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new DeliveryResponseDTO(600, "10m", "soon"));

        service.updateStatus(2L, OrderStatus.SHIPPING);
        assertEquals(OrderStatus.SHIPPING, ord2.getOrderStatus());
        assertNotNull(ord2.getExpectedDeliveryTime());
        assertNotNull(ord2.getShipper());
        assertFalse(ord2.getShipper().getIsAvailable());
        verify(orderRepository, atLeastOnce()).save(ord2);
        verify(notificationService, atLeastOnce()).pushNotification(any());

        // SHIPPING errors: no shippers
        Order ord3 = new Order(); ord3.setOrderStatus(OrderStatus.PROCESSING); ord3.setDestinationLatitude(1.0); ord3.setDestinationLongitude(1.0);
        when(orderRepository.findById(3L)).thenReturn(Optional.of(ord3));
        when(shipperRepository.findByIsAvailableTrue()).thenReturn(Collections.emptyList());
        assertThrows(BadRequestException.class, () -> service.updateStatus(3L, OrderStatus.SHIPPING));

        // SHIPPING errors: missing coords
        Order ord4 = new Order(); ord4.setOrderStatus(OrderStatus.PROCESSING);
        when(orderRepository.findById(4L)).thenReturn(Optional.of(ord4));
        when(shipperRepository.findByIsAvailableTrue()).thenReturn(List.of(new Shipper()));
        assertThrows(BadRequestException.class, () -> service.updateStatus(4L, OrderStatus.SHIPPING));

        // CANCELED: allowed regardless of sequential increment
        Order ord5 = new Order(); ord5.setOrderStatus(OrderStatus.PROCESSING);
        User u5 = new User(); u5.setId(50L); ord5.setUser(u5);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(ord5));
        service.updateStatus(5L, OrderStatus.CANCELED);
        assertEquals(OrderStatus.CANCELED, ord5.getOrderStatus());
    }

    @Test
    @DisplayName("markPaymentCompleted/Failed update payment status and return order id; completed is idempotent")
    void markPayment_cases() {
        Order byTxn = new Order(); byTxn.setId(50L);
        byTxn.setPaymentStatus(PaymentStatus.PENDING);
        User u4 = new User(); u4.setId(40L); byTxn.setUser(u4);
        when(orderRepository.findByTxnRef("abc")).thenReturn(Optional.of(byTxn));

        Long id = service.markPaymentCompleted("abc");
        assertEquals(50L, id);
        assertEquals(PaymentStatus.COMPLETED, byTxn.getPaymentStatus());
        assertEquals(OrderStatus.PENDING, byTxn.getOrderStatus());
        verify(notificationService, atLeastOnce()).pushNotification(any());

        // call again should be idempotent (no change and still returns id)
        reset(notificationService);
        Long id2 = service.markPaymentCompleted("abc");
        assertEquals(50L, id2);
        verify(notificationService, never()).pushNotification(any());

        // Failed case
        Order byTxn2 = new Order(); byTxn2.setId(51L); byTxn2.setPaymentStatus(PaymentStatus.PENDING);
        User u6 = new User(); u6.setId(60L); byTxn2.setUser(u6);
        when(orderRepository.findByTxnRef("def")).thenReturn(Optional.of(byTxn2));
        Long id3 = service.markPaymentFailed("def");
        assertEquals(51L, id3);
        assertEquals(PaymentStatus.FAILED, byTxn2.getPaymentStatus());
        verify(notificationService, atLeastOnce()).pushNotification(any());
    }
}
