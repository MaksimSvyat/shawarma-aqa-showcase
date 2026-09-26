package com.shawarmashop.tests.e2e.showcase;

import com.shawarmashop.tests.auth.ApiClient;
import com.shawarmashop.tests.auth.Users;
import com.shawarmashop.tests.dto.Page;
import com.shawarmashop.tests.dto.orders.OrderFilter;
import com.shawarmashop.tests.dto.orders.OrderResponse;
import com.shawarmashop.tests.e2e.support.helpers.OrderHelper;
import com.shawarmashop.tests.extensions.DbTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DbTest
@DisplayName("E2E: GET /orders — фильтры и пагинация")
class OrderQueryE2ETest {

    private static final int RECIPE_ID = 1;
    private final ApiClient owner = ApiClient.asUser(Users.OWNER);
    private final OrderHelper orderHelper = new OrderHelper(owner);

    @Test
    @DisplayName("status=PENDING возвращает только PENDING и содержит созданный заказ")
    void filterByStatus() {
        OrderResponse created = orderHelper.createOrder(RECIPE_ID, 1, "CARD");

        Page<OrderResponse> page = owner.orders()
                .list(
                        OrderFilter.builder()
                                .status("PENDING")
                                .size(100)
                                .build()
                )
                .success();

        assertThat(page.getContent())
                .allSatisfy(order ->
                        assertThat(order.getStatus()).isEqualTo("PENDING")
                )
                .anySatisfy(order ->
                        assertThat(order.getId()).isEqualTo(created.getId())
                );
    }

    @Test
    @DisplayName("recipeId возвращает только заказы указанного рецепта")
    void filterByRecipeId() {
        OrderResponse created = orderHelper.createOrder(RECIPE_ID, 1, "CARD");

        Page<OrderResponse> page = owner.orders()
                .list(
                        OrderFilter.builder()
                                .recipeId(RECIPE_ID)
                                .size(100)
                                .build()
                )
                .success();

        assertThat(page.getContent())
                .allSatisfy(order ->
                        assertThat(order.getRecipe().getId()).isEqualTo(RECIPE_ID)
                )
                .anySatisfy(order ->
                        assertThat(order.getId()).isEqualTo(created.getId())
                );
    }

    @Test
    @DisplayName("minPrice и maxPrice ограничивают цену заказа")
    void filterByPriceRange() {
        OrderResponse created = orderHelper.createOrder(RECIPE_ID, 1, "CARD");
        Integer minPrice = 200;
        Integer maxPrice = 300;

        Page<OrderResponse> page = owner.orders()
                .list(
                        OrderFilter.builder()
                                .recipeId(RECIPE_ID)
                                .minPrice(minPrice)
                                .maxPrice(maxPrice)
                                .size(100)
                                .build()
                )
                .success();

        assertThat(page.getContent())
                .allSatisfy(order ->
                        assertThat(order.getTotalPrice())
                                .isBetween(minPrice.doubleValue(), maxPrice.doubleValue())
                )
                .anySatisfy(order ->
                        assertThat(order.getId())
                                .isEqualTo(created.getId())
                );
    }

    @Test
    @DisplayName("minPrice исключает заказ дешевле порога")
    void minPriceExcludesCheaperOrder() {
        OrderResponse cheapOrder = orderHelper.createOrder(RECIPE_ID, 1, "CARD");
        int minPrice = 1000;

        Page<OrderResponse> page = owner.orders()
                .list(
                        OrderFilter.builder()
                                .recipeId(RECIPE_ID)
                                .minPrice(minPrice)
                                .size(100)
                                .build()
                )
                .success();

        assertThat(page.getContent())
                .allSatisfy(order ->
                        assertThat(order.getTotalPrice())
                                .isGreaterThanOrEqualTo(minPrice)
                )
                .noneSatisfy(order ->
                        assertThat(order.getId())
                                .isEqualTo(cheapOrder.getId())
                );
    }

    @Test
    @DisplayName("параметр size ограничивает размер страницы")
    void paginationRespectsSize() {
        orderHelper.createOrder(RECIPE_ID, 1, "CARD");
        orderHelper.createOrder(RECIPE_ID, 1, "CARD");

        Page<OrderResponse> page = owner.orders()
                .list(
                        OrderFilter.builder()
                                .recipeId(RECIPE_ID)
                                .size(1)
                                .build()
                )
                .success();

        assertThat(page.getContent())
                .hasSize(1);

        assertThat(page.getSize())
                .isEqualTo(1);

        assertThat(page.getTotalElements())
                .isGreaterThanOrEqualTo(2);
    }

}
