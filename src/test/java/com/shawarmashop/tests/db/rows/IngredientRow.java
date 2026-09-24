package com.shawarmashop.tests.db.rows;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IngredientRow {
    private Long id;
    private String name;
    private String unit;
    private Integer onHand;
    private Integer minLevel;
    private String supplierCode;
    private String supplierChannel;
    private Integer supplierLeadTimeDays;
}
