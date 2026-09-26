package com.shawarmashop.tests.ui.components;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$;

public class OrderDetailsModal {

    private static final Duration MODAL_TIMEOUT = Duration.ofSeconds(5);

    private final SelenideElement root = $("div.arcade-modal");
    private final SelenideElement heading = root.$("h2.arcade-pixel");

    @Step("Дождаться открытия деталей заказа")
    public OrderDetailsModal shouldBeOpened() {
        root.shouldBe(Condition.visible, MODAL_TIMEOUT);
        return this;
    }

    @Step("Проверить, что открыты детали заказа #{orderId}")
    public OrderDetailsModal shouldHaveOrderId(long orderId) {
        heading.shouldHave(Condition.exactText("ORDER #%03d".formatted(orderId)));
        return this;
    }
}
