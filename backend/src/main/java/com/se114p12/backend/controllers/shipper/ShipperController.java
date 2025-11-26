package com.se114p12.backend.controllers.shipper;

import com.se114p12.backend.annotations.ErrorResponse;
import com.se114p12.backend.constants.AppConstant;
import com.se114p12.backend.dtos.shipper.ShipperRequest;
import com.se114p12.backend.dtos.shipper.ShipperResponse;
import com.se114p12.backend.entities.shipper.Shipper;
import com.se114p12.backend.services.shipper.ShipperService;
import com.se114p12.backend.vo.PageVO;
import com.turkraft.springfilter.boot.Filter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Shipper Module", description = "APIs for managing shippers (admin only)")
@RestController
@RequestMapping(AppConstant.API_BASE_PATH + "/shippers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ShipperController {

  private final ShipperService shipperService;

  @Operation(summary = "Create a new shipper")
  @ApiResponse(
      responseCode = "200",
      description = "Successfully created",
      content = @Content(schema = @Schema(implementation = ShipperResponse.class)))
  @ErrorResponse
  @PostMapping
  public ResponseEntity<ShipperResponse> create(@RequestBody ShipperRequest request) {
    return ResponseEntity.ok(shipperService.create(request));
  }

  @Operation(summary = "Get all shippers", description = "Supports pagination and filtering")
  @ApiResponse(
      responseCode = "200",
      description = "List of shippers",
      content = @Content(schema = @Schema(implementation = PageVO.class)))
  @ErrorResponse
  @GetMapping
  public ResponseEntity<PageVO<ShipperResponse>> getAll(
      @ParameterObject Pageable pageable,
      @Filter @Parameter(name = "filter") Specification<Shipper> specification) {
    pageable = pageable.isPaged() ? pageable : Pageable.unpaged();
    return ResponseEntity.ok(shipperService.getAll(specification, pageable));
  }

  @Operation(summary = "Get shipper by ID")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Found the shipper",
            content = @Content(schema = @Schema(implementation = ShipperResponse.class))),
        @ApiResponse(responseCode = "404", description = "Shipper not found")
      })
  @ErrorResponse
  @GetMapping("/{id}")
  public ResponseEntity<ShipperResponse> getById(
      @Parameter(description = "Shipper ID") @PathVariable("id") Long id) {
    return ResponseEntity.ok(shipperService.getById(id));
  }

  @Operation(summary = "Update a shipper by ID")
  @ApiResponse(
      responseCode = "200",
      description = "Updated successfully",
      content = @Content(schema = @Schema(implementation = ShipperResponse.class)))
  @ErrorResponse
  @PutMapping("/{id}")
  public ResponseEntity<ShipperResponse> update(
      @Parameter(description = "Shipper ID") @PathVariable("id") Long id,
      @RequestBody ShipperRequest request) {
    return ResponseEntity.ok(shipperService.update(id, request));
  }

  @Operation(summary = "Delete a shipper by ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Shipper not found")
      })
  @ErrorResponse
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@Parameter(description = "Shipper ID") @PathVariable("id") Long id) {
    shipperService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
