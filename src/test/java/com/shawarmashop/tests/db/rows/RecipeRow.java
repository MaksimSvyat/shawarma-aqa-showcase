package com.shawarmashop.tests.db.rows;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecipeRow {
    private Integer id;
    private String name;
    private String description;
    private String size;
    private Double price;
    private Integer prepSeconds;
    private String imageUrl;
}
