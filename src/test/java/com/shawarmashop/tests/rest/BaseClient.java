package com.shawarmashop.tests.rest;

import com.shawarmashop.tests.env.TestEnvironment;
import com.shawarmashop.tests.auth.ApiClient;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class BaseClient {

    private final ApiClient apiClient;

    protected RequestSpecification spec() {
        RequestSpecBuilder builder = baseSpec();
        String token = apiClient.tokenOrNull();
        if (token != null && !token.isBlank()) {
            builder.addHeader("Authorization", "Bearer " + token);
        }
        return builder.build();
    }

    protected RequestSpecification publicSpec() {
        return baseSpec().build();
    }

    private static RequestSpecBuilder baseSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(TestEnvironment.INSTANCE.backendBaseUrl())
                .setBasePath("/api/v1")
                .setContentType(ContentType.JSON)
                .addFilter(new AllureRestAssured())
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter());
    }
}
