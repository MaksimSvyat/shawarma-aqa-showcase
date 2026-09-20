package com.shawarmashop.tests.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiError {
    private String type;
    private String title;
    private Integer status;
    private String detail;
    private String instance;
    private Instant timestamp;
}
