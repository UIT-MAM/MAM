package com.se114p12.backend.services.cart;

import com.se114p12.backend.dtos.cart.CartResponseDTO;
import com.se114p12.backend.entities.cart.Cart;
import com.se114p12.backend.exceptions.DataConflictException;
import com.se114p12.backend.exceptions.ResourceNotFoundException;
import com.se114p12.backend.mappers.cart.CartMapper;
import com.se114p12.backend.repositories.cart.CartRepository;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
  private final CartRepository cartRepository;
  private final CartMapper cartMapper;

  @Override
  public CartResponseDTO getCartResponseByUserId(Long userId) {
    Cart cart =
        cartRepository
            .findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
    return cartMapper.toCartResponseDTO(cart);
  }

  @Override
  public Cart getCartById(Long id) {
    return cartRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Cart not found with ID: " + id));
  }

  @Override
  public Cart create(Cart cart) {
    if (cart.getUser() != null && cart.getUser().getId() != null &&
            cartRepository.existsByUserId(cart.getUser().getId())) {
      Map<String, String> errorDetails = new HashMap<>();
      errorDetails.put("userId", "User already has a cart");
      throw new DataConflictException(errorDetails);
    }
    return cartRepository.save(cart);
  }

  @Override
  public void delete(Long id) {
    if (!cartRepository.existsById(id)) {
      throw new ResourceNotFoundException("Cart does not exist");
    }
    cartRepository.deleteById(id);
  }

  @Override
  public boolean existsByIdAndUserId(Long id, Long userId) {
    return cartRepository.existsByIdAndUserId(id, userId);
  }

  @Override
  public int countCartItemsByUserId(Long userId) {
    return cartRepository.findByUserId(userId).map(cart -> cart.getCartItems().size()).orElse(0);
  }
}
