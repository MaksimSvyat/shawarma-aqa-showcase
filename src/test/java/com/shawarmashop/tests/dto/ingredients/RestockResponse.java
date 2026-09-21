package com.shawarmashop.tests.dto.ingredients;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestockResponse {
    private Integer id;
    private String name;
    private Integer onHand;
    private Integer totalCost;
    private String message;
}
