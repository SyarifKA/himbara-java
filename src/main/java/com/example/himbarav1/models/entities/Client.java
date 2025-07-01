package com.example.himbarav1.models.entities;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
    private String productName;

    @Column(length = 50)
    private String orderId;

    @Column(length = 50)
    private String sessionId;

    @Column(length = 15)
    private String status;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime created_at;

    @Column
    private OffsetDateTime updated_at;

    @PrePersist
    protected void onCreate() {
        this.created_at = OffsetDateTime.now(ZoneOffset.UTC);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updated_at = OffsetDateTime.now(ZoneOffset.UTC);
    }
}
