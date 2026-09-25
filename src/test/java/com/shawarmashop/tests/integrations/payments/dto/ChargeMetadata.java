package com.shawarmashop.tests.integrations.payments.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargeMetadata {
    @JsonAlias("recipe_id")
    private Long recipeId;
    @JsonAlias("recipe_name")
    private String recipeName;
}
