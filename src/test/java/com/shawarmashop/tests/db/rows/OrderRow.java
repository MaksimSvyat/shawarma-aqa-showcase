package com.shawarmashop.tests.db.rows;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class OrderRow {
    private Long id;
    private Long recipeId;
    private Integer qty;
    private BigDecimal totalPrice;
    private String status;
    private Instant placedAt;
    private Instant etaAt;
    private Instant completedAt;
    private String breadBatchId;
    private String paymentMethod;
}

