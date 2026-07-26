package com.mzansiconnect.backend.dto.support;

import com.mzansiconnect.backend.dto.auth.UserResponse;
import com.mzansiconnect.backend.enums.SupportIssueType;
import com.mzansiconnect.backend.enums.SupportTicketPriority;
import com.mzansiconnect.backend.enums.SupportTicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicketResponse {

    private Long id;
    private UserResponse user;
    private SupportIssueType issueType;
    private String subject;
    private String description;
    private SupportTicketStatus status;
    private SupportTicketPriority priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
}