package com.shawarmashop.tests.db.rows;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class PaymentRow {
    private Long id;
    private Long orderId;
    private String idempotencyKey;
    private BigDecimal amount;
    private String method;
    private String txnId;
    private String status;
    private String failureReason;
    private Instant createdAt;
}
