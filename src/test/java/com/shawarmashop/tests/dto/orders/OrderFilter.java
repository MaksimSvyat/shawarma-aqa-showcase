package com.shawarmashop.tests.dto.orders;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Value
@Builder
public class OrderFilter {
    String status;
    Instant fromDate;
    Instant toDate;
    Integer minPrice;
    Integer maxPrice;
    Integer recipeId;
    Integer page;
    Integer size;
    String sort;

    public Map<String, Object> toQueryParams(){
        Map<String, Object> params = new LinkedHashMap<>();
        putIfPresent(params, "status", status);
        putIfPresent(params, "fromDate", fromDate);
        putIfPresent(params, "toDate", toDate);
        putIfPresent(params, "minPrice", minPrice);
        putIfPresent(params, "maxPrice", maxPrice);
        putIfPresent(params, "recipeId", recipeId);
        putIfPresent(params, "page", page);
        putIfPresent(params, "size", size);
        putIfPresent(params, "sort", sort);
        return params;
    }

    private static void putIfPresent(Map<String, Object> params, String key, Object value) {
        if (value != null) {
            params.put(key, value);
        }
    }

}
