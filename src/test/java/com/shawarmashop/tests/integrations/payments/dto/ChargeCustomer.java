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
public class ChargeCustomer {
    private Long id;
    @JsonAlias("display_name")
    private String displayName;
}
