package com.se114p12.backend.controllers.notification;

import com.se114p12.backend.annotations.ErrorResponse;
import com.se114p12.backend.constants.AppConstant;
import com.se114p12.backend.dtos.nofitication.NotificationRequestDTO;
import com.se114p12.backend.dtos.nofitication.NotificationResponseDTO;
import com.se114p12.backend.entities.notification.Notification;
import com.se114p12.backend.services.notification.NotificationService;
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
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notification Module", description = "APIs for managing notifications")
@RestController
@RequiredArgsConstructor
@RequestMapping(AppConstant.API_BASE_PATH + "/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "Send notification to all users", description = "Send a notification to all users with role USER")
    @ApiResponse(responseCode = "200", description = "Notification sent to all")
    @PostMapping("/send-to-all")
    public ResponseEntity<NotificationResponseDTO> sendToAll(@RequestBody NotificationRequestDTO request) {
        return ResponseEntity.ok(notificationService.sendToAll(request));
    }

    @Operation(summary = "Send a notification to users", description = "Send a custom notification to multiple users")
    @ApiResponse(responseCode = "200", description = "Notification sent", content = @Content(schema = @Schema(implementation = NotificationResponseDTO.class)))
    @PostMapping("/send")
    public ResponseEntity<NotificationResponseDTO> send(@RequestBody NotificationRequestDTO request) {
        return ResponseEntity.ok(notificationService.pushNotification(request));
    }

    @Operation(summary = "Get all notifications (Admin)", description = "Admin only: Retrieve all notifications with filter")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fetched all notifications", content = @Content(schema = @Schema(implementation = PageVO.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<PageVO<NotificationResponseDTO>> getAllNotifications(
            @ParameterObject Pageable pageable,
            @Filter @Parameter(name = "filter") Specification<Notification> specification
    ) {
        pageable = pageable.isPaged() ? pageable : Pageable.unpaged();
        return ResponseEntity.ok(notificationService.getAll(specification, pageable));
    }

    @Operation(summary = "Get my notifications", description = "Get paginated and filtered notifications for the current user")
    @ApiResponse(responseCode = "200", description = "Fetched user notifications", content = @Content(schema = @Schema(implementation = PageVO.class)))
    @GetMapping("/me")
    public ResponseEntity<PageVO<NotificationResponseDTO>> getMyNotifications(
            @ParameterObject Pageable pageable,
            @Filter @Parameter(name = "filter") Specification<Notification> specification
    ) {
        pageable = pageable.isPaged() ? pageable : Pageable.unpaged();
        Long userId = jwtUtil.getCurrentUserId();
        return ResponseEntity.ok(notificationService.getNotificationsByUserId(userId, specification, pageable));
    }

    @Operation(summary = "Mark all my notifications as read", description = "Set all current user's notifications as read")
    @ApiResponse(responseCode = "200", description = "Marked all as read")
    @ErrorResponse
    @PutMapping("/me/read-all")
    public ResponseEntity<Void> markAllMyNotificationsAsRead() {
        Long userId = jwtUtil.getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Mark a notification as read", description = "Mark a specific notification as read by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Marked as read"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @ErrorResponse
    @PutMapping("/read/{id}")
    public ResponseEntity<Void> markAsRead(
            @Parameter(description = "Notification ID to mark as read") @PathVariable("id") Long id) {
        Long userId = jwtUtil.getCurrentUserId();
        notificationService.markAsRead(userId, id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete a notification", description = "Delete a notification by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @ErrorResponse
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Notification ID to delete") @PathVariable("id") Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Count unread notifications", description = "Get number of unread notifications for current user")
    @ApiResponse(responseCode = "200", description = "Returned unread count", content = @Content(schema = @Schema(implementation = Long.class)))
    @ErrorResponse
    @GetMapping("/me/unread-count")
    public ResponseEntity<Long> countMyUnreadNotifications() {
        Long userId = jwtUtil.getCurrentUserId();
        return ResponseEntity.ok(notificationService.countUnreadByUserId(userId));
    }
}
