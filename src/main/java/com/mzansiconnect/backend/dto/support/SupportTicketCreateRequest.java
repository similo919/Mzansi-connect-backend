package com.mzansiconnect.backend.dto.support;

import com.mzansiconnect.backend.enums.SupportIssueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupportTicketCreateRequest {

    @NotNull(message = "Issue type is required")
    private SupportIssueType issueType;

    @NotBlank(message = "Subject is required")
    @Size(max = 200, message = "Subject cannot exceed 200 characters")
    private String subject;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
}