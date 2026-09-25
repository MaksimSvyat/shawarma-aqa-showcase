package com.shawarmashop.tests.integrations.reviews;

import com.shawarmashop.tests.integrations.reviews.dto.GraphQLRatingResponse;
import com.shawarmashop.tests.integrations.reviews.dto.RecipeRatingDto;
import com.shawarmashop.tests.integrations.wiremock.StubBuilder;
import com.shawarmashop.tests.integrations.wiremock.WireMockAdminClient;
import com.shawarmashop.tests.integrations.wiremock.WireMockStubBase;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import static com.shawarmashop.tests.integrations.wiremock.StubBuilder.post;

public class ReviewsStubs extends WireMockStubBase {

    private final static String GRAPHQL_PATH = "/graphql";
    private final static String OPERATION_RECIPE_RATING = "RecipeRating";
    private final static String OPERATION_REVIEWS_BY_RECIPE = "ReviewsByRecipe";

    public ReviewsStubs(WireMockAdminClient admin) {
        super(admin);
    }

    public RatingBuilder ratingByRecipe(int recipeId) {
        return new RatingBuilder(recipeId);
    }

    public void reviewsByRecipeError(int recipeId, String message) {
        admin.addMapping(post(GRAPHQL_PATH)
                .withPriority(1)
                .withJsonPath("$.operationName", OPERATION_REVIEWS_BY_RECIPE)
                .withJsonPath("$.variables.recipeId", String.valueOf(recipeId))
                .willReturnJson(200, GraphQLRatingResponse.error(message)));
    }

    @RequiredArgsConstructor
    @Data
    public final class RatingBuilder {
        private final int recipeId;

        public void respondWith(double averageStars, int totalReviews, String topComplaint, String trend) {
            respondWith(RecipeRatingDto.builder()
                    .recipeId(recipeId)
                    .averageStars(averageStars)
                    .totalReviews(totalReviews)
                    .topComplaint(topComplaint)
                    .trend(trend)
                    .build());
        }

        public void respondWith(RecipeRatingDto ratingDto) {
            admin.addMapping(graphqlMapping().willReturnJson(200, GraphQLRatingResponse.of(ratingDto)));
        }

        public void respondWithNull() {
            admin.addMapping(graphqlMapping().willReturnJson(200, GraphQLRatingResponse.empty()));
        }

        public void respondWithGraphQLError(String message) {
            admin.addMapping(graphqlMapping().willReturnJson(200, GraphQLRatingResponse.error(message)));
        }

        private StubBuilder graphqlMapping(){
            return post(GRAPHQL_PATH)
                    .withPriority(1)
                    .withJsonPath("$.operationName", OPERATION_RECIPE_RATING)
                    .withJsonPath("$.variables.recipeId", String.valueOf(recipeId));
        }
    }
}
