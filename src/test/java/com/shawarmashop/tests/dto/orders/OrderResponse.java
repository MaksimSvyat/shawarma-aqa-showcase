package com.shawarmashop.tests.dto.orders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Integer id;
    private String status;
    private OrderRecipeSlim recipe;
    private Integer qty;
    private Double totalPrice;
    private Instant placedAt;
    private Instant etaAt;
    private OrderPaymentSlim payment;
}
