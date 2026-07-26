package com.mzansiconnect.backend.service;

import com.mzansiconnect.backend.dto.support.SupportTicketCreateRequest;
import com.mzansiconnect.backend.dto.support.SupportTicketResponse;
import com.mzansiconnect.backend.enums.SupportTicketPriority;
import com.mzansiconnect.backend.enums.SupportTicketStatus;

import java.util.List;

public interface SupportTicketService {

    SupportTicketResponse createTicket(
            String email,
            SupportTicketCreateRequest request
    );

    List<SupportTicketResponse> getCurrentUserTickets(String email);

    SupportTicketResponse getCurrentUserTicket(
            String email,
            Long ticketId
    );

    List<SupportTicketResponse> getAllTicketsForAdmin();

    SupportTicketResponse updateStatus(
            Long ticketId,
            SupportTicketStatus status
    );

    SupportTicketResponse updatePriority(
            Long ticketId,
            SupportTicketPriority priority
    );
}