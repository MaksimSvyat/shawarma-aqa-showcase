package com.shawarmashop.tests.dto.recipes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReviewPageResponse {
    private Long recipeId;
    private Integer totalCount;
    private Double averageStars;
    private Integer page;
}
