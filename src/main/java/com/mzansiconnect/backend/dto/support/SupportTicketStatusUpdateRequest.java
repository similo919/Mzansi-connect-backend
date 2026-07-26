package com.mzansiconnect.backend.dto.support;

import com.mzansiconnect.backend.enums.SupportTicketStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupportTicketStatusUpdateRequest {

    @NotNull(message = "Support ticket status is required")
    private SupportTicketStatus status;
}