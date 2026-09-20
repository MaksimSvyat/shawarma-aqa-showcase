package com.shawarmashop.tests.env;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;

@Config.Sources({
        "system:properties",
        "system:env",
        "classpath:shawarma.properties"
})
@Config.LoadPolicy(Config.LoadType.MERGE)
public interface TestEnvironment extends Config {

    TestEnvironment INSTANCE = ConfigFactory.create(TestEnvironment.class);

    @Key("backend.url")
    String backendBaseUrl();

    @Key("frontend.url")
    String frontendBaseUrl();

    @Key("wiremock.rest.baseUrl")
    String wiremockRestBaseUrl();

    @Key("wiremock.grpc.adminUrl")
    String wiremockGrpcAdminUrl();

    @Key("kafka.bootstrap")
    String kafkaBootstrap();

    @Key("db.url")
    String jdbcUrl();

    @Key("db.user")
    String jdbcUser();

    @Key("db.password")
    String jdbcPassword();

}
