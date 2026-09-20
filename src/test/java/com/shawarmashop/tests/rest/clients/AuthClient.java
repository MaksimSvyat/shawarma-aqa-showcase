package com.shawarmashop.tests.rest.clients;

import com.shawarmashop.tests.auth.ApiClient;
import com.shawarmashop.tests.dto.auth.JwtResponse;
import com.shawarmashop.tests.dto.auth.LoginRequest;
import com.shawarmashop.tests.dto.auth.UserResponse;
import com.shawarmashop.tests.rest.ApiResult;
import com.shawarmashop.tests.rest.BaseClient;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class AuthClient extends BaseClient {

    private static final String LOGIN_PATH = "/auth/login";
    private static final String ME_PATH = "/auth/me";

    public AuthClient(ApiClient apiClient) {
        super(apiClient);
    }

    @Step("POST /auth/login (username={0})")
    public ApiResult<JwtResponse> login(String username, String password) {
        Response response = given(publicSpec())
                .body(new LoginRequest(username, password))
                .post(LOGIN_PATH);

        return ApiResult.from(response, JwtResponse.class);
    }

    @Step("GET /auth/me")
    public ApiResult<UserResponse> me() {
        Response response = given(spec())
                .get(ME_PATH);
        return ApiResult.from(response, UserResponse.class);
    }
}
