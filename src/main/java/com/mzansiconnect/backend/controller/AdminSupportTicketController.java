package com.mzansiconnect.backend.controller;

import com.mzansiconnect.backend.dto.support.SupportTicketPriorityUpdateRequest;
import com.mzansiconnect.backend.dto.support.SupportTicketResponse;
import com.mzansiconnect.backend.dto.support.SupportTicketStatusUpdateRequest;
import com.mzansiconnect.backend.response.ApiResponse;
import com.mzansiconnect.backend.service.SupportTicketService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/support-tickets")
@RequiredArgsConstructor
@Validated
public class AdminSupportTicketController {

    private final SupportTicketService supportTicketService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SupportTicketResponse>>>
    getAllTickets() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Support tickets retrieved successfully",
                        supportTicketService.getAllTicketsForAdmin()
                )
        );
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<SupportTicketResponse>> updateStatus(
            @PathVariable
            @Positive(message = "Support ticket ID must be positive")
            Long id,

            @Valid
            @RequestBody
            SupportTicketStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Support ticket status updated successfully",
                        supportTicketService.updateStatus(
                                id,
                                request.getStatus()
                        )
                )
        );
    }

    @PutMapping("/{id}/priority")
    public ResponseEntity<ApiResponse<SupportTicketResponse>> updatePriority(
            @PathVariable
            @Positive(message = "Support ticket ID must be positive")
            Long id,

            @Valid
            @RequestBody
            SupportTicketPriorityUpdateRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Support ticket priority updated successfully",
                        supportTicketService.updatePriority(
                                id,
                                request.getPriority()
                        )
                )
        );
    }
}