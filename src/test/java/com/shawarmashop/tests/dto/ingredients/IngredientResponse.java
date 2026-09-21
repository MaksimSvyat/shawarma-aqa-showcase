package com.shawarmashop.tests.dto.ingredients;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IngredientResponse {
    private Long id;
    private String name;
    private String unit;
    private Integer onHand;
    private Integer minLevel;
    private SupplierInfo supplier;
}
