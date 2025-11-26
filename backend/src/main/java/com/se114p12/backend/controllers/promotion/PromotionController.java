package com.se114p12.backend.controllers.promotion;

import com.se114p12.backend.annotations.ErrorResponse;
import com.se114p12.backend.constants.AppConstant;
import com.se114p12.backend.dtos.promotion.PromotionRequestDTO;
import com.se114p12.backend.dtos.promotion.PromotionResponseDTO;
import com.se114p12.backend.entities.promotion.Promotion;
import com.se114p12.backend.services.promotion.PromotionService;
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

@Tag(name = "Promotion Module", description = "Endpoints for managing promotions")
@RestController
@RequestMapping(AppConstant.API_BASE_PATH + "/promotions")
@RequiredArgsConstructor
public class PromotionController {

  private final PromotionService promotionService;

  @Operation(
      summary = "Get all promotions",
      description = "Retrieve a paginated list of promotions with optional filtering")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of promotions retrieved successfully",
            content = @Content(schema = @Schema(implementation = PageVO.class)))
      })
  @ErrorResponse
  @GetMapping
  public ResponseEntity<PageVO<PromotionResponseDTO>> getAllPromotions(
      @ParameterObject Pageable pageable,
      @Filter @Parameter(name = "filter") Specification<Promotion> specification) {
    pageable = pageable.isPaged() ? pageable : Pageable.unpaged();
    return ResponseEntity.ok(promotionService.getAll(specification, pageable));
  }

  @Operation(summary = "Get promotion by ID", description = "Retrieve a promotion by its ID")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Promotion retrieved successfully",
            content = @Content(schema = @Schema(implementation = PromotionResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Promotion not found", content = @Content)
      })
  @ErrorResponse
  @GetMapping("/{id}")
  public ResponseEntity<PromotionResponseDTO> getPromotionById(@PathVariable("id") Long id) {
    return ResponseEntity.ok(promotionService.findById(id));
  }

  @Operation(summary = "Create a new promotion", description = "Only accessible by admin")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Promotion created successfully",
            content = @Content(schema = @Schema(implementation = PromotionResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content)
      })
  @ErrorResponse
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<PromotionResponseDTO> createPromotion(
      @Valid @RequestBody PromotionRequestDTO dto) {
    return ResponseEntity.ok(promotionService.create(dto));
  }

  @Operation(summary = "Update a promotion", description = "Only accessible by admin")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Promotion updated successfully",
            content = @Content(schema = @Schema(implementation = PromotionResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Promotion not found", content = @Content)
      })
  @ErrorResponse
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public ResponseEntity<PromotionResponseDTO> updatePromotion(
      @PathVariable("id") Long id, @Valid @RequestBody PromotionRequestDTO dto) {
    return ResponseEntity.ok(promotionService.update(id, dto));
  }

  @Operation(summary = "Delete a promotion", description = "Only accessible by admin")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Promotion deleted successfully",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Promotion not found", content = @Content)
      })
  @ErrorResponse
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletePromotion(@PathVariable("id") Long id) {
    promotionService.delete(id);
    return ResponseEntity.ok().build();
  }
}
