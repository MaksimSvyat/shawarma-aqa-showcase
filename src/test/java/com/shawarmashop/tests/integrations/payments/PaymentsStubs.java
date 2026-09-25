package com.shawarmashop.tests.integrations.payments;

import com.shawarmashop.tests.integrations.payments.dto.*;
import com.shawarmashop.tests.integrations.wiremock.WireMockAdminClient;
import com.shawarmashop.tests.integrations.wiremock.WireMockStubBase;
import com.shawarmashop.tests.support.Json;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.shawarmashop.tests.integrations.wiremock.StubBuilder.post;

public class PaymentsStubs extends WireMockStubBase  {

    private static final String CHARGE_PATH = "/payments/charge";
    private static final String CURRENCY_RUB = "RUB";
    private static final BigDecimal DEFAULT_FEE = new BigDecimal("5.0");

    public PaymentsStubs(WireMockAdminClient admin) {
        super(admin);
    }

    public void responseSuccess(String tnxId) {
        admin.addMapping(post(CHARGE_PATH)
                .willReturnJson(200, ChargeResponseDto.builder()
                        .txnId(tnxId)
                        .status(ChargeStatus.SUCCEEDED)
                        .processedAt(Instant.now())
                        .fee(new ChargeMoney(DEFAULT_FEE, CURRENCY_RUB))
                        .message("OK")
                        .build()));
    }

    /** Без priority — для случаев, когда нет конфликтующих маппингов. */
    public void responseFailed(String reason) {
        admin.addMapping(post(CHARGE_PATH)
                .willReturnJson(200, ChargeResponseDto.builder()
                        .txnId(shortId())
                        .status(ChargeStatus.FAILED)
                        .processedAt(Instant.now())
                        .fee(new ChargeMoney(BigDecimal.ZERO, CURRENCY_RUB))
                        .message(reason)
                        .build()));
    }

    public void respondPending(String txnId) {
        admin.addMapping(post(CHARGE_PATH)
                .withPriority(1)
                .willReturnJson(200, ChargeResponseDto.builder()
                        .txnId(txnId)
                        .status(ChargeStatus.PENDING)
                        .processedAt(Instant.now())
                        .fee(new ChargeMoney(BigDecimal.ZERO, CURRENCY_RUB))
                        .message("Awaiting webhook")
                        .build()));
    }

    /** С priority=1 — переопределяет дефолтный success-stub. */
    public void respondFailed(String reason) {
        admin.addMapping(post(CHARGE_PATH)
                .withPriority(1)
                .willReturnJson(200, ChargeResponseDto.builder()
                        .txnId("txn_failed_" + shortId())
                        .status(ChargeStatus.FAILED)
                        .processedAt(Instant.now())
                        .fee(new ChargeMoney(BigDecimal.ZERO, CURRENCY_RUB))
                        .message(reason)
                        .build()));
    }


    public void response500() {
        admin.addMapping(post(CHARGE_PATH)
                .willReturnJson(503, PaymentsErrorDto.builder()
                        .error("service_unavailable")
                        .message("Payments provider is temporarily unavailable")
                        .attempt(1)
                        .build()));
    }

    public List<ChargeRequestDto> recordedRequests(){
        Map<String, Object> filter = Map.of("method", "POST", "url", CHARGE_PATH);
        return admin.findRequests(filter).stream()
                .map(x->x.get("body"))
                .filter(Objects::nonNull)
                .map(x-> Json.fromJson(x.toString(), ChargeRequestDto.class))
                .toList();
    }
}

