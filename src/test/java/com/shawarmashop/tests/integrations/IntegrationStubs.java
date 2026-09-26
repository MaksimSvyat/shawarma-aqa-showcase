package com.shawarmashop.tests.integrations;

import com.shawarmashop.tests.env.TestEnvironment;
import com.shawarmashop.tests.integrations.bread.BreadStubs;
import com.shawarmashop.tests.integrations.meat.MeatStubs;
import com.shawarmashop.tests.integrations.payments.PaymentsStubs;
import com.shawarmashop.tests.integrations.reviews.ReviewsStubs;
import com.shawarmashop.tests.integrations.wiremock.WireMockAdminClient;

public final class IntegrationStubs {
    private final WireMockAdminClient restAdmin;
    private final WireMockAdminClient grpcAdmin;

    public IntegrationStubs() {
        restAdmin = new WireMockAdminClient(TestEnvironment.INSTANCE.wiremockRestBaseUrl());
        grpcAdmin = new WireMockAdminClient(TestEnvironment.INSTANCE.wiremockGrpcAdminUrl());
    }

    public void resetToDefaults() {
        restAdmin.clearRecorded();
        grpcAdmin.clearRecorded();
        restAdmin.resetToDefaults();
        grpcAdmin.resetToDefaults();
    }

    public PaymentsStubs payments() {
        return new PaymentsStubs(restAdmin);
    }

    public ReviewsStubs reviews() {
        return new ReviewsStubs(restAdmin);
    }

    public MeatStubs meat(){
        return new MeatStubs(restAdmin);
    }

    public BreadStubs bread(){
        return new BreadStubs(grpcAdmin);
    }
}
