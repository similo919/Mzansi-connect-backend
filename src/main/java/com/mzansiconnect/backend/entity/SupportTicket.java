package com.mzansiconnect.backend.entity;

import com.mzansiconnect.backend.enums.SupportIssueType;
import com.mzansiconnect.backend.enums.SupportTicketPriority;
import com.mzansiconnect.backend.enums.SupportTicketStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "support_ticket")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_support_tickets_user")
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "issue_type",
            nullable = false,
            columnDefinition = "enum('FARE_DISPUTE','INCORRECT_ROUTE','INCORRECT_TAXI_RANK','SAFETY_CONCERN','ACCOUNT_PROBLEM','TECHNICAL_PROBLEM','OTHER')"
    )
    private SupportIssueType issueType;

    @Column(name = "subject", nullable = false, length = 200)
    private String subject;

    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            columnDefinition = "enum('OPEN','IN_PROGRESS','RESOLVED','CLOSED')"
    )
    @Builder.Default
    private SupportTicketStatus status = SupportTicketStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "priority",
            nullable = false,
            columnDefinition = "enum('LOW','MEDIUM','HIGH','URGENT')"
    )
    @Builder.Default
    private SupportTicketPriority priority = SupportTicketPriority.MEDIUM;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}
