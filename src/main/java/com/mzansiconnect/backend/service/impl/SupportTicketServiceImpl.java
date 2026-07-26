package com.mzansiconnect.backend.service.impl;

import com.mzansiconnect.backend.dto.support.SupportTicketCreateRequest;
import com.mzansiconnect.backend.dto.support.SupportTicketResponse;
import com.mzansiconnect.backend.entity.SupportTicket;
import com.mzansiconnect.backend.entity.User;
import com.mzansiconnect.backend.enums.SupportTicketPriority;
import com.mzansiconnect.backend.enums.SupportTicketStatus;
import com.mzansiconnect.backend.exception.BusinessValidationException;
import com.mzansiconnect.backend.exception.ResourceNotFoundException;
import com.mzansiconnect.backend.mapper.UserMapper;
import com.mzansiconnect.backend.repository.SupportTicketRepository;
import com.mzansiconnect.backend.repository.UserRepository;
import com.mzansiconnect.backend.service.SupportTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SupportTicketServiceImpl
        implements SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public SupportTicketResponse createTicket(
            String email,
            SupportTicketCreateRequest request
    ) {
        User user = getCurrentUser(email);

        SupportTicket ticket = SupportTicket.builder()
                .user(user)
                .issueType(request.getIssueType())
                .subject(normalizeRequiredText(
                        request.getSubject(),
                        "Subject is required"
                ))
                .description(normalizeRequiredText(
                        request.getDescription(),
                        "Description is required"
                ))
                .status(SupportTicketStatus.OPEN)
                .priority(SupportTicketPriority.MEDIUM)
                .build();

        return toResponse(supportTicketRepository.save(ticket));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketResponse> getCurrentUserTickets(
            String email
    ) {
        User user = getCurrentUser(email);

        return supportTicketRepository
                .findByUser_IdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SupportTicketResponse getCurrentUserTicket(
            String email,
            Long ticketId
    ) {
        User user = getCurrentUser(email);

        return supportTicketRepository
                .findByIdAndUser_Id(ticketId, user.getId())
                .map(this::toResponse)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Support ticket was not found"
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketResponse> getAllTicketsForAdmin() {
        return supportTicketRepository.findAll()
                .stream()
                .sorted(
                        Comparator.comparing(
                                SupportTicket::getCreatedAt,
                                Comparator.nullsLast(
                                        Comparator.reverseOrder()
                                )
                        )
                )
                .map(this::toResponse)
                .toList();
    }

    @Override
    public SupportTicketResponse updateStatus(
            Long ticketId,
            SupportTicketStatus status
    ) {
        SupportTicket ticket = getTicket(ticketId);
        ticket.setStatus(status);

        if (status == SupportTicketStatus.RESOLVED
                || status == SupportTicketStatus.CLOSED) {
            if (ticket.getResolvedAt() == null) {
                ticket.setResolvedAt(LocalDateTime.now());
            }
        } else {
            ticket.setResolvedAt(null);
        }

        return toResponse(ticket);
    }

    @Override
    public SupportTicketResponse updatePriority(
            Long ticketId,
            SupportTicketPriority priority
    ) {
        SupportTicket ticket = getTicket(ticketId);
        ticket.setPriority(priority);

        return toResponse(ticket);
    }

    private SupportTicketResponse toResponse(SupportTicket ticket) {
        return SupportTicketResponse.builder()
                .id(ticket.getId())
                .user(userMapper.toResponse(ticket.getUser()))
                .issueType(ticket.getIssueType())
                .subject(ticket.getSubject())
                .description(ticket.getDescription())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .resolvedAt(ticket.getResolvedAt())
                .build();
    }

    private User getCurrentUser(String email) {
        return userRepository
                .findByEmailIgnoreCaseAndEnabledTrue(email)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Authenticated user was not found"
                        )
                );
    }

    private SupportTicket getTicket(Long ticketId) {
        return supportTicketRepository.findById(ticketId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Support ticket was not found"
                        )
                );
    }

    private String normalizeRequiredText(
            String value,
            String message
    ) {
        String normalized = value == null ? null : value.trim();

        if (normalized == null || normalized.isBlank()) {
            throw new BusinessValidationException(message);
        }

        return normalized;
    }
}