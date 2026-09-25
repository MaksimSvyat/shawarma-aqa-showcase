package com.shawarmashop.tests.integrations.bread.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreadConfirmationDto {
    @JsonAlias("batch_id")
    private String batchId;

    @JsonAlias("ready_in_sec")
    private int readyInSec;

    @JsonAlias("price_per_unit")
    private int pricePerUnit;

    @JsonAlias("total_price")
    private int totalPrice;

    private BreadBakery bakery;
}
