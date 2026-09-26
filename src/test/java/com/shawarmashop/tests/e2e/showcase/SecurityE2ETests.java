package com.shawarmashop.tests.e2e.showcase;

import com.shawarmashop.tests.auth.ApiClient;
import com.shawarmashop.tests.auth.Credentials;
import com.shawarmashop.tests.auth.Users;
import com.shawarmashop.tests.dto.auth.JwtResponse;
import com.shawarmashop.tests.dto.orders.CreateOrderRequest;
import com.shawarmashop.tests.dto.orders.OrderFilter;
import com.shawarmashop.tests.dto.recipes.RecipeResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("E2E: безопасность")
class SecurityE2ETests {

    private static final int RECIPE_ID = 1;
    private final ApiClient anonymousClient = ApiClient.anonymous();
    private final ApiClient invalidTokenClient = ApiClient.withToken("not-a-real-jwt");
    private final ApiClient ownerClient = ApiClient.asUser(Users.OWNER);

    @Test
    @DisplayName("аноним получает 401 на защищённых ручках")
    void protectedEndpointsRejectAnonymous() {
        assertAll(
                "Все защищённые ручки должны возвращать 401",

                () -> anonymousClient.auth()
                        .me()
                        .assertStatus(401),

                () -> anonymousClient.orders()
                        .list(OrderFilter.builder().build())
                        .assertStatus(401),

                () -> anonymousClient.orders()
                        .create(CreateOrderRequest.of(RECIPE_ID, 1, "CARD"))
                        .assertStatus(401),

                () -> anonymousClient.ingredients()
                        .list()
                        .assertStatus(401),

                () -> anonymousClient.events()
                        .list()
                        .assertStatus(401)
        );
    }

    @Test
    @DisplayName("невалидный JWT возвращает 401")
    void invalidTokenIs401() {
        invalidTokenClient.orders()
                .list(OrderFilter.builder().build())
                .assertStatus(401);
    }

    @Test
    @DisplayName("валидный JWT открывает защищённые ручки")
    void validTokenAllowsAccess() {
        assertAll(
                "Владелец должен иметь доступ к защищённым ручкам",

                () -> ownerClient.auth()
                        .me()
                        .success(),

                () -> ownerClient.orders()
                        .list(OrderFilter.builder().build())
                        .success(),

                () -> ownerClient.ingredients()
                        .list()
                        .success(),

                () -> ownerClient.events()
                        .list()
                        .success()
        );
    }

    @Test
    @DisplayName("витрина рецептов доступна без JWT")
    void publicRecipesReachableAnonymously() {
        anonymousClient.recipes()
                .list()
                .success();

        RecipeResponse recipe = anonymousClient.recipes()
                .get(RECIPE_ID)
                .success();

        assertThat(recipe.getId())
                .isEqualTo(RECIPE_ID);

        assertThat(recipe.getName())
                .isNotBlank();
    }

    @Test
    @DisplayName("login доступен анонимно и выдаёт JWT")
    void loginIsPublic() {
        Credentials owner = Users.OWNER;

        JwtResponse response = anonymousClient.auth()
                .login(owner.getUsername(), owner.getPassword())
                .success();

        assertThat(response.getToken())
                .isNotBlank();
    }
}

