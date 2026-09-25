package com.shawarmashop.tests.integrations.wiremock;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public abstract class WireMockStubBase {

    protected final WireMockAdminClient admin;

    protected static String shortId(){
        return UUID.randomUUID().toString().substring(0,8);
    }
}
