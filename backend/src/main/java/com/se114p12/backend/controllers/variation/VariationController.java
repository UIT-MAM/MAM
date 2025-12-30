package com.se114p12.backend.controllers.variation;

import com.se114p12.backend.annotations.ErrorResponse;
import com.se114p12.backend.constants.AppConstant;
import com.se114p12.backend.dtos.variation.VariationRequestDTO;
import com.se114p12.backend.dtos.variation.VariationResponseDTO;
import com.se114p12.backend.services.variation.VariationService;
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

@Tag(name = "Variation Module", description = "APIs for managing variations of products")
@RestController
@RequestMapping(AppConstant.API_BASE_PATH + "/variations")
@RequiredArgsConstructor
public class VariationController {

  private final VariationService variationService;

  @Operation(summary = "Get variations by product ID", description = "Retrieve a paginated list of variations for a specific product, optionally filtered by name")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Successfully retrieved variations",
                  content = @Content(schema = @Schema(implementation = PageVO.class)))
  })
  @ErrorResponse
  @GetMapping
  public ResponseEntity<PageVO<VariationResponseDTO>> getVariationsByProduct(
          @Parameter(description = "ID of the product", required = true)
          @RequestParam Long productId,

          @Parameter(description = "Filter by variation name (optional)")
          @RequestParam(required = false) String name,

          @ParameterObject Pageable pageable
  ) {
    PageVO<VariationResponseDTO> pageVO = variationService.getVariationsByProductId(productId, name, pageable);
    return ResponseEntity.ok(pageVO);
  }

  @Operation(summary = "Create a new variation", description = "Create a new variation for a product")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Variation created successfully",
                  content = @Content(schema = @Schema(implementation = VariationResponseDTO.class)))
  })
  @ErrorResponse
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<VariationResponseDTO> createVariation(
          @Valid @RequestBody VariationRequestDTO dto
  ) {
    return ResponseEntity.ok(variationService.create(dto));
  }

  @Operation(summary = "Update an existing variation", description = "Update variation details by its ID")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Variation updated successfully",
                  content = @Content(schema = @Schema(implementation = VariationResponseDTO.class)))
  })
  @ErrorResponse
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public ResponseEntity<VariationResponseDTO> updateVariation(
          @Parameter(description = "ID of the variation to update", required = true)
          @PathVariable("id") Long id,

          @Valid @RequestBody VariationRequestDTO dto
  ) {
    return ResponseEntity.ok(variationService.update(id, dto));
  }

  @Operation(summary = "Delete a variation", description = "Delete a variation by its ID")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "204", description = "Variation deleted successfully")
  })
  @ErrorResponse
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteVariation(
          @Parameter(description = "ID of the variation to delete", required = true)
          @PathVariable("id") Long id
  ) {
    variationService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
