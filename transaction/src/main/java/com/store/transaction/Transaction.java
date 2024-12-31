package com.store.transaction;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id") // Avoid reserved word conflict
    private Long user;

    @Column(name = "product_id") // Optionally rename for consistency
    private Long product;

    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer quantity;

    @Column(nullable = false, columnDefinition = "DOUBLE PRECISION DEFAULT 0.0")
    private Double totalPrice;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
