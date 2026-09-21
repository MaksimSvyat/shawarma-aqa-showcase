package com.shawarmashop.tests.dto.recipes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeIngredientsResponse {
    private String recipeName;
    private List<IngredientInfo> ingredients;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredientInfo {
        private Long ingredientId;
        private String name;
        private Integer qtyNeeded;
        private String unit;
        private Integer onHand;
    }
}
