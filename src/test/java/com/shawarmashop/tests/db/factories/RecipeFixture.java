package com.shawarmashop.tests.db.factories;

import com.shawarmashop.tests.db.rows.RecipeRow;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class RecipeFixture {

    @Builder.Default
    private String name = "TEST_RECIPE_" + UUID.randomUUID();
    @Builder.Default
    private String description = "test fixture";
    @Builder.Default
    private String size = "MEDIUM";
    @Builder.Default
    private Double price = 99.00;
    @Builder.Default
    private Integer prepSeconds = 10;
    @Builder.Default
    private String imageUrl = "https://picsum.photos/200/300";

    public RecipeRow insert() {
        return Inserts.recipe(this);
    }
}
