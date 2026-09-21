package com.shawarmashop.tests.rest.clients;

import com.fasterxml.jackson.core.type.TypeReference;
import com.shawarmashop.tests.auth.ApiClient;
import com.shawarmashop.tests.dto.events.EventLogResponse;
import com.shawarmashop.tests.rest.ApiResult;
import com.shawarmashop.tests.rest.BaseClient;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.List;

import static io.restassured.RestAssured.given;

public class EventClient extends BaseClient {

    private static final String EVENTS_PATH = "/events";
    private static final String LIMIT_QUERY = "limit";
    private static final String TYPE_QUERY = "type";

    public EventClient(ApiClient apiClient) {
        super(apiClient);
    }

    public ApiResult<List<EventLogResponse>> list() {
        return list(null, null);
    }

    @Step("GET /events (limit={0}, type={1})")
    public ApiResult<List<EventLogResponse>> list(Integer limit, String type) {
        RequestSpecification request = given(spec());
        if (limit != null) {
            request.queryParam(LIMIT_QUERY, limit);
        }
        if (type != null) {
            request.queryParam(TYPE_QUERY, type);
        }

        Response response = request.get(EVENTS_PATH);

        return ApiResult.from(response, new TypeReference<>() {
        });
    }

}
