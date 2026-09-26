package com.shawarmashop.tests.e2e.showcase;

import com.shawarmashop.tests.auth.ApiClient;
import com.shawarmashop.tests.auth.Users;
import com.shawarmashop.tests.db.factories.Inserts;
import com.shawarmashop.tests.db.repository.IngredientRepository;
import com.shawarmashop.tests.db.repository.OrderRepository;
import com.shawarmashop.tests.db.repository.PaymentRepository;
import com.shawarmashop.tests.db.rows.IngredientRow;
import com.shawarmashop.tests.db.rows.OrderRow;
import com.shawarmashop.tests.db.rows.PaymentRow;
import com.shawarmashop.tests.db.rows.RecipeRow;
import com.shawarmashop.tests.dto.orders.OrderResponse;
import com.shawarmashop.tests.dto.payments.IdempotencyKey;
import com.shawarmashop.tests.dto.payments.PaymentResponse;
import com.shawarmashop.tests.e2e.support.helpers.IngredientHelper;
import com.shawarmashop.tests.e2e.support.helpers.OrderHelper;
import com.shawarmashop.tests.e2e.support.helpers.PaymentHelper;
import com.shawarmashop.tests.e2e.support.helpers.RecipeHelper;
import com.shawarmashop.tests.extensions.DbTest;
import com.shawarmashop.tests.extensions.IntegrationsTest;
import com.shawarmashop.tests.extensions.KafkaTest;
import com.shawarmashop.tests.integrations.IntegrationStubs;
import com.shawarmashop.tests.integrations.bread.dto.BreadBakery;
import com.shawarmashop.tests.integrations.bread.dto.BreadConfirmationDto;
import com.shawarmashop.tests.kafka.KafkaTestBus;
import com.shawarmashop.tests.kafka.OrderEventsTopic;
import com.shawarmashop.tests.kafka.events.OrderEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DbTest
@KafkaTest
@IntegrationsTest
@DisplayName("E2E: заказ PENDING → PAID → PREPARING → DONE")
class OrderLifecycleE2ETest {

    private static final Duration LIFECYCLE_TIMEOUT = Duration.ofSeconds(20);

    private final ApiClient owner = ApiClient.asUser(Users.OWNER);

    private final OrderRepository orderDb = new OrderRepository();
    private final PaymentRepository paymentDb = new PaymentRepository();
    private final IngredientRepository ingredientDb = new IngredientRepository();

    private final OrderHelper orderHelper = new OrderHelper(owner);
    private final RecipeHelper recipeHelper = new RecipeHelper();
    private final IngredientHelper ingredientHelper = new IngredientHelper();
    private final PaymentHelper paymentHelper = new PaymentHelper(owner);

    @DisplayName("заказ проходит полный жизненный цикл во всех каналах")
    @Test
    void fullLifecycle(IntegrationStubs stubs, KafkaTestBus kafka) {
        int startStock = 500;
        int qtyNeededPerPortion = 100;

        RecipeRow recipe = recipeHelper.createRecipeWithPrice("E2E_LIFECYCLE", 250.00);
        IngredientRow ingredient = ingredientHelper.createIngredient("E2E_MEAT", "REST", startStock);

        Inserts.recipeIngredient(recipe.getId(), ingredient.getId(), qtyNeededPerPortion);

        String txnId = "txn_" + UUID.randomUUID() + "cancel_paid";
        String breadBatchId = "B-E2E-" + System.nanoTime();

        stubs.payments().responseSuccess(txnId);

        stubs.bread().respondWith(BreadConfirmationDto.builder()
                .batchId(breadBatchId)
                .readyInSec(5)
                .pricePerUnit(2000)
                .totalPrice(2000)
                .bakery(BreadBakery.builder().masterChef("James").branch("east").build())
                .build()
        );

        OrderEventsTopic orderEvents = kafka.orders();

        // ============================================================
        // 1. Создание заказа
        // ============================================================

        OrderResponse created = orderHelper.createOrder(recipe.getId(), 1, "CARD");
        int orderId = created.getId();

        assertThat(created.getStatus()).isEqualTo("PENDING");
        assertThat(created.getTotalPrice()).isEqualByComparingTo(250.00);
        assertThat(created.getPayment()).isNull();

        OrderEvent placed = orderEvents.waitPlaced(orderId);

        assertThat(placed.getOrderId()).isEqualTo(orderId);
        assertThat(placed.getRecipeId()).isEqualTo(recipe.getId());
        assertThat(placed.getQty()).isEqualTo(1);
        assertThat(placed.getTotalPrice()).isEqualTo(250);

        // ============================================================
        // 2. Оплата
        // ============================================================

        IdempotencyKey idempotencyKey = IdempotencyKey.random();
        PaymentResponse payment = paymentHelper.createPayment(orderId, "CARD", idempotencyKey);

        assertThat(payment.getStatus()).isEqualTo("SUCCEEDED");
        assertThat(payment.getTxnId()).isEqualTo(txnId);

        // ============================================================
        // 3. Бронирование хлеба и переход в PREPARING
        // ============================================================

        orderDb.awaitStatus(orderId, "PREPARING", LIFECYCLE_TIMEOUT);

        OrderRow preparing = orderDb.findById(orderId).orElseThrow();

        assertThat(preparing.getBreadBatchId()).isEqualTo(breadBatchId);
        assertThat(preparing.getEtaAt()).isNotNull();

        OrderEvent paid = orderEvents.waitPaid(orderId, LIFECYCLE_TIMEOUT);

        assertThat(paid.getOrderId()).isEqualTo(orderId);
        assertThat(paid.getTxnId()).isEqualTo(txnId);
        assertThat(paid.getEtaAt()).isNotNull();

        assertThat(stubs.bread().recordedCalls())
                .filteredOn(call -> ("order-" + orderId).equals(call.getOrderRef()))
                .singleElement()
                .satisfies(call ->
                        assertThat(call.getOrderRef()).isEqualTo("order-" + orderId)
                );

        // ============================================================
        // 4. Завершение заказа
        // ============================================================

        orderDb.awaitStatus(orderId, "DONE", LIFECYCLE_TIMEOUT);

        OrderRow done = orderDb.findById(orderId).orElseThrow();

        assertThat(done.getCompletedAt()).isNotNull();

        OrderEvent doneEvent = orderEvents.waitDone(orderId, LIFECYCLE_TIMEOUT);

        assertThat(doneEvent.getOrderId()).isEqualTo(orderId);
        assertThat(doneEvent.getRecipeName()).isEqualTo(recipe.getName());
        assertThat(doneEvent.getCompletedAt()).isNotNull();

        // ============================================================
        // 5. Проверка списания ингредиента
        // ============================================================

        IngredientRow actualIngredient = ingredientDb
                .findById(ingredient.getId())
                .orElseThrow();

        assertThat(actualIngredient.getOnHand())
                .isEqualTo(startStock - qtyNeededPerPortion);

        // ============================================================
        // 6. Финальные REST-проекции
        // ============================================================

        OrderResponse fetched = owner.orders().get(orderId).success();
        assertThat(fetched.getStatus()).isEqualTo("DONE");
        assertThat(fetched.getEtaAt()).isNotNull();

        List<PaymentResponse> paymentHistory = owner.payments()
                .list(orderId)
                .success();

        assertThat(paymentHistory)
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.getStatus()).isEqualTo("SUCCEEDED");
                    assertThat(item.getTxnId()).isEqualTo(txnId);
                });

        List<PaymentRow> dbPayments = paymentDb.findByOrderId(orderId);

        assertThat(dbPayments)
                .singleElement()
                .satisfies(item ->
                        assertThat(item.getIdempotencyKey()).isEqualTo(idempotencyKey.getValue())
                );
    }

}

