package com.mzansiconnect.backend.dto.support;

import com.mzansiconnect.backend.enums.SupportTicketPriority;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupportTicketPriorityUpdateRequest {

    @NotNull(message = "Support ticket priority is required")
    private SupportTicketPriority priority;
}