package com.example.himbarav1.models.entities;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "client")
public class Client implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 15, nullable = false)
    private String phoneNumber;

    @Column(length = 20)
    private String bank;

    @Column(length = 255)
    private String urlRedirect;

    private Integer price;

    @Column(length = 50)
    private String productId;

    @Column(length = 50)
    private String sessionId;

    @Column(length = 15)
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP(0)")
    private LocalDateTime created_at;

    // @Column(nullable = false)
    private Timestamp updated_at;

    @PrePersist
    protected void onCreate() {
        this.created_at = LocalDateTime.now().withNano(0);
    }

}
