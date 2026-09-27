package com.shawarmashop.tests.ui.tests;

import com.shawarmashop.tests.db.scope.TestDbScope;
import com.shawarmashop.tests.db.repository.OrderRepository;
import com.shawarmashop.tests.db.repository.PaymentRepository;
import com.shawarmashop.tests.db.rows.PaymentRow;
import com.shawarmashop.tests.dto.orders.CreateOrderRequest;
import com.shawarmashop.tests.dto.orders.OrderResponse;
import com.shawarmashop.tests.extensions.DbTest;
import com.shawarmashop.tests.extensions.IntegrationsTest;
import com.shawarmashop.tests.integrations.IntegrationStubs;
import com.shawarmashop.tests.ui.BaseUITest;
import com.shawarmashop.tests.ui.model.OrderUiStatus;
import com.shawarmashop.tests.ui.pages.OrdersPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationsTest
@DbTest
@DisplayName("UI · заказы")
class OrdersUiTest extends BaseUITest {

    private static final int RECIPE_ID = 1;
    private static final Duration PAYMENT_TIMEOUT = Duration.ofSeconds(10);

    private final OrdersPage ordersPage = new OrdersPage();
    private final OrderRepository orderDb = new OrderRepository();
    private final PaymentRepository paymentDb = new PaymentRepository();

    @BeforeEach
    void initPageAndLogin() {
        defaultAuth();
    }

    @Test
    @DisplayName("PENDING-заказ отображается в канбане")
    void pendingOrderRendersInKanban() {
        OrderResponse created = createOrder("CASH");

        ordersPage.openOrders()
                .shouldShowOrder(created.getId(), OrderUiStatus.PENDING);
    }

    @Test
    @DisplayName("CANCEL переводит PENDING-заказ в CANCELLED")
    void cancelPendingOrderViaUi() {
        OrderResponse created = createOrder("CASH");

        ordersPage.openOrders()
                .cardById(created.getId())
                .cancel();

        ordersPage.shouldShowOrder(created.getId(), OrderUiStatus.CANCELLED);

        String status = owner.orders()
                .get(created.getId())
                .success()
                .getStatus();

        assertThat(status).isEqualTo("CANCELLED");
    }

    @Test
    @DisplayName("INFO открывает детали выбранного заказа")
    void infoOpensDetailsModal() {
        OrderResponse created = createOrder("CASH");

        ordersPage.openOrders()
                .cardById(created.getId())
                .openDetails()
                .shouldHaveOrderId(created.getId());
    }

    @Test
    @DisplayName("PAY показывает PAYMENT OK и оплачивает заказ")
    void payPendingOrderViaUi(IntegrationStubs stubs) {
        String txnId = "txn_" + UUID.randomUUID() + "ui_pay";

        stubs.payments().responseSuccess(txnId);

        OrderResponse created = createOrder("CARD");

        ordersPage.openOrders()
                .cardById(created.getId())
                .pay();

        ordersPage.expectToast("PAYMENT OK");

        orderDb.awaitStatus(created.getId(), "PAID", PAYMENT_TIMEOUT);

        PaymentRow payment = findSinglePayment(created.getId());

        assertThat(payment.getTxnId()).isEqualTo(txnId);
        assertThat(payment.getStatus()).isEqualTo("SUCCEEDED");
    }

    @Test
    @DisplayName("фильтр CANCELLED показывает отменённый заказ и скрывает PENDING")
    void filterByStatusHidesOthers() {
        OrderResponse pending = createOrder("CASH");
        OrderResponse cancelled = createOrder("CASH");

        owner.orders()
                .cancel(cancelled.getId())
                .assertStatus(200);

        ordersPage.openOrders()
                .filterByStatus(OrderUiStatus.CANCELLED)
                .shouldShowOrder(cancelled.getId(), OrderUiStatus.CANCELLED)
                .shouldNotShowOrder(pending.getId());
    }

    private OrderResponse createOrder(String paymentMethod) {
        CreateOrderRequest body = CreateOrderRequest.of(RECIPE_ID, 1, paymentMethod);

        OrderResponse order = owner.orders()
                .create(body)
                .success();

        TestDbScope.current().track("orders", order.getId());
        return order;
    }

    private PaymentRow findSinglePayment(long orderId) {
        List<PaymentRow> payments = paymentDb.findByOrderId(orderId);

        assertThat(payments)
                .as("Платежи заказа id=%s", orderId)
                .hasSize(1);

        PaymentRow payment = payments.getFirst();
        TestDbScope.current().track("payments", payment.getId());
        return payment;
    }
}
