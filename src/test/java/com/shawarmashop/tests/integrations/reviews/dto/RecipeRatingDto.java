package com.shawarmashop.tests.integrations.reviews.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecipeRatingDto {
    @JsonAlias("recipe_id")
    private Integer recipeId;

    @JsonAlias("average_stars")
    private Double averageStars;

    @JsonAlias("total_reviews")
    private Integer totalReviews;

    @JsonAlias("top_complaint")
    private String topComplaint;

    private String trend;
}
