package com.se114p12.backend.controllers.cart;

import com.se114p12.backend.annotations.ErrorResponse;
import com.se114p12.backend.constants.AppConstant;
import com.se114p12.backend.dtos.cart.CartItemRequestDTO;
import com.se114p12.backend.dtos.cart.CartItemResponseDTO;
import com.se114p12.backend.services.cart.CartItemService;
import com.se114p12.backend.vo.PageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cart Item Module", description = "APIs for managing cart items")
@RestController
@RequestMapping(AppConstant.API_BASE_PATH + "/cart-items")
@RequiredArgsConstructor
public class CartItemController {

    private final CartItemService cartItemService;

    @Operation(
            summary = "Get all cart items",
            description = "Retrieve all cart items with optional pagination"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved cart items",
            content = @Content(schema = @Schema(implementation = PageVO.class))
    )
    @ErrorResponse
    @GetMapping
    public ResponseEntity<PageVO<CartItemResponseDTO>> getAllCartItems(
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(cartItemService.getAllCartItems(pageable));
    }

    @Operation(
            summary = "Create a cart item",
            description = "Add a new item to the cart"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully created cart item",
            content = @Content(schema = @Schema(implementation = CartItemResponseDTO.class))
    )
    @ErrorResponse
    @PostMapping
    public ResponseEntity<CartItemResponseDTO> createCartItem(
            @Parameter(description = "Cart item data")
            @Valid @RequestBody CartItemRequestDTO dto) {
        return ResponseEntity.ok(cartItemService.createCartItem(dto));
    }

    @Operation(
            summary = "Update a cart item",
            description = "Update an existing cart item by ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated cart item", content = @Content(schema = @Schema(implementation = CartItemResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    @ErrorResponse
    @PutMapping("/{id}")
    public ResponseEntity<CartItemResponseDTO> updateCartItem(
            @Parameter(description = "ID of the cart item to update") @PathVariable("id") Long id,
            @Valid @RequestBody CartItemRequestDTO dto) {
        return ResponseEntity.ok(cartItemService.updateCartItem(id, dto));
    }

    @Operation(
            summary = "Delete a cart item",
            description = "Remove a cart item by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted cart item"),
            @ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    @ErrorResponse
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCartItem(
            @Parameter(description = "ID of the cart item to delete") @PathVariable("id") Long id) {
        cartItemService.deleteCartItem(id);
        return ResponseEntity.noContent().build();
    }
}
