package com.shawarmashop.tests.ui.components;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.shawarmashop.tests.ui.model.OrderUiStatus;
import io.qameta.allure.Step;
import lombok.Getter;

@Getter
public class OrderKanbanCard {

    private final SelenideElement root;
    private final SelenideElement recipeName;
    private final ElementsCollection statusLabels;
    private final SelenideElement infoButton;
    private final SelenideElement payButton;
    private final SelenideElement cancelButton;

    public OrderKanbanCard(SelenideElement root) {
        this.root = root;
        this.recipeName = root.$("h4");
        this.statusLabels = root.$$x(".//span[contains(@class, 'arcade-pixel')]");
        this.infoButton = root.$x(".//button[contains(., 'INFO')]");
        this.payButton = root.$x(".//button[contains(., 'PAY')]");
        this.cancelButton = root.$x(".//button[contains(., 'CANCEL')]");
    }

    @Step("Проверить статус заказа: {expected}")
    public OrderKanbanCard shouldHaveStatus(OrderUiStatus expected) {
        statusLabels.findBy(Condition.exactText(expected.label()))
                .shouldBe(Condition.visible);
        return this;
    }

    @Step("Проверить название рецепта: {expectedName}")
    public OrderKanbanCard shouldHaveRecipeName(String expectedName) {
        recipeName.shouldHave(Condition.exactText(expectedName));
        return this;
    }

    @Step("Открыть детали заказа")
    public OrderDetailsModal openDetails() {
        infoButton.shouldBe(Condition.enabled).click();
        return new OrderDetailsModal().shouldBeOpened();
    }

    @Step("Оплатить заказ")
    public void pay() {
        payButton.shouldBe(Condition.enabled).click();
    }

    @Step("Отменить заказ")
    public void cancel() {
        cancelButton.shouldBe(Condition.enabled).click();
    }
}
