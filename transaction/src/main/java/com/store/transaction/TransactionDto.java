package com.store.transaction;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
public class TransactionDto {
    private Long id;
    private Long userId; // Updated for clarity
    private Long productId; // Updated for clarity
    private Integer quantity;
    private Double totalPrice;
    private LocalDateTime createdAt;
}

