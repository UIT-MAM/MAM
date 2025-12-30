package com.se114p12.backend.services.cart;

import com.se114p12.backend.dtos.cart.CartItemRequestDTO;
import com.se114p12.backend.dtos.cart.CartItemResponseDTO;
import com.se114p12.backend.entities.cart.Cart;
import com.se114p12.backend.entities.cart.CartItem;
import com.se114p12.backend.entities.product.Product;
import com.se114p12.backend.entities.user.User;
import com.se114p12.backend.entities.variation.Variation;
import com.se114p12.backend.entities.variation.VariationOption;
import com.se114p12.backend.exceptions.ResourceNotFoundException;
import com.se114p12.backend.mappers.cart.CartItemMapper;
import com.se114p12.backend.repositories.authentication.UserRepository;
import com.se114p12.backend.repositories.cart.CartItemRepository;
import com.se114p12.backend.repositories.cart.CartRepository;
import com.se114p12.backend.repositories.product.ProductRepository;
import com.se114p12.backend.repositories.variation.VariationOptionRepository;
import com.se114p12.backend.util.JwtUtil;
import com.se114p12.backend.vo.PageVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CartItemServiceImplTest {

    private JwtUtil jwtUtil;
    private UserRepository userRepository;
    private CartService cartService;
    private CartItemRepository cartItemRepository;
    private CartItemMapper cartItemMapper;
    private CartRepository cartRepository;
    private ProductRepository productRepository;
    private VariationOptionRepository variationOptionRepository;

    private CartItemServiceImpl service;

    @BeforeEach
    void setUp() {
        jwtUtil = mock(JwtUtil.class);
        userRepository = mock(UserRepository.class);
        cartService = mock(CartService.class);
        cartItemRepository = mock(CartItemRepository.class);
        cartItemMapper = mock(CartItemMapper.class);
        cartRepository = mock(CartRepository.class);
        productRepository = mock(ProductRepository.class);
        variationOptionRepository = mock(VariationOptionRepository.class);

        service = new CartItemServiceImpl(jwtUtil, userRepository, cartService, cartItemRepository, cartItemMapper, cartRepository, productRepository, variationOptionRepository);
    }

    @Test
    @DisplayName("getAllCartItems maps page to PageVO")
    void getAllCartItems_mapsPage() {
        Pageable pageable = PageRequest.of(0, 2);
        CartItem item1 = new CartItem();
        CartItem item2 = new CartItem();
        Page<CartItem> page = new PageImpl<>(List.of(item1, item2), pageable, 2);

        when(cartItemRepository.findAll(pageable)).thenReturn(page);
        when(cartItemMapper.toDTO(item1)).thenReturn(new CartItemResponseDTO());
        when(cartItemMapper.toDTO(item2)).thenReturn(new CartItemResponseDTO());

        PageVO<CartItemResponseDTO> vo = service.getAllCartItems(pageable);
        assertEquals(2, vo.getContent().size());
        assertEquals(0, vo.getPage());
        assertEquals(2, vo.getSize());
        assertEquals(2, vo.getTotalElements());
        assertEquals(1, vo.getTotalPages());
        assertEquals(2, vo.getNumberOfElements());
    }

    @Test
    @DisplayName("createCartItem creates new cart if missing and calculates price")
    void createCartItem_createsCartAndCalculatesPrice() {
        when(jwtUtil.getCurrentUserId()).thenReturn(100L);
        User user = new User();
        user.setId(100L);
        user.setCart(null);

        when(userRepository.findById(100L)).thenReturn(Optional.of(user));

        // simulate mapper creating entity with product and variation options
        CartItemRequestDTO dto = new CartItemRequestDTO();
        dto.setProductId(1L);
        dto.setVariationOptionIds(new HashSet<>(Arrays.asList(11L, 12L)));

        Product product = new Product();
        product.setId(1L);
        product.setOriginalPrice(new BigDecimal("100.00"));

        VariationOption vo1 = new VariationOption();
        vo1.setId(11L);
        Variation v = new Variation(); v.setId(1L); vo1.setVariation(v);
        vo1.setAdditionalPrice(10.0);
        VariationOption vo2 = new VariationOption();
        vo2.setId(12L);
        vo2.setVariation(v);
        vo2.setAdditionalPrice(5.5);
        Set<VariationOption> vos = new HashSet<>(Arrays.asList(vo1, vo2));

        CartItem mapped = new CartItem();
        mapped.setProduct(product);
        mapped.setVariationOptions(vos);
        mapped.setQuantity(1L);

        when(cartItemMapper.toEntity(eq(dto), any(), any())).thenReturn(mapped);

        Cart createdCart = new Cart();
        createdCart.setId(200L);
        createdCart.setUser(user);
        when(cartService.create(any(Cart.class))).thenReturn(createdCart);

        CartItem saved = new CartItem();
        saved.setId(999L);
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(saved);

        CartItemResponseDTO responseDTO = new CartItemResponseDTO();
        responseDTO.setId(999L);
        when(cartItemMapper.toDTO(saved)).thenReturn(responseDTO);

        CartItemResponseDTO result = service.createCartItem(dto);

        // price should be 100 + 10 + 5.5 = 115.5
        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(captor.capture());
        CartItem toSave = captor.getValue();
        assertEquals(0, toSave.getPrice().compareTo(new BigDecimal("115.5")));
        assertEquals(createdCart, toSave.getCart());
        assertEquals(999L, result.getId());
    }

    @Test
    @DisplayName("createCartItem merges quantity when identical variations exist")
    void createCartItem_mergesExisting() {
        when(jwtUtil.getCurrentUserId()).thenReturn(101L);
        User user = new User();
        user.setId(101L);
        Cart userCart = new Cart();
        userCart.setId(300L);
        user.setCart(userCart);
        when(userRepository.findById(101L)).thenReturn(Optional.of(user));

        CartItemRequestDTO dto = new CartItemRequestDTO();
        Product product = new Product();
        product.setId(1L);
        product.setOriginalPrice(new BigDecimal("50"));

        VariationOption vo = new VariationOption();
        vo.setId(7L);
        Variation var = new Variation(); var.setId(1L); vo.setVariation(var);
        vo.setAdditionalPrice(2.0);
        Set<VariationOption> set = new HashSet<>(List.of(vo));

        CartItem mapped = new CartItem();
        mapped.setProduct(product);
        mapped.setVariationOptions(set);
        mapped.setQuantity(2L);

        when(cartItemMapper.toEntity(eq(dto), any(), any())).thenReturn(mapped);

        CartItem existing = new CartItem();
        existing.setId(10L);
        existing.setCart(userCart);
        existing.setProduct(product);
        existing.setVariationOptions(new HashSet<>(set));
        existing.setQuantity(3L);
        when(cartItemRepository.findAllByCartIdAndProductId(300L, 1L)).thenReturn(List.of(existing));

        CartItem afterSave = new CartItem();
        afterSave.setId(10L);
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cartItemMapper.toDTO(any(CartItem.class))).thenReturn(new CartItemResponseDTO());

        CartItemResponseDTO resp = service.createCartItem(dto);
        assertNotNull(resp);
        // verify quantity merged to 5
        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(captor.capture());
        assertEquals(5L, captor.getValue().getQuantity());
    }

    @Test
    @DisplayName("updateCartItem recalculates price and saves; throws when not found")
    void updateCartItem_cases() {
        // not found case
        when(cartItemRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.updateCartItem(404L, new CartItemRequestDTO()));

        // success case
        CartItem existing = new CartItem();
        existing.setId(5L);
        Cart cart = new Cart(); cart.setId(9L);
        existing.setCart(cart);
        when(cartItemRepository.findById(5L)).thenReturn(Optional.of(existing));

        CartItemRequestDTO dto = new CartItemRequestDTO();
        Product product = new Product(); product.setId(2L); product.setOriginalPrice(new BigDecimal("80"));
        VariationOption vo = new VariationOption(); vo.setId(8L); vo.setAdditionalPrice(3.0); Variation var = new Variation(); var.setId(1L); vo.setVariation(var);
        Set<VariationOption> vset = new HashSet<>(List.of(vo));

        CartItem mapped = new CartItem();
        mapped.setProduct(product);
        mapped.setVariationOptions(vset);
        mapped.setQuantity(1L);
        when(cartItemMapper.toEntity(eq(dto), any(), any())).thenReturn(mapped);

        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cartItemMapper.toDTO(any(CartItem.class))).thenReturn(new CartItemResponseDTO());

        CartItemResponseDTO res = service.updateCartItem(5L, dto);
        assertNotNull(res);
        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(captor.capture());
        assertEquals(0, captor.getValue().getPrice().compareTo(new BigDecimal("83")));
        assertEquals(5L, captor.getValue().getId());
        assertEquals(9L, captor.getValue().getCart().getId());
    }

    @Test
    @DisplayName("deleteCartItem delegates to repository")
    void deleteCartItem_delegates() {
        service.deleteCartItem(77L);
        verify(cartItemRepository).deleteById(77L);
    }
}
