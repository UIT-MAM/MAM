package com.se114p12.backend.controllers.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.se114p12.backend.constants.AppConstant;
import com.se114p12.backend.dtos.order.OrderRequestDTO;
import com.se114p12.backend.dtos.order.OrderResponseDTO;
import com.se114p12.backend.entities.order.Order;
import com.se114p12.backend.enums.OrderStatus;
import com.se114p12.backend.enums.PaymentMethod;
import com.se114p12.backend.services.order.OrderService;
import com.se114p12.backend.util.JwtUtil;
import com.se114p12.backend.vo.PageVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderControllerTest {

    // Simple argument resolver to supply a null Specification when none is provided in request
    static class SpecificationArgumentResolver implements org.springframework.web.method.support.HandlerMethodArgumentResolver {
        @Override
        public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
            return Specification.class.isAssignableFrom(parameter.getParameterType());
        }

        @Override
        public Object resolveArgument(org.springframework.core.MethodParameter parameter,
                                      org.springframework.web.method.support.ModelAndViewContainer mavContainer,
                                      org.springframework.web.context.request.NativeWebRequest webRequest,
                                      org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
            return null; // let controller receive null, which it passes to service
        }
    }

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private OrderService orderService;
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        orderService = Mockito.mock(OrderService.class);
        jwtUtil = Mockito.mock(JwtUtil.class);

        OrderController controller = new OrderController(orderService, jwtUtil);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new org.springframework.data.web.PageableHandlerMethodArgumentResolver(), new SpecificationArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
        objectMapper = new ObjectMapper();

        when(jwtUtil.getCurrentUserId()).thenReturn(100L);
    }

    private String basePath() {
        return AppConstant.API_BASE_PATH + "/orders";
    }

    @Test
    @DisplayName("GET /orders/{id} returns 200 with order response")
    void findById_returns200() throws Exception {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(1L);
        dto.setUserId(100L);
        dto.setTotalPrice(new BigDecimal("123.45"));

        when(orderService.getById(1L)).thenReturn(dto);

        mockMvc.perform(get(basePath() + "/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.userId", is(100)))
                .andExpect(jsonPath("$.totalPrice", is(123.45)));

        verify(orderService).getById(1L);
    }

    @Test
    @DisplayName("GET /orders/me returns 200 with current user's orders and uses JwtUtil")
    void getMyOrders_returns200() throws Exception {
        PageVO<OrderResponseDTO> pageVO = PageVO.<OrderResponseDTO>builder()
                .page(0)
                .size(0)
                .totalElements(0L)
                .totalPages(0)
                .numberOfElements(0)
                .content(Collections.emptyList())
                .build();

        when(orderService.getOrdersByUserId(eq(100L), any(), any())).thenReturn(pageVO);

        mockMvc.perform(get(basePath() + "/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(0)))
                .andExpect(jsonPath("$.totalElements", is(0)));

        // Capture pageable passed to ensure unpaged when none provided
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(orderService).getOrdersByUserId(eq(100L), isNull(), pageableCaptor.capture());
        Pageable passed = pageableCaptor.getValue();
        // Default Pageable is paged
        org.junit.jupiter.api.Assertions.assertTrue(passed.isPaged());
    }

    @Test
    @DisplayName("GET /orders returns 200 with page body")
    void getAllOrders_returns200() throws Exception {
        PageVO<OrderResponseDTO> pageVO = PageVO.<OrderResponseDTO>builder()
                .page(0)
                .size(0)
                .totalElements(0L)
                .totalPages(0)
                .numberOfElements(0)
                .content(Collections.emptyList())
                .build();
        when(orderService.getAll(any(), any())).thenReturn(pageVO);

        mockMvc.perform(get(basePath()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(0)));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(orderService).getAll(isNull(), pageableCaptor.capture());
        org.junit.jupiter.api.Assertions.assertTrue(pageableCaptor.getValue().isPaged());
    }

    @Test
    @DisplayName("POST /orders/create returns 200 with created order")
    void createOrder_returns200() throws Exception {
        OrderRequestDTO req = new OrderRequestDTO();
        req.setDestinationLatitude(10.0);
        req.setDestinationLongitude(20.0);
        req.setShippingAddress("Addr");
        req.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        OrderResponseDTO created = new OrderResponseDTO();
        created.setId(5L);
        created.setUserId(100L);

        when(orderService.create(any(OrderRequestDTO.class))).thenReturn(created);

        mockMvc.perform(post(basePath() + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.userId", is(100)));

        verify(orderService).create(any(OrderRequestDTO.class));
    }

    @Test
    @DisplayName("PUT /orders/update/{id} returns 200 with updated order")
    void updateOrder_returns200() throws Exception {
        OrderRequestDTO req = new OrderRequestDTO();
        req.setDestinationLatitude(11.0);
        req.setDestinationLongitude(21.0);
        req.setShippingAddress("New Addr");
        req.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        OrderResponseDTO updated = new OrderResponseDTO();
        updated.setId(9L);
        updated.setUserId(100L);

        when(orderService.update(eq(9L), any(OrderRequestDTO.class))).thenReturn(updated);

        mockMvc.perform(put(basePath() + "/update/{id}", 9)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(9)))
                .andExpect(jsonPath("$.userId", is(100)));

        verify(orderService).update(eq(9L), any(OrderRequestDTO.class));
    }

    @Test
    @DisplayName("DELETE /orders/delete/{id} returns 204 and calls service.delete")
    void deleteOrder_returns204() throws Exception {
        doNothing().when(orderService).delete(7L);

        mockMvc.perform(delete(basePath() + "/delete/{id}", 7))
                .andExpect(status().isNoContent());

        verify(orderService).delete(7L);
    }

    @Test
    @DisplayName("POST /orders/cancel/{id} returns 204 and calls service.cancelOrder")
    void cancelOrder_returns204() throws Exception {
        doNothing().when(orderService).cancelOrder(8L);

        mockMvc.perform(post(basePath() + "/cancel/{id}", 8))
                .andExpect(status().isNoContent());

        verify(orderService).cancelOrder(8L);
    }

    @Test
    @DisplayName("PUT /orders/{orderId}/delivered returns 200 with message and calls service.markOrderAsDelivered")
    void markDelivered_returns200() throws Exception {
        doNothing().when(orderService).markOrderAsDelivered(12L);

        mockMvc.perform(put(basePath() + "/{orderId}/delivered", 12))
                .andExpect(status().isOk())
                .andExpect(content().string("\"Order marked as delivered.\""));

        verify(orderService).markOrderAsDelivered(12L);
    }

    @Test
    @DisplayName("GET /orders/{orderId}/status?status=SHIPPING returns 204 and calls updateStatus")
    void updateStatus_returns204() throws Exception {
        doNothing().when(orderService).updateStatus(22L, OrderStatus.SHIPPING);

        mockMvc.perform(get(basePath() + "/{orderId}/status", 22)
                        .param("status", OrderStatus.SHIPPING.name()))
                .andExpect(status().isNoContent());

        verify(orderService).updateStatus(22L, OrderStatus.SHIPPING);
    }
}
