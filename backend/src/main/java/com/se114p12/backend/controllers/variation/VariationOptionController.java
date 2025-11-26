package com.se114p12.backend.controllers.variation;

import com.se114p12.backend.annotations.ErrorResponse;
import com.se114p12.backend.constants.AppConstant;
import com.se114p12.backend.dtos.variation.VariationOptionRequestDTO;
import com.se114p12.backend.dtos.variation.VariationOptionResponseDTO;
import com.se114p12.backend.services.variation.VariationOptionService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Variation Option Module", description = "APIs for managing variation options")
@RestController
@RequestMapping(AppConstant.API_BASE_PATH + "/variation-options")
@RequiredArgsConstructor
public class VariationOptionController {

  private final VariationOptionService variationOptionService;

  @Operation(
      summary = "Get variation options by variation ID",
      description = "Retrieve all variation options associated with a specific variation")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved variation options",
            content = @Content(schema = @Schema(implementation = PageVO.class)))
      })
  @ErrorResponse
  @GetMapping
  public ResponseEntity<PageVO<VariationOptionResponseDTO>> getByVariationId(
      @Parameter(description = "ID of the variation", required = true) @RequestParam
          Long variationId,
      @ParameterObject Pageable pageable) {
    pageable = pageable.isPaged() ? pageable : Pageable.unpaged();
    return ResponseEntity.ok(variationOptionService.getByVariationId(variationId, pageable));
  }

  @Operation(
      summary = "Create a variation option",
      description = "Create a new variation option (Admin only)")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Variation option created successfully",
            content = @Content(schema = @Schema(implementation = VariationOptionResponseDTO.class)))
      })
  @ErrorResponse
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<VariationOptionResponseDTO> create(
      @Valid @RequestBody VariationOptionRequestDTO dto) {
    return ResponseEntity.ok(variationOptionService.create(dto));
  }

  @Operation(
      summary = "Update a variation option",
      description = "Update an existing variation option by its ID (Admin only)")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Variation option updated successfully",
            content = @Content(schema = @Schema(implementation = VariationOptionResponseDTO.class)))
      })
  @ErrorResponse
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public ResponseEntity<VariationOptionResponseDTO> update(
      @Parameter(description = "ID of the variation option to update") @PathVariable("id") Long id,
      @Valid @RequestBody VariationOptionRequestDTO dto) {
    return ResponseEntity.ok(variationOptionService.update(id, dto));
  }

  @Operation(
      summary = "Delete a variation option",
      description = "Delete a variation option by its ID (Admin only)")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Variation option deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Variation option not found")
      })
  @ErrorResponse
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
      @Parameter(description = "ID of the variation option to delete") @PathVariable("id") Long id) {
    variationOptionService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
