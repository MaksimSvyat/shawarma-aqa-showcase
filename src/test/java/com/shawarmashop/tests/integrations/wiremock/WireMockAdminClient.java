package com.shawarmashop.tests.integrations.wiremock;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

@RequiredArgsConstructor
public class WireMockAdminClient {

    private final String baseUrl;

    public String addMapping(StubBuilder builder) {
        String id = given(spec())
                .body(builder.build())
                .post("/__admin/mappings")
                .then().statusCode(201)
                .extract().jsonPath().getString("id");
        if (id == null || id.isBlank()) {
            throw new IllegalStateException("WireMock не вернул id маппинга");
        }

        return id;
    }

    public void resetToDefaults(){
        given(spec())
                .post("/__admin/mappings/reset")
                .then().statusCode(200);
    }

    public void clearRecorded(){
        given(spec())
                .delete("/__admin/mappings")
                .then().statusCode(200);
    }

    public List<Map<String, Object>> findRequests(Map<String, Object> criteria) {
        List<Map<String, Object>> requests = given(spec())
                .body(criteria)
                .post("/__admin/requests/find")
                .then().statusCode(200)
                .extract().jsonPath().getList("requests");

        return requests == null ? List.of() : requests;
    }

    private RequestSpecification spec() {
        return new RequestSpecBuilder()
                .setBaseUri(baseUrl)
                .setContentType(ContentType.JSON)
                .addFilter(new AllureRestAssured())
                .addFilter(new ResponseLoggingFilter())
                .addFilter(new RequestLoggingFilter())
                .build();
    }
}
