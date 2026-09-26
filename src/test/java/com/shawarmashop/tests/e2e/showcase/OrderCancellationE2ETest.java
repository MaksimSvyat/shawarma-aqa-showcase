package com.shawarmashop.tests.e2e.showcase;

import com.shawarmashop.tests.auth.ApiClient;
import com.shawarmashop.tests.auth.Users;
import com.shawarmashop.tests.dto.orders.OrderResponse;
import com.shawarmashop.tests.e2e.support.helpers.OrderHelper;
import com.shawarmashop.tests.e2e.support.helpers.PaymentHelper;
import com.shawarmashop.tests.extensions.DbTest;
import com.shawarmashop.tests.extensions.IntegrationsTest;
import com.shawarmashop.tests.extensions.KafkaTest;
import com.shawarmashop.tests.integrations.IntegrationStubs;
import com.shawarmashop.tests.kafka.KafkaTestBus;
import com.shawarmashop.tests.kafka.OrderEventsTopic;
import com.shawarmashop.tests.kafka.events.OrderEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationsTest
@KafkaTest
@DbTest
@DisplayName("E2E: отмена заказа")
class OrderCancellationE2ETest {

    private static final int RECIPE_ID = 1;
    private static final Duration PAID_STATUS_TIMEOUT = Duration.ofSeconds(3);

    private final ApiClient owner = ApiClient.asUser(Users.OWNER);
    private final OrderHelper orderHelper = new OrderHelper(owner);
    private final PaymentHelper paymentHelper = new PaymentHelper(owner);

    @Test
    @DisplayName("PENDING → CANCELLED + ORDER_CANCELLED")
    void cancelFromPending(KafkaTestBus kafka) {
        OrderEventsTopic orderEvents = kafka.orders();
        OrderResponse order = orderHelper.createOrder(RECIPE_ID, 1, "CARD");

        OrderResponse cancelled = owner.orders()
                .cancel(order.getId())
                .expect(200);

        assertThat(cancelled.getStatus()).isEqualTo("CANCELLED");

        OrderEvent cancelledOrderEvent = orderEvents.waitCancelled(order.getId());

        orderHelper.assertCancelledEvent(cancelledOrderEvent, order.getId());
    }

    @Test
    @DisplayName("PAID → CANCELLED + ORDER_CANCELLED")
    void cancelFromPaid(IntegrationStubs stubs, KafkaTestBus kafka) {
        OrderEventsTopic orderEvents = kafka.orders();

        String txnId = "txn_" + UUID.randomUUID() + "cancel_paid";
        stubs.payments().respondPending(txnId);

        OrderResponse order = orderHelper.createOrder(RECIPE_ID, 1, "CARD");

        paymentHelper.createPayment(order.getId(), "CARD");

        orderHelper.moveToPaid(order.getId(), txnId, PAID_STATUS_TIMEOUT);

        OrderResponse cancelled = owner.orders()
                .cancel(order.getId())
                .expect(200);

        assertThat(cancelled.getStatus()).isEqualTo("CANCELLED");

        OrderEvent cancelledOrderEvent = orderEvents.waitCancelled(order.getId());

        orderHelper.assertCancelledEvent(cancelledOrderEvent, order.getId());
    }

    @Test
    @DisplayName("повторная отмена CANCELLED-заказа возвращает 400")
    void cancelTwiceIs400() {
        OrderResponse order = orderHelper.createOrder(RECIPE_ID, 1, "CARD");

        owner.orders()
                .cancel(order.getId())
                .expect(200);

        owner.orders()
                .cancel(order.getId())
                .assertStatus(400);
    }
}
