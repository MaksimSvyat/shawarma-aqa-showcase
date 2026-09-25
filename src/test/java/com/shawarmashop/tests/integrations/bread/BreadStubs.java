package com.shawarmashop.tests.integrations.bread;

import com.shawarmashop.tests.integrations.bread.dto.BreadConfirmationDto;
import com.shawarmashop.tests.integrations.bread.dto.BreadErrorDto;
import com.shawarmashop.tests.integrations.bread.dto.BreadOrderDto;
import com.shawarmashop.tests.integrations.wiremock.WireMockAdminClient;
import com.shawarmashop.tests.integrations.wiremock.WireMockStubBase;
import com.shawarmashop.tests.support.Json;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.shawarmashop.tests.integrations.wiremock.StubBuilder.post;

public class BreadStubs extends WireMockStubBase {

    private final static String GRPC_PATH = "/BreadBakery/orderBatch";

    public BreadStubs(WireMockAdminClient admin) {
        super(admin);
    }

    public void respondWith(BreadConfirmationDto confirmationDto) {
        admin.addMapping(post(GRPC_PATH).willReturnJson(200, confirmationDto));
    }

    public void respondUnavailable() {
        admin.addMapping(post(GRPC_PATH).willReturnJson(502, BreadErrorDto.builder()
                .error("bad_gateway")
                .message("Bread bakery service in unavailable")
                .occurredAt(Instant.now())
                .build()));
    }

    public List<BreadOrderDto> recordedCalls(){
        Map<String, Object> filter = Map.of("method", "POST", "url", GRPC_PATH);
        return admin.findRequests(filter).stream()
                .map(x->x.get("body"))
                .filter(Objects::nonNull)
                .map(x-> Json.fromJson(x.toString(), BreadOrderDto.class))
                .toList();
    }

}
