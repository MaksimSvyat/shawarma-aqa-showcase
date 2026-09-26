package com.shawarmashop.tests.e2e.showcase.mocks;

import com.shawarmashop.tests.auth.ApiClient;
import com.shawarmashop.tests.auth.Users;
import com.shawarmashop.tests.db.repository.OrderRepository;
import com.shawarmashop.tests.db.rows.OrderRow;
import com.shawarmashop.tests.dto.orders.OrderResponse;
import com.shawarmashop.tests.e2e.support.helpers.OrderHelper;
import com.shawarmashop.tests.e2e.support.helpers.PaymentHelper;
import com.shawarmashop.tests.extensions.DbTest;
import com.shawarmashop.tests.extensions.IntegrationsTest;
import com.shawarmashop.tests.extensions.KafkaTest;
import com.shawarmashop.tests.integrations.IntegrationStubs;
import com.shawarmashop.tests.integrations.bread.dto.BreadBakery;
import com.shawarmashop.tests.integrations.bread.dto.BreadConfirmationDto;
import com.shawarmashop.tests.integrations.bread.dto.BreadOrderDto;
import com.shawarmashop.tests.kafka.KafkaTestBus;
import com.shawarmashop.tests.kafka.OrderEventsTopic;
import com.shawarmashop.tests.kafka.events.OrderEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationsTest
@KafkaTest
@DbTest
@DisplayName("Showcase: gRPC bread bakery — happy + UNAVAILABLE")
class GrpcBreadShowcaseTest {

    private static final int RECIPE_ID = 1;

    private final ApiClient owner = ApiClient.asUser(Users.OWNER);
    private final OrderRepository orderDb = new OrderRepository();
    private final OrderHelper orderHelper = new OrderHelper(owner);
    private final PaymentHelper paymentHelper = new PaymentHelper(owner);

    @Test
    @DisplayName("happy: pay → пекарня подтверждает → бэк дёргает gRPC ровно один раз")
    void breadHappyPath(IntegrationStubs stubs) {
        BreadConfirmationDto confirmation = BreadConfirmationDto.builder()
                .batchId("B-" + UUID.randomUUID().toString().substring(0, 8))
                .readyInSec(400)
                .pricePerUnit(2500)
                .totalPrice(25000)
                .bakery(BreadBakery.builder().branch("test-branch").masterChef("Showcase Chef").build())
                .build();
        stubs.bread().respondWith(confirmation);
        stubs.payments().responseSuccess("txn_bread_" + UUID.randomUUID());

        OrderResponse order = orderHelper.createOrder(RECIPE_ID, 1, "CARD");

        paymentHelper.createPayment(order.getId(), "CARD");

        orderDb.awaitStatus(order.getId(), "PREPARING", Duration.ofSeconds(15));

        // breadBatchId захвачен в строке заказа.
        assertThat(orderDb.findById(order.getId()).orElseThrow().getBreadBatchId())
                .isEqualTo(confirmation.getBatchId());

        // Бэк отправил пекарне ровно один gRPC-вызов.
        List<BreadOrderDto> calls = stubs.bread().recordedCalls();
        assertThat(calls).hasSize(1);
        assertThat(calls.getFirst().getOrderRef()).isEqualTo("order-" + order.getId());
    }

    @Test
    @DisplayName(
            "UNAVAILABLE: /pay → 202, заказ остаётся PAID, BREAD_RESERVATION_FAILED публикуется в Kafka"
    )
    void breadUnavailable(IntegrationStubs stubs, KafkaTestBus kafka) {
        OrderEventsTopic orderEvents = kafka.orders();

        stubs.bread().respondUnavailable();
        stubs.payments().responseSuccess("txn_bread_unavail_" + UUID.randomUUID());

        OrderResponse order = orderHelper.createOrder(RECIPE_ID, 1, "CARD");

        // /pay отвечает за оплату, а не за хлеб.
        paymentHelper.createPayment(order.getId(), "CARD");

        // Scheduler подождёт около 5 секунд,
        // попробует вызвать пекарню и опубликует событие.
        OrderEvent failedEvent = orderEvents.waitBreadReservationFailed(order.getId());

        assertThat(failedEvent.getOrderId()).isEqualTo(order.getId());
        assertThat(failedEvent.getReason()).contains("UNAVAILABLE");

        // После ошибки хлеба заказ остаётся оплаченным.
        OrderRow paidOrder = orderDb.findById(order.getId()).orElseThrow();

        assertThat(paidOrder.getStatus()).isEqualTo("PAID");
        assertThat(paidOrder.getBreadBatchId()).isNull();

        // Scheduler может сделать больше одной попытки,
        // поэтому нельзя проверять hasSize(1).
        assertThat(stubs.bread().recordedCalls())
                .anySatisfy(call ->
                        assertThat(call.getOrderRef())
                                .isEqualTo("order-" + order.getId())
                );
    }
}
