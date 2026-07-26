package com.mzansiconnect.backend.controller;

import com.mzansiconnect.backend.dto.support.SupportTicketCreateRequest;
import com.mzansiconnect.backend.dto.support.SupportTicketResponse;
import com.mzansiconnect.backend.response.ApiResponse;
import com.mzansiconnect.backend.service.SupportTicketService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/support-tickets")
@RequiredArgsConstructor
@Validated
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    @PostMapping
    public ResponseEntity<ApiResponse<SupportTicketResponse>> createTicket(
            @Valid
            @RequestBody
            SupportTicketCreateRequest request,

            Authentication authentication
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Support ticket created successfully",
                                supportTicketService.createTicket(
                                        authentication.getName(),
                                        request
                                )
                        )
                );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<SupportTicketResponse>>>
    getCurrentUserTickets(Authentication authentication) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Support tickets retrieved successfully",
                        supportTicketService.getCurrentUserTickets(
                                authentication.getName()
                        )
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SupportTicketResponse>>
    getCurrentUserTicket(
            @PathVariable
            @Positive(message = "Support ticket ID must be positive")
            Long id,

            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Support ticket retrieved successfully",
                        supportTicketService.getCurrentUserTicket(
                                authentication.getName(),
                                id
                        )
                )
        );
    }
}