package com.shawarmashop.tests.ui.pages;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.shawarmashop.tests.ui.components.OrderKanbanCard;
import com.shawarmashop.tests.ui.model.OrderUiStatus;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.$x;

public class OrdersPage extends BasePage<OrdersPage> {

    private static final Duration CARD_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration FILTER_TIMEOUT = Duration.ofSeconds(5);

    private final ElementsCollection cards = $$("main article.arcade-card");
    private final SelenideElement filtersToggle = $x("//button[contains(@class, 'arcade-btn') and normalize-space(.)='FILTERS']");
    private final SelenideElement statusSelect = $x("//label[normalize-space(.)='STATUS']/following::select[1]");
    private final SelenideElement minPriceInput = $x("//label[normalize-space(.)='MIN $']/following::input[1]");

    @Step("Открыть страницу заказов")
    public OrdersPage openOrders() {
        return open("/orders").shouldBeOpened();
    }

    @Step("Открыть фильтры заказов")
    public OrdersPage openFilters() {
        if (!statusSelect.isDisplayed()) {
            filtersToggle.shouldBe(Condition.enabled).click();
        }
        statusSelect.shouldBe(Condition.visible, FILTER_TIMEOUT);
        return this;
    }

    @Step("Фильтровать заказы по статусу: {status}")
    public OrdersPage filterByStatus(OrderUiStatus status) {
        openFilters();
        statusSelect.selectOption(status.label());
        return this;
    }

    @Step("Установить минимальную цену: {value}")
    public OrdersPage setMinPrice(int value) {
        openFilters();
        minPriceInput.setValue(String.valueOf(value));
        return this;
    }

    @Step("Найти заказ #{id}")
    public OrderKanbanCard cardById(int id) {
        String regex = orderIdPattern(id);
        SelenideElement root = cards
                .findBy(Condition.matchText(regex))
                .shouldBe(Condition.visible, Duration.ofSeconds(10));
        return new OrderKanbanCard(root);
    }

    @Step("Проверить заказ #{id} со статусом {status}")
    public OrdersPage shouldShowOrder(int id, OrderUiStatus status) {
        cardById(id).shouldHaveStatus(status);
        return this;
    }

    @Step("Проверить отсутствие заказа #{id}")
    public OrdersPage shouldNotShowOrder(int id) {
        String regex = orderIdPattern(id);
        cards.filterBy(Condition.matchText(regex))
                .shouldBe(CollectionCondition.empty);
        return this;
    }

    private String orderIdPattern(int id) {
        return String.format("#%03d", id);
    }
}
