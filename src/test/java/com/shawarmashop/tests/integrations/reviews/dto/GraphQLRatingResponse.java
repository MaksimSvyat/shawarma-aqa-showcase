package com.shawarmashop.tests.integrations.reviews.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GraphQLRatingResponse {

    private Payload data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<GraphQLErrorDto> errors;

    public static GraphQLRatingResponse of(RecipeRatingDto ratingDto) {
        return GraphQLRatingResponse.builder()
                .data(new Payload(ratingDto))
                .build();
    }

    public static GraphQLRatingResponse empty() {
        return GraphQLRatingResponse.builder()
                .data(null)
                .build();
    }

    public static GraphQLRatingResponse error(String error) {
        return GraphQLRatingResponse.builder()
                .data(null)
                .errors(List.of(GraphQLErrorDto.builder().message(error).build()))
                .build();
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Payload {
        private RecipeRatingDto recipeRating;
    }
}
