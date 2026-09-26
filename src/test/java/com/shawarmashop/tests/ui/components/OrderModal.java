package com.shawarmashop.tests.ui.components;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Selenide.$;

public class OrderModal {

    private final SelenideElement root = $("div.arcade-modal");
    private final SelenideElement quantityInput = root.$("input[type='number']");
    private final SelenideElement cancelButton = root.$("button.arcade-btn--secondary");
    private final SelenideElement submitButton = root.$("button[type='submit']");

    public OrderModal shouldOpened() {
        root.shouldBe(Condition.visible);
        return this;
    }

    public OrderModal setQty(int qty) {
        quantityInput.sendKeys(String.valueOf(qty));
        return this;
    }

    @Step("Отменить создание заказа")
    public void cancel() {
        cancelButton.shouldBe(Condition.enabled).click();
        root.shouldNot(Condition.exist);
    }

    @Step("Подтвердить создание заказа")
    public void submit() {
        submitButton.shouldBe(Condition.enabled).click();
    }
}
