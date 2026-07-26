package com.mzansiconnect.backend.repository;

import com.mzansiconnect.backend.entity.SupportTicket;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupportTicketRepository
        extends JpaRepository<SupportTicket, Long> {

    @EntityGraph(attributePaths = {"user", "user.roles"})
    List<SupportTicket> findByUser_IdOrderByCreatedAtDesc(
            Long userId
    );

    @EntityGraph(attributePaths = {"user", "user.roles"})
    Optional<SupportTicket> findByIdAndUser_Id(
            Long id,
            Long userId
    );

    @Override
    @EntityGraph(attributePaths = {"user", "user.roles"})
    List<SupportTicket> findAll();
}