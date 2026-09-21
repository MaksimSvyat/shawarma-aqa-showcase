package com.shawarmashop.tests.dto.ingredients;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupplierInfo {
    private String code;
    private String channel;
    private Integer leadTimeDays;
}
