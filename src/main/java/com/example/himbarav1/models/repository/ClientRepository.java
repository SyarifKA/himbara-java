package com.example.himbarav1.models.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.himbarav1.models.entities.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {

}