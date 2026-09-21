package com.shawarmashop.tests.rest.clients;


import com.fasterxml.jackson.core.type.TypeReference;
import com.shawarmashop.tests.auth.ApiClient;
import com.shawarmashop.tests.dto.payments.IdempotencyKey;
import com.shawarmashop.tests.dto.payments.PayRequest;
import com.shawarmashop.tests.dto.payments.PaymentResponse;
import com.shawarmashop.tests.dto.payments.WebhookRequest;
import com.shawarmashop.tests.rest.ApiResult;
import com.shawarmashop.tests.rest.BaseClient;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.util.List;

import static io.restassured.RestAssured.given;

public class PaymentClient extends BaseClient {

    private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";
    private static final String PAY_PATH = "/orders/{id}/pay";
    private static final String PAYMENTS_BY_ORDER = "/orders/{id}/payments";
    private static final String WEBHOOK_PATH = "/webhooks/payments";

    public PaymentClient(ApiClient client) {
        super(client);
    }

    @Step("POST /orders/{0}/pay (Idempotency-Key={2})")
    public ApiResult<PaymentResponse> pay(long orderId, PayRequest request, IdempotencyKey idempotencyKey) {
        Response response =  given(spec())
                .pathParam("id", orderId)
                .header(IDEMPOTENCY_HEADER, idempotencyKey.getValue())
                .body(request)
                .post(PAY_PATH);

        return ApiResult.from(response, PaymentResponse.class);
    }

    @Step("GET /orders/{0}/payments")
    public ApiResult<List<PaymentResponse>> list(long orderId) {
        Response response = given(spec())
                .pathParam("id", orderId)
                .get(PAYMENTS_BY_ORDER);

        return ApiResult.from(response, new TypeReference<>() {
        });
    }

    @Step("POST /webhooks/payments")
    public ApiResult<Void> sendWebhook(WebhookRequest request) {
        Response response = given(publicSpec())
                .body(request)
                .post(WEBHOOK_PATH);

        return ApiResult.from(response, Void.class);
    }
}
