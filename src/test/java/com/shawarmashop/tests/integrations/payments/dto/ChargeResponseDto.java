package com.shawarmashop.tests.integrations.payments.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChargeResponseDto {
    @JsonAlias("txn_id")
    private String txnId;
    private ChargeStatus status;
    @JsonAlias("processed_at")
    private Instant processedAt;
    private ChargeMoney fee;
    private String message;
}
