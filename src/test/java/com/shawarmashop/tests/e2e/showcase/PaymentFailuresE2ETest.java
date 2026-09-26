package com.shawarmashop.tests.e2e.showcase;

import com.shawarmashop.tests.auth.ApiClient;
import com.shawarmashop.tests.auth.Users;
import com.shawarmashop.tests.db.repository.PaymentRepository;
import com.shawarmashop.tests.db.rows.PaymentRow;
import com.shawarmashop.tests.dto.orders.OrderResponse;
import com.shawarmashop.tests.dto.payments.IdempotencyKey;
import com.shawarmashop.tests.dto.payments.PayRequest;
import com.shawarmashop.tests.dto.payments.WebhookRequest;
import com.shawarmashop.tests.e2e.support.helpers.OrderHelper;
import com.shawarmashop.tests.e2e.support.helpers.PaymentHelper;
import com.shawarmashop.tests.extensions.DbTest;
import com.shawarmashop.tests.extensions.IntegrationsTest;
import com.shawarmashop.tests.integrations.IntegrationStubs;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationsTest
@DbTest
@DisplayName("E2E: сбои оплаты")
class PaymentFailuresE2ETest {

    private static final int RECIPE_ID = 1;
    private static final Duration PAYMENT_STATUS_TIMEOUT = Duration.ofSeconds(5);

    private final ApiClient owner = ApiClient.asUser(Users.OWNER);
    private final PaymentRepository paymentDb = new PaymentRepository();
    private final OrderHelper orderHelper = new OrderHelper(owner);
    private final PaymentHelper paymentHelper = new PaymentHelper(owner);

    @Test
    @DisplayName("declined → 402, платёж FAILED, заказ остаётся PENDING")
    void declinedPaymentIs402(IntegrationStubs stubs) {
        String failureReason = "Insufficient funds";
        stubs.payments().respondFailed(failureReason);

        OrderResponse order = orderHelper.createOrder(RECIPE_ID, 1, "CARD");
        IdempotencyKey idempotencyKey = IdempotencyKey.random();

        owner.payments()
                .pay(order.getId(), new PayRequest("CARD"), idempotencyKey)
                .assertStatus(402);

        PaymentRow payment = paymentHelper.findAndTrackPayment(idempotencyKey);

        assertThat(payment.getStatus()).isEqualTo("FAILED");
        assertThat(payment.getFailureReason()).isEqualTo(failureReason);
        orderHelper.assertOrderPending(order.getId());
    }


    @Test
    @DisplayName("webhook FAILED переводит PENDING-платёж в FAILED")
    void webhookFailedKeepsOrderUnpaid(IntegrationStubs stubs) {
        String txnId = "txn_" + UUID.randomUUID() + "cancel_paid";
        stubs.payments().respondPending(txnId);

        OrderResponse order = orderHelper.createOrder(RECIPE_ID, 1, "CARD");
        IdempotencyKey idempotencyKey = IdempotencyKey.random();

        paymentHelper.createPayment(order.getId(), "CARD", idempotencyKey);

        orderHelper.assertOrderPending(order.getId());

        WebhookRequest body = new WebhookRequest(txnId, "FAILED", Instant.now(), null);
        owner.payments()
                .sendWebhook(body)
                .assertStatus(204);

        Awaitility.await("платёж txnId=" + txnId + " переходит в FAILED")
                .atMost(PAYMENT_STATUS_TIMEOUT)
                .pollInterval(Duration.ofMillis(250))
                .untilAsserted(() -> {
                    PaymentRow actual = paymentDb
                            .findByTxnId(txnId)
                            .orElseThrow();

                    assertThat(actual.getStatus()).isEqualTo("FAILED");
                });

        orderHelper.assertOrderPending(order.getId());
    }

    @Test
    @DisplayName("повтор с тем же ключом не создаёт второй платёж")
    void declinedReplayIsIdempotent(IntegrationStubs stubs) {
        stubs.payments().respondFailed("Card blocked");

        OrderResponse order = orderHelper.createOrder(RECIPE_ID, 1, "CARD");
        IdempotencyKey idempotencyKey = IdempotencyKey.random();

        owner.payments()
                .pay(order.getId(), new PayRequest("CARD"), idempotencyKey)
                .assertStatus(402);

        PaymentRow originalPayment = paymentHelper.findAndTrackPayment(idempotencyKey);

        owner.payments()
                .pay(order.getId(), new PayRequest("CARD"), idempotencyKey)
                .assertStatus(402);

        assertThat(paymentDb.findByOrderId(order.getId()))
                .singleElement()
                .satisfies(payment -> {
                    assertThat(payment.getId()).isEqualTo(originalPayment.getId());
                    assertThat(payment.getIdempotencyKey()).isEqualTo(idempotencyKey.getValue());
                    assertThat(payment.getStatus()).isEqualTo("FAILED");
                });
    }

    @Test
    @DisplayName("тот же Idempotency-Key для другого заказа → 409")
    void idempotencyKeyIsScopedToOrder(IntegrationStubs stubs) {
        String txnId = "txn_" + UUID.randomUUID() + "scope";
        stubs.payments().responseSuccess(txnId);

        OrderResponse firstOrder = orderHelper.createOrder(RECIPE_ID, 1, "CARD");
        OrderResponse secondOrder = orderHelper.createOrder(RECIPE_ID, 1, "CARD");

        IdempotencyKey idempotencyKey = IdempotencyKey.random();

        paymentHelper.createPayment(firstOrder.getId(), "CARD", idempotencyKey);

        owner.payments()
                .pay(secondOrder.getId(), new PayRequest("CARD"), idempotencyKey)
                .assertStatus(409);

        orderHelper.assertOrderPending(secondOrder.getId());

        assertThat(paymentDb.findByOrderId(firstOrder.getId()))
                .singleElement()
                .satisfies(actual -> {
                    assertThat(actual.getIdempotencyKey()).isEqualTo(idempotencyKey.getValue());
                    assertThat(actual.getTxnId()).isEqualTo(txnId);
                });

        assertThat(paymentDb.findByOrderId(secondOrder.getId())).isEmpty();
    }

}
