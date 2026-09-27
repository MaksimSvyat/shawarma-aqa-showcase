package com.shawarmashop.tests.ui.tests;

import com.codeborne.selenide.Condition;
import com.shawarmashop.tests.dto.orders.OrderResponse;
import com.shawarmashop.tests.e2e.support.helpers.OrderHelper;
import com.shawarmashop.tests.extensions.DbTest;
import com.shawarmashop.tests.ui.BaseUITest;
import com.shawarmashop.tests.ui.components.OrderModal;
import com.shawarmashop.tests.ui.extensions.UseSelenideProxy;
import com.shawarmashop.tests.ui.model.OrderUiStatus;
import com.shawarmashop.tests.ui.pages.MenuPage;
import com.shawarmashop.tests.ui.pages.OrdersPage;
import io.netty.handler.codec.http.HttpMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DbTest
public class CreateOrderUITest extends BaseUITest {

    private final MenuPage menuPage = new MenuPage();
    private final OrdersPage ordersPage = new OrdersPage();
    private final OrderHelper orderHelper = new OrderHelper(owner);

    @Test
    @DisplayName("Создание заказа через меню отображает его в канбане со статусом PENDING")
    @UseSelenideProxy
    public void createOrderViaMenuShowsInKanban() {
        String recipeName = "BBQ Курица";
        defaultAuth();

        OrderModal orderModal = menuPage
                .openMenu()
                .startOrderFor(recipeName);

        OrderResponse orderResponse = interceptResponse(
                HttpMethod.POST,
                "/api/v1/orders",
                OrderResponse.class,
                orderModal::submit);

        orderHelper.trackOrder(orderResponse.getId());

        ordersPage.openOrders()
                .cardById(orderResponse.getId())
                .shouldHaveStatus(OrderUiStatus.PENDING)
                .getRecipeName().shouldHave(Condition.partialText(recipeName));
    }
}
