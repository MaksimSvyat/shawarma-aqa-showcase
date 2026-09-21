package com.shawarmashop.tests.dto.recipes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeRating {
    private Long recipeId;
    private Double averageStars;
    private Integer totalReviews;
    private String topComplaint;
    private String trend;
}
