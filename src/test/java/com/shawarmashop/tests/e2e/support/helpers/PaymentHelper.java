package com.shawarmashop.tests.e2e.support.helpers;

import com.shawarmashop.tests.auth.ApiClient;
import com.shawarmashop.tests.db.repository.PaymentRepository;
import com.shawarmashop.tests.db.rows.PaymentRow;
import com.shawarmashop.tests.db.scope.TestDbScope;
import com.shawarmashop.tests.dto.payments.IdempotencyKey;
import com.shawarmashop.tests.dto.payments.PayRequest;
import com.shawarmashop.tests.dto.payments.PaymentResponse;
import io.qameta.allure.Step;

public class PaymentHelper {

    private static final String PAYMENTS_TABLE = "payments";

    private final PaymentRepository paymentDb;
    private final ApiClient client;

    public PaymentHelper(ApiClient client) {
        this.client = client;
        this.paymentDb = new PaymentRepository();
    }

    @Step("Создание платежа: orderId={0}, method={1}")
    public PaymentResponse createPayment(long orderId, String method) {
        return createPayment(orderId, method, IdempotencyKey.random());
    }

    @Step("Создание платежа: orderId={0}, method={1}, idempotencyKey={2}")
    public PaymentResponse createPayment(long orderId, String method, IdempotencyKey idempotencyKey) {
        PaymentResponse payment = client.payments()
                .pay(orderId, new PayRequest(method), idempotencyKey)
                .expect(202);

        trackPayment(payment.getId());

        return payment;
    }

    @Step("Поиск и трекинг платежа: idempotencyKey={0}")
    public PaymentRow findAndTrackPayment(IdempotencyKey idempotencyKey) {
        PaymentRow payment = paymentDb
                .findByIdempotencyKey(idempotencyKey.getValue())
                .orElseThrow(() -> new AssertionError("Платёж с ключом " + idempotencyKey.getValue() + " не найден"));

        trackPayment(payment.getId());

        return payment;
    }

    private void trackPayment(long paymentId) {
        TestDbScope.current().track(PAYMENTS_TABLE, paymentId);
    }
}
