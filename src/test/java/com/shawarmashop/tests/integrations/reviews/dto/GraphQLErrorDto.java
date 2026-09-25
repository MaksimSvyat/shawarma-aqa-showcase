package com.shawarmashop.tests.integrations.reviews.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GraphQLErrorDto {
    private String message;
    private List<Object> path;
    private Map<String, Object> extensions;
}
