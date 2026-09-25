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
public class BreadOrderDto {
    @JsonAlias("order_ref")
    private String orderRef;

    private BreadOrderBread bread;
    private BreadOrderPickup pickup;

    @JsonAlias("coupon_code")
    private String couponCode;

    private boolean urgent;
}
