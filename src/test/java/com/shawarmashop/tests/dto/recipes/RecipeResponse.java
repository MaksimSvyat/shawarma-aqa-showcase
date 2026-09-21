package com.shawarmashop.tests.dto.recipes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeResponse {
    private Integer id;
    private String name;
    private String size;
    private BigDecimal price;
    private Integer prepSeconds;
    private List<RecipeIngredientView> ingredients;
    private String imageUrl;
    private RecipeRating rating;
}
