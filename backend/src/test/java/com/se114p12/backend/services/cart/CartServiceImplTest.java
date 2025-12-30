package com.se114p12.backend.services.cart;

import com.se114p12.backend.dtos.cart.CartResponseDTO;
import com.se114p12.backend.entities.cart.Cart;
import com.se114p12.backend.entities.cart.CartItem;
import com.se114p12.backend.entities.user.User;
import com.se114p12.backend.exceptions.DataConflictException;
import com.se114p12.backend.exceptions.ResourceNotFoundException;
import com.se114p12.backend.mappers.cart.CartMapper;
import com.se114p12.backend.repositories.cart.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartServiceImplTest {

    private CartRepository cartRepository;
    private CartMapper cartMapper;
    private CartServiceImpl service;

    @BeforeEach
    void setUp() {
        cartRepository = mock(CartRepository.class);
        cartMapper = mock(CartMapper.class);
        service = new CartServiceImpl(cartRepository, cartMapper);
    }

    @Test
    @DisplayName("getCartResponseByUserId returns DTO when cart exists")
    void getCartResponseByUserId_success() {
        Cart cart = new Cart();
        CartResponseDTO dto = new CartResponseDTO();
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartMapper.toCartResponseDTO(cart)).thenReturn(dto);

        CartResponseDTO result = service.getCartResponseByUserId(1L);

        assertSame(dto, result);
        verify(cartRepository).findByUserId(1L);
        verify(cartMapper).toCartResponseDTO(cart);
    }

    @Test
    @DisplayName("getCartResponseByUserId throws when not found")
    void getCartResponseByUserId_notFound() {
        when(cartRepository.findByUserId(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getCartResponseByUserId(2L));
    }

    @Test
    @DisplayName("getCartById returns cart when exists")
    void getCartById_success() {
        Cart cart = new Cart();
        when(cartRepository.findById(5L)).thenReturn(Optional.of(cart));
        assertSame(cart, service.getCartById(5L));
    }

    @Test
    @DisplayName("getCartById throws when missing")
    void getCartById_notFound() {
        when(cartRepository.findById(6L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getCartById(6L));
    }

    @Test
    @DisplayName("create saves when user has no cart")
    void create_success() {
        User user = new User();
        user.setId(10L);
        Cart cart = new Cart();
        cart.setUser(user);

        when(cartRepository.existsByUserId(10L)).thenReturn(false);
        when(cartRepository.save(cart)).thenReturn(cart);

        Cart saved = service.create(cart);
        assertSame(cart, saved);
        verify(cartRepository).existsByUserId(10L);
        verify(cartRepository).save(cart);
    }

    @Test
    @DisplayName("create throws DataConflictException when user already has cart")
    void create_conflict() {
        User user = new User();
        user.setId(10L);
        Cart cart = new Cart();
        cart.setUser(user);

        when(cartRepository.existsByUserId(10L)).thenReturn(true);
        assertThrows(DataConflictException.class, () -> service.create(cart));
        verify(cartRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete removes when exists")
    void delete_success() {
        when(cartRepository.existsById(99L)).thenReturn(true);
        service.delete(99L);
        verify(cartRepository).deleteById(99L);
    }

    @Test
    @DisplayName("delete throws when not exists")
    void delete_notFound() {
        when(cartRepository.existsById(100L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> service.delete(100L));
        verify(cartRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("existsByIdAndUserId delegates to repository")
    void existsByIdAndUserId() {
        when(cartRepository.existsByIdAndUserId(1L, 2L)).thenReturn(true);
        assertTrue(service.existsByIdAndUserId(1L, 2L));
        verify(cartRepository).existsByIdAndUserId(1L, 2L);
    }

    @Test
    @DisplayName("countCartItemsByUserId returns size when cart exists and 0 when absent")
    void countCartItemsByUserId_cases() {
        Cart cart = new Cart();
        List<CartItem> items = new ArrayList<>();
        items.add(new CartItem());
        items.add(new CartItem());
        cart.setCartItems(items);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.findByUserId(2L)).thenReturn(Optional.empty());

        assertEquals(2, service.countCartItemsByUserId(1L));
        assertEquals(0, service.countCartItemsByUserId(2L));
    }
}
