package com.shawarmashop.tests.e2e.support.helpers;

import com.shawarmashop.tests.auth.ApiClient;
import com.shawarmashop.tests.db.repository.OrderRepository;
import com.shawarmashop.tests.db.scope.TestDbScope;
import com.shawarmashop.tests.dto.orders.CreateOrderRequest;
import com.shawarmashop.tests.dto.orders.OrderResponse;
import com.shawarmashop.tests.dto.payments.WebhookRequest;
import com.shawarmashop.tests.kafka.events.OrderEvent;
import io.qameta.allure.Step;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class OrderHelper {

    public static final String CANCELLED_REASON = "user-cancelled";
    private static final String ORDERS_TABLE = "orders";

    private final ApiClient client;
    private final OrderRepository orderDb;

    public OrderHelper(ApiClient client) {
        this.client = client;
        this.orderDb = new OrderRepository();
    }

    @Step("Создание заказа: recipeId={0}, qty={1}, method={2}")
    public OrderResponse createOrder(int recipeId, int qty, String method) {
        CreateOrderRequest body = CreateOrderRequest.of(recipeId, qty, method);
        OrderResponse order = client.orders().create(body).expect(201);
        trackOrder(order.getId());
        return order;
    }

    @Step("Перевод заказа в PAID: orderId={0}, txnId={1}")
    public void moveToPaid(long orderId, String txnId, Duration timeout) {
        WebhookRequest body = new WebhookRequest(txnId, "SUCCEEDED", Instant.now(), null);
        client.payments().sendWebhook(body).assertStatus(204);
        orderDb.awaitStatus(orderId, "PAID", timeout);
    }

    @Step("Проверка статуса заказа: orderId={0} должен быть PENDING")
    public void assertOrderPending(int orderId) {
        String status = client.orders().get(orderId).success().getStatus();
        assertThat(status).isEqualTo("PENDING");
    }

    @Step("Проверка события ORDER_CANCELLED: orderId={1}")
    public void assertCancelledEvent(OrderEvent event, long orderId) {
        assertThat(event.getOrderId()).isEqualTo(orderId);
        assertThat(event.getReason()).isEqualTo(CANCELLED_REASON);
    }

    public void trackOrder(long orderId) {
        TestDbScope.current().track(ORDERS_TABLE, orderId);
    }
}
