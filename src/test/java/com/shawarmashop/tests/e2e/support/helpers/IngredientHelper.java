package com.shawarmashop.tests.e2e.support.helpers;


import com.shawarmashop.tests.db.factories.IngredientFixture;
import com.shawarmashop.tests.db.rows.IngredientRow;
import com.shawarmashop.tests.e2e.support.utils.UniqueNameUtils;
import io.qameta.allure.Step;

public class IngredientHelper {

    private static final int DEFAULT_MIN_LEVEL = 50;
    private static final int DEFAULT_ON_HAND = 100;
    private static final int DEFAULT_LEAD_TIME_DAYS = 1;

    @Step("Создание ингредиента: namePrefix={0}, supplierChannel={1}, onHand={2}")
    public IngredientRow createIngredient(String namePrefix, String supplierChannel, int onHand) {
        return IngredientFixture.builder()
                .name(UniqueNameUtils.uniqueName(namePrefix))
                .supplierChannel(supplierChannel)
                .onHand(onHand)
                .minLevel(DEFAULT_MIN_LEVEL)
                .supplierLeadTimeDays(DEFAULT_LEAD_TIME_DAYS)
                .build()
                .insert();
    }

    @Step("Создание SOAP-ингредиента: namePrefix={0}, supplierCode={1}")
    public int createSoapIngredientId(String namePrefix, String supplierCode) {
        return Math.toIntExact(IngredientFixture.builder()
                .name(namePrefix + "_SOAP_INGREDIENT_" + System.nanoTime())
                .supplierChannel("SOAP")
                .supplierCode(supplierCode)
                .onHand(DEFAULT_ON_HAND)
                .minLevel(DEFAULT_MIN_LEVEL)
                .supplierLeadTimeDays(DEFAULT_LEAD_TIME_DAYS)
                .build()
                .insert()
                .getId());
    }
}
