package com.shawarmashop.tests.e2e.showcase.mocks;

import com.shawarmashop.tests.auth.ApiClient;
import com.shawarmashop.tests.auth.Users;
import com.shawarmashop.tests.db.rows.RecipeRow;
import com.shawarmashop.tests.dto.recipes.RecipeResponse;
import com.shawarmashop.tests.e2e.support.helpers.RecipeHelper;
import com.shawarmashop.tests.extensions.DbTest;
import com.shawarmashop.tests.extensions.IntegrationsTest;
import com.shawarmashop.tests.integrations.IntegrationStubs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationsTest
@DbTest
@DisplayName("Showcase: GraphQL reviews — rating мерджится в /recipes/{id}, GraphQL error → null")
class GraphQLReviewsShowcaseTest {

    private final ApiClient owner = ApiClient.asUser(Users.OWNER);
    private final RecipeHelper recipeHelper = new RecipeHelper();

    @Test
    @DisplayName("happy: застабленный rating оказывается в теле ответа /recipes/{id}")
    void ratingMergedIntoRecipe(IntegrationStubs stubs) {
        RecipeRow seeded = recipeHelper.createRecipeWithPrep("REVIEWS_SHOWCASE", 20);
        stubs.reviews()
                .ratingByRecipe(seeded.getId())
                .respondWith(4.4, 12, "слишком сухой", "UP");

        RecipeResponse loaded = owner.recipes().get(seeded.getId()).success();
        assertThat(loaded.getRating()).isNotNull();
        assertThat(loaded.getRating().getAverageStars()).isEqualTo(4.4);
        assertThat(loaded.getRating().getTotalReviews()).isEqualTo(12);
        assertThat(loaded.getRating().getTopComplaint()).isEqualTo("слишком сухой");
        assertThat(loaded.getRating().getTrend()).isEqualTo("UP");
    }

    @Test
    @DisplayName("партнёр вернул GraphQL errors → recipe грузится с rating=null")
    void graphQLErrorFallsBackToNullRating(IntegrationStubs stubs) {
        RecipeRow seeded = recipeHelper.createRecipeWithPrep("REVIEWS_SHOWCASE", 20);
        stubs.reviews()
                .ratingByRecipe(seeded.getId())
                .respondWithGraphQLError("rate-limit exceeded");

        RecipeResponse loaded = owner.recipes().get(seeded.getId()).success();
        assertThat(loaded.getRating()).isNull();
        assertThat(loaded.getId()).isEqualTo(seeded.getId());
        assertThat(loaded.getName()).isEqualTo(seeded.getName());
    }

}
