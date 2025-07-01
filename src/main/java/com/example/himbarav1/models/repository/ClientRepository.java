package com.example.himbarav1.models.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.himbarav1.models.entities.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {
    boolean existsBySessionId(String sessionId);

    Optional<Client> findBySessionId(String sessionId);

    Optional<Client> findByOrderId(String orderId);
}