package com.shawarmashop.tests.dto.orders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreparationResponse {
    private Integer orderId;
    private String status;
    private Integer progress;
    private Instant startedAt;
    private Instant eta;
    private Integer remainingSeconds;
}
