package com.shawarmashop.tests.e2e.showcase;

import com.shawarmashop.tests.auth.ApiClient;
import com.shawarmashop.tests.auth.Users;
import com.shawarmashop.tests.db.rows.RecipeRow;
import com.shawarmashop.tests.dto.recipes.RecipeResponse;
import com.shawarmashop.tests.dto.recipes.UpdateRecipeRequest;
import com.shawarmashop.tests.e2e.support.helpers.RecipeHelper;
import com.shawarmashop.tests.e2e.support.utils.ResourceUtils;
import com.shawarmashop.tests.e2e.support.utils.UniqueNameUtils;
import com.shawarmashop.tests.extensions.DbTest;
import com.shawarmashop.tests.extensions.IntegrationsTest;
import com.shawarmashop.tests.integrations.IntegrationStubs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationsTest
@DbTest
@DisplayName("E2E: управление рецептами")
class RecipeManagementE2ETest {

    private static final String SANDWICH_IMAGE = "src/test/resources/test-data/sandwich.jpg";
    private static final Double DEFAULT_PRICE = 199.00;

    private final ApiClient owner = ApiClient.asUser(Users.OWNER);
    private final RecipeHelper recipeHelper = new RecipeHelper();

    @Test
    @DisplayName("PATCH обновляет имя, описание и цену")
    void patchUpdatesFields() {
        RecipeRow recipe = recipeHelper.createRecipeWithPrice("PATCH_SOURCE", DEFAULT_PRICE);

        String newName = UniqueNameUtils.uniqueName("PATCHED");
        String newDescription = "Обновлённое описание";
        BigDecimal newPrice = new BigDecimal("345.00");

        RecipeResponse updated = owner.recipes()
                .update(
                        recipe.getId(),
                        UpdateRecipeRequest.builder()
                                .name(newName)
                                .description(newDescription)
                                .price(newPrice)
                                .build()
                )
                .success();

        assertThat(updated.getId()).isEqualTo(recipe.getId());

        assertThat(updated.getName()).isEqualTo(newName);

        assertThat(updated.getPrice()).isEqualByComparingTo(newPrice);

        RecipeResponse fetched = owner.recipes()
                .get(recipe.getId())
                .success();

        assertThat(fetched.getName()).isEqualTo(newName);

        assertThat(fetched.getPrice()).isEqualByComparingTo(newPrice);
    }

    @Test
    @DisplayName("PATCH несуществующего рецепта возвращает 404")
    void patchUnknownIs404() {
        long unknownRecipeId = 99_999_999L;

        owner.recipes()
                .update(
                        unknownRecipeId,
                        UpdateRecipeRequest.builder()
                                .price(new BigDecimal("10.00"))
                                .build()
                )
                .assertStatus(404);
    }

    @Test
    @DisplayName("PATCH с занятым именем возвращает 409")
    void patchDuplicateNameIs409() {
        RecipeRow existing = recipeHelper.createRecipeWithPrice("DUPLICATE_EXISTING", DEFAULT_PRICE);
        RecipeRow target = recipeHelper.createRecipeWithPrice("DUPLICATE_TARGET", DEFAULT_PRICE);

        owner.recipes()
                .update(
                        target.getId(),
                        UpdateRecipeRequest.builder()
                                .name(existing.getName())
                                .build()
                )
                .assertStatus(409);

        RecipeResponse unchanged = owner.recipes()
                .get(target.getId())
                .success();

        assertThat(unchanged.getName()).isEqualTo(target.getName());
    }

    @Test
    @DisplayName("POST /image принимает JPEG и возвращает URL")
    void uploadImageHappy() {
        RecipeRow recipe = recipeHelper.createRecipeWithPrice("IMAGE_UPLOAD", DEFAULT_PRICE);
        byte[] image = ResourceUtils.readResource(SANDWICH_IMAGE);

        var result = owner.recipes()
                .uploadImage(
                        recipe.getId(),
                        "sandwich.jpg",
                        "image/jpeg",
                        image
                );

        result.assertStatus(200);

        assertThat(result.getRawBody()).contains("/uploads/recipes/");
    }

    @Test
    @DisplayName("POST /image с неподдерживаемым типом возвращает 400")
    void uploadImageWrongTypeIs400() {
        RecipeRow recipe = recipeHelper.createRecipeWithPrice("IMAGE_WRONG_TYPE", DEFAULT_PRICE);

        owner.recipes()
                .uploadImage(
                        recipe.getId(),
                        "note.txt",
                        "text/plain",
                        "not an image".getBytes()
                )
                .assertStatus(400);
    }


    @Test
    @DisplayName("GraphQL-ошибка партнёра на GET /reviews возвращает 502")
    void reviewsPartnerErrorIs502(IntegrationStubs stubs) {
        RecipeRow recipe = recipeHelper.createRecipeWithPrice("REVIEWS_ERROR", DEFAULT_PRICE);

        stubs.reviews()
                .reviewsByRecipeError(recipe.getId(), "rate-limit exceeded");

        owner.recipes()
                .reviews(recipe.getId(), 0)
                .assertStatus(502);
    }

}
