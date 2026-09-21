package com.shawarmashop.tests.dto.orders;

import com.shawarmashop.tests.dto.PaymentChoice;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateOrderRequest {
    private Integer recipeId;
    private Integer qty;
    private PaymentChoice payment;

    public static CreateOrderRequest of(int recipeId, int qty, String method) {
        return CreateOrderRequest.builder()
                .recipeId(recipeId)
                .qty(qty)
                .payment(new PaymentChoice(method))
                .build();
    }
}
