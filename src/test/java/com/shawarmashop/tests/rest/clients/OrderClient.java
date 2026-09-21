package com.shawarmashop.tests.rest.clients;

import com.fasterxml.jackson.core.type.TypeReference;
import com.shawarmashop.tests.auth.ApiClient;
import com.shawarmashop.tests.dto.Page;
import com.shawarmashop.tests.dto.orders.CreateOrderRequest;
import com.shawarmashop.tests.dto.orders.OrderFilter;
import com.shawarmashop.tests.dto.orders.OrderResponse;
import com.shawarmashop.tests.dto.orders.PreparationResponse;
import com.shawarmashop.tests.rest.ApiResult;
import com.shawarmashop.tests.rest.BaseClient;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class OrderClient extends BaseClient {

    private static final String ORDERS_PATH = "/orders";
    private static final String ORDER_BY_ID = "/orders/{id}";
    private static final String ORDER_PREPARATION = "/orders/{id}/preparation";
    private static final String ORDER_CANCEL = "/orders/{id}/cancel";

    public OrderClient(ApiClient apiClient) {
        super(apiClient);
    }

    @Step("POST /orders")
    public ApiResult<OrderResponse> create(CreateOrderRequest request) {
        Response response = given(spec())
                .body(request)
                .post(ORDERS_PATH);

        return ApiResult.from(response, OrderResponse.class);
    }

    @Step("GET /orders (filter={0})")
    public ApiResult<Page<OrderResponse>> list(OrderFilter orderFilter) {
        Response response = given(spec())
                .queryParams(orderFilter.toQueryParams())
                .get(ORDERS_PATH);

        return ApiResult.from(response, new TypeReference<>() {
        });
    }


    @Step("GET /orders/{0}")
    public ApiResult<OrderResponse> get(int id) {
        Response response = given(spec())
                .pathParam("id", id)
                .get(ORDER_BY_ID);
        return ApiResult.from(response, OrderResponse.class);
    }

    @Step("GET /orders/{0}/preparation")
    public ApiResult<PreparationResponse> preparation(int id) {
        Response response = given(spec())
                .pathParam("id", id)
                .get(ORDER_PREPARATION);
        return ApiResult.from(response, PreparationResponse.class);
    }

    @Step("POST /orders/{0}/cancel")
    public ApiResult<OrderResponse> cancel(int id) {
        Response response = given(spec())
                .pathParam("id", id)
                .post(ORDER_CANCEL);
        return ApiResult.from(response, OrderResponse.class);
    }
}
