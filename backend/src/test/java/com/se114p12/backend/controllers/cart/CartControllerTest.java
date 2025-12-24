package com.se114p12.backend.controllers.cart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.se114p12.backend.constants.AppConstant;
import com.se114p12.backend.dtos.cart.CartResponseDTO;
import com.se114p12.backend.entities.cart.Cart;
import com.se114p12.backend.mappers.cart.CartMapper;
import com.se114p12.backend.repositories.cart.CartRepository;
import com.se114p12.backend.services.cart.CartService;
import com.se114p12.backend.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CartControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private CartRepository cartRepository;
    private CartService cartService;
    private CartMapper cartMapper;
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        cartRepository = Mockito.mock(CartRepository.class);
        cartService = Mockito.mock(CartService.class);
        cartMapper = Mockito.mock(CartMapper.class);
        jwtUtil = Mockito.mock(JwtUtil.class);

        CartController controller = new CartController(cartRepository, cartService, cartMapper, jwtUtil);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
        objectMapper = new ObjectMapper();

        when(jwtUtil.getCurrentUserId()).thenReturn(100L);
    }

    private String basePath() {
        return AppConstant.API_BASE_PATH + "/carts";
    }

    @Test
    @DisplayName("GET /carts/me returns 200 with cart response of current user")
    void getMyCart_returns200() throws Exception {
        CartResponseDTO dto = new CartResponseDTO();
        dto.setId(1L);
        dto.setUserId(100L);

        when(cartService.getCartResponseByUserId(100L)).thenReturn(dto);

        mockMvc.perform(get(basePath() + "/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.userId", is(100)));
    }

    @Test
    @DisplayName("GET /carts/{id} returns 404 when cart does not exist")
    void getCartById_returns404_whenNotExists() throws Exception {
        when(cartRepository.existsById(999L)).thenReturn(false);

        mockMvc.perform(get(basePath() + "/{id}", 999))
                .andExpect(status().isNotFound());

        verify(cartRepository).existsById(999L);
        verifyNoInteractions(cartService, cartMapper);
    }

    @Test
    @DisplayName("GET /carts/{id} returns 200 when exists with mapped DTO")
    void getCartById_returns200_whenExists() throws Exception {
        when(cartRepository.existsById(1L)).thenReturn(true);
        Cart cart = new Cart();
        CartResponseDTO dto = new CartResponseDTO();
        dto.setId(1L);
        dto.setUserId(200L);

        when(cartService.getCartById(1L)).thenReturn(cart);
        when(cartMapper.toCartResponseDTO(cart)).thenReturn(dto);

        mockMvc.perform(get(basePath() + "/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.userId", is(200)));
    }

    @Test
    @DisplayName("POST /carts returns 200 with created cart body")
    void createNewCart_returns200() throws Exception {
        Cart created = new Cart();
        created.setId(77L);

        when(cartService.create(any(Cart.class))).thenReturn(created);

        mockMvc.perform(post(basePath())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(77)));
    }

    @Test
    @DisplayName("DELETE /carts/{id} returns 404 when cart is not found or not belongs to user")
    void deleteCart_returns404_whenNotBelongs() throws Exception {
        when(cartService.existsByIdAndUserId(10L, 100L)).thenReturn(false);

        mockMvc.perform(delete(basePath() + "/{id}", 10))
                .andExpect(status().isNotFound());

        verify(cartService, times(1)).existsByIdAndUserId(10L, 100L);
        verify(cartService, never()).delete(anyLong());
    }

    @Test
    @DisplayName("DELETE /carts/{id} returns 204 when deleted")
    void deleteCart_returns204_whenDeleted() throws Exception {
        when(cartService.existsByIdAndUserId(11L, 100L)).thenReturn(true);
        doNothing().when(cartService).delete(11L);

        mockMvc.perform(delete(basePath() + "/{id}", 11))
                .andExpect(status().isNoContent());

        verify(cartService).existsByIdAndUserId(11L, 100L);
        verify(cartService).delete(11L);
    }

    @Test
    @DisplayName("GET /carts/me/count returns 200 with count number")
    void countMyCartItems_returns200() throws Exception {
        when(cartService.countCartItemsByUserId(100L)).thenReturn(5);

        mockMvc.perform(get(basePath() + "/me/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }
}
