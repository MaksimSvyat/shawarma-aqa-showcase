package com.shawarmashop.tests.e2e.support.helpers;

import com.shawarmashop.tests.db.factories.RecipeFixture;
import com.shawarmashop.tests.db.rows.RecipeRow;
import com.shawarmashop.tests.e2e.support.utils.UniqueNameUtils;
import io.qameta.allure.Step;

public class RecipeHelper {

    @Step("Создание рецепта: prefix={0}, price={1}")
    public RecipeRow createRecipeWithPrice(String prefix, Double price) {
        return RecipeFixture.builder()
                .name(UniqueNameUtils.uniqueName(prefix))
                .price(price)
                .build()
                .insert();
    }

    @Step("Создание рецепта: prefix={0}, prepSeconds={1}")
    public RecipeRow createRecipeWithPrep(String prefix, int prepSeconds) {
        return RecipeFixture.builder()
                .name(UniqueNameUtils.uniqueName(prefix))
                .prepSeconds(prepSeconds)
                .build()
                .insert();
    }

}
