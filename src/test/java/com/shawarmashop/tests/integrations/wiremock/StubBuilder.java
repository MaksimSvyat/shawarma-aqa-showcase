package com.shawarmashop.tests.integrations.wiremock;

import com.shawarmashop.tests.support.Json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StubBuilder {
    private final Map<String, Object> requests = new LinkedHashMap<>();
    private final Map<String, Object> response = new LinkedHashMap<>();
    private final Map<String, Object> mappings = new LinkedHashMap<>();

    private final List<Map<String, Object>> bodyPatterns = new ArrayList<>();

    private StubBuilder(String method, String url) {
        requests.put("method", method);
        requests.put("url", url);
    }

    public static StubBuilder request(String method, String url){
        return new StubBuilder(method, url);
    }

    public static StubBuilder post(String url) {
        return request("POST", url);
    }

    public static StubBuilder get(String url) {
        return request("GET", url);
    }

    public StubBuilder withPriority(int priority) {
        mappings.put("priority", priority);
        return this;
    }

    public StubBuilder withJsonPath(String jsonPath, String expectedValue) {
        bodyPatterns.add(Map.of(
                "matchesJsonPath", Map.of(
                        "expression", jsonPath,
                        "equalTo", expectedValue
                )
        ));
        return this;
    }

    public StubBuilder withXpath(String xpath) {
        bodyPatterns.add(Map.of("matchesXPath", xpath));
        return this;
    }

    public StubBuilder willReturnJson(int status, Object object) {
        response.put("status", status);
        response.put("headers", Map.of("Content-type", "application/json"));
        response.put("jsonBody", Json.MAPPER.convertValue(object, Map.class));
        return this;
    }


    public StubBuilder willReturnXml(int status, String xml) {
        response.put("status", status);
        response.put("headers", Map.of("Content-type", "text/xml; charset=UTF-8"));
        response.put("body", xml);
        return this;
    }

    public Map<String, Object> buildRequest() {
        if (!bodyPatterns.isEmpty()) {
            requests.put("bodyPatterns", bodyPatterns);
        }
        return requests;
    }

    public Map<String, Object> build() {
        if (!bodyPatterns.isEmpty()) {
            requests.put("bodyPatterns", bodyPatterns);
        }
        mappings.put("request", requests);
        mappings.put("response", response);
        return mappings;
    }
}
