package com.shawarmashop.tests.integrations.payments.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargeRequestDto {
    @JsonAlias("order_ref")
    private String orderRef;
    private ChargeMoney amount;
    private String method;
    private ChargeCustomer customer;
    private ChargeMetadata metadata;
}
