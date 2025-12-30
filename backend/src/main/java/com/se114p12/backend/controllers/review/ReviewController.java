package com.se114p12.backend.controllers.review;

import com.se114p12.backend.annotations.ErrorResponse;
import com.se114p12.backend.constants.AppConstant;
import com.se114p12.backend.dtos.review.ReviewRequestDTO;
import com.se114p12.backend.dtos.review.ReviewResponseDTO;
import com.se114p12.backend.services.review.ReviewService;
import com.se114p12.backend.vo.PageVO;
import io.swagger.v3.oas.annotations.Operation;
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

@Tag(name = "Review Module", description = "APIs for managing reviews")
@RestController
@RequestMapping(AppConstant.API_BASE_PATH + "/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Get all reviews", description = "Return the list of reviews")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get all reviews successfully"),
            @ApiResponse(responseCode = "404", description = "Reviews not found")
    })
    @ErrorResponse
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<PageVO<ReviewResponseDTO>> getAllReviews(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(reviewService.getReviews(pageable));
    }

    @Operation(summary = "Get the review of the order", description = "Return the list of reviews related to the order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get review of order successfully"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @ErrorResponse
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ReviewResponseDTO> getReviewsByOrder(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(reviewService.getReviewsByOrder(orderId));
    }

    @Operation(summary = "Create a reply to the review", description = "Add new content to the reply")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reply successfully"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @ErrorResponse
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{reviewId}/reply")
    public ResponseEntity<ReviewResponseDTO> replyToReview(
            @PathVariable("reviewId") Long reviewId,
            @RequestParam String reply
    ) {
        return ResponseEntity.ok(reviewService.replyToReview(reviewId, reply));
    }

    @Operation(summary = "Create a new review", description = "Create a new review for the completed order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Create successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @ErrorResponse
    @PostMapping
    public ResponseEntity<ReviewResponseDTO> createReview(@Valid @RequestBody ReviewRequestDTO request) {
        return ResponseEntity.ok(reviewService.createReview(request));
    }

    @Operation(summary = "Update reviews")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Update successfully"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @ErrorResponse
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDTO> updateReview(
            @PathVariable("reviewId") Long reviewId,
            @Valid @RequestBody ReviewRequestDTO request
    ) {
        return ResponseEntity.ok(reviewService.updateReview(reviewId, request));
    }

    @Operation(summary = "Delete review", description = "Delete a review by the id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Delete review successfully"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @ErrorResponse
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable("reviewId") Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}
