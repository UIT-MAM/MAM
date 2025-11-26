package com.se114p12.backend.controllers.order;

import com.se114p12.backend.annotations.ErrorResponse;
import com.se114p12.backend.constants.AppConstant;
import com.se114p12.backend.dtos.order.OrderRequestDTO;
import com.se114p12.backend.dtos.order.OrderResponseDTO;
import com.se114p12.backend.entities.order.Order;
import com.se114p12.backend.enums.OrderStatus;
import com.se114p12.backend.services.order.OrderService;
import com.se114p12.backend.util.JwtUtil;
import com.se114p12.backend.vo.PageVO;
import com.turkraft.springfilter.boot.Filter;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Order Module", description = "APIs for managing orders")
@RestController
@RequiredArgsConstructor
@RequestMapping(AppConstant.API_BASE_PATH + "/orders")
public class OrderController {

  private final OrderService orderService;
  private final JwtUtil jwtUtil;

  @Operation(summary = "Get order by ID", description = "Retrieve an order by its ID")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved order",
            content = @Content(schema = @Schema(implementation = OrderResponseDTO.class)))
      })
  @ErrorResponse
  @GetMapping("/{id}")
  public ResponseEntity<OrderResponseDTO> findById(@PathVariable("id") Long id) {
    return ResponseEntity.ok(orderService.getById(id));
  }

  @Operation(
      summary = "Get current user's orders",
      description = "Retrieve paginated and filtered orders for the current user.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved orders",
            content = @Content(schema = @Schema(implementation = PageVO.class)))
      })
  @ErrorResponse
  @GetMapping("/me")
  public ResponseEntity<PageVO<OrderResponseDTO>> getMyOrders(
      @ParameterObject Pageable pageable,
      @Filter @Parameter(name = "filter") Specification<Order> specification) {
    Long userId = jwtUtil.getCurrentUserId();
    pageable = pageable.isPaged() ? pageable : Pageable.unpaged();
    return ResponseEntity.ok(orderService.getOrdersByUserId(userId, specification, pageable));
  }

  @Operation(
      summary = "Get all orders with filtering and pagination",
      description = "Retrieve a paginated list of orders with optional filters")
  @ApiResponses(
      value = {@ApiResponse(responseCode = "200", description = "Successfully retrieved orders")})
  @ErrorResponse
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<PageVO<OrderResponseDTO>> getAllOrders(
      @ParameterObject Pageable pageable,
      @Filter @Parameter(name = "filter") Specification<Order> specification) {
    pageable = pageable.isPaged() ? pageable : Pageable.unpaged();
    return ResponseEntity.ok(orderService.getAll(specification, pageable));
  }

  @Operation(
      summary = "Create a new order",
      description = "Create a new order from cart or manual input")
  @ApiResponse(
      responseCode = "200",
      description = "Order successfully created",
      content = @Content(schema = @Schema(implementation = OrderResponseDTO.class)))
  @ErrorResponse
  @PostMapping("/create")
  public ResponseEntity<OrderResponseDTO> createOrder(
      @Valid @RequestBody OrderRequestDTO orderRequestDTO) {
    return ResponseEntity.ok(orderService.create(orderRequestDTO));
  }

  @Operation(summary = "Update an order", description = "Update the order data by its ID")
  @ApiResponse(
      responseCode = "200",
      description = "Order successfully updated",
      content = @Content(schema = @Schema(implementation = OrderResponseDTO.class)))
  @ErrorResponse
  @PutMapping("/update/{id}")
  public ResponseEntity<OrderResponseDTO> updateOrder(
      @PathVariable("id") Long id, @Valid @RequestBody OrderRequestDTO orderRequestDTO) {
    return ResponseEntity.ok(orderService.update(id, orderRequestDTO));
  }

  @Operation(summary = "Delete an order", description = "Delete an order by its ID")
  @ApiResponse(responseCode = "204", description = "Order successfully deleted")
  @ErrorResponse
  @DeleteMapping("/delete/{id}")
  public ResponseEntity<Void> deleteOrder(@PathVariable("id") Long id) {
    orderService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Cancel an order", description = "Cancel an order by its ID")
  @ApiResponse(responseCode = "204", description = "Order successfully cancelled")
  @ErrorResponse
  @PostMapping("/cancel/{id}")
  public ResponseEntity<Void> cancelOrder(@PathVariable("id") Long id) {
    orderService.cancelOrder(id);
    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "Mark an order as delivered",
      description = "Mark a specific order as delivered")
  @ApiResponse(responseCode = "200", description = "Order marked as delivered")
  @ErrorResponse
  @PutMapping("/{orderId}/delivered")
  public ResponseEntity<String> markOrderAsDelivered(@PathVariable("orderId") Long orderId) {
    orderService.markOrderAsDelivered(orderId);
    return ResponseEntity.ok("Order marked as delivered.");
  }

  @Operation(
      summary = "Update order status",
      description = "Update the status of an order by its ID")
  @ApiResponse(responseCode = "200", description = "Order status updated")
  @ErrorResponse
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/{orderId}/status")
  public ResponseEntity<Void> updateStatus(
      @PathVariable("orderId") Long orderId, @RequestParam("status") OrderStatus status) {
    orderService.updateStatus(orderId, status);
    return ResponseEntity.noContent().build();
  }
}
