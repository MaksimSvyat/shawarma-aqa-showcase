package com.shawarmashop.tests.ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.logevents.SelenideLogger;
import com.shawarmashop.tests.auth.ApiClient;
import com.shawarmashop.tests.auth.Credentials;
import com.shawarmashop.tests.auth.Users;
import com.shawarmashop.tests.env.TestEnvironment;
import com.shawarmashop.tests.support.Json;
import com.shawarmashop.tests.ui.extensions.AllureExtension;
import com.shawarmashop.tests.ui.extensions.BrowserLifecycleExtension;
import io.netty.handler.codec.http.HttpMethod;
import io.qameta.allure.selenide.AllureSelenide;
import io.qameta.allure.selenide.LogType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

@ExtendWith({
        BrowserLifecycleExtension.class,
        AllureExtension.class
})
public abstract class BaseUITest {

    protected final ApiClient owner = ApiClient.asUser(Users.OWNER);


    @BeforeAll
    public static void initSelenide() {
        Configuration.baseUrl = TestEnvironment.INSTANCE.frontendBaseUrl();
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";
        Configuration.proxyEnabled = true;
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide()
                .savePageSource(true)
                .screenshots(true)
                .enableLogs(LogType.BROWSER, Level.ALL));
    }

    protected <T> T interceptResponse(HttpMethod httpMethod, String urlPath, Class<T> tClass, Runnable runnable) {
        CompletableFuture<T> future = new CompletableFuture<>();
        String filter = "response-" + UUID.randomUUID();
        WebDriverRunner.getSelenideProxy().addResponseFilter(
                filter,
                ((response, contents, messageInfo) -> {
                    if (future.isDone()) {
                        return;
                    }

                    String actualUrl = messageInfo.getUrl();
                    if (!actualUrl.contains(urlPath)) {
                        return;
                    }

                    HttpMethod actualMethod = messageInfo.getOriginalRequest().method();
                    if (actualMethod != httpMethod) {
                        return;
                    }

                    String json = contents.getTextContents();
                    try {
                        T result = Json.MAPPER.readValue(json, tClass);
                        future.complete(result);
                    } catch (Exception e) {
                        future.completeExceptionally(new IllegalStateException("Не удалось распарсить ответ от " + actualUrl, e));
                    }
                }));

        runnable.run();

        try {
            return future.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            String msg = String.format("Не дождались ответа %s %s за %d секунд", httpMethod, urlPath, 10);
            throw new AssertionError(msg, e);
        } catch (Exception e) {
            String msg = String.format("Ошибка при перехвате ответа %s %s", httpMethod, urlPath);
            throw new RuntimeException(msg, e);
        }
    }

    protected void defaultAuth() {
        pushTokenToLocalStorage(owner.tokenOrNull());
    }

    protected void loginAs(Credentials credentials) {
        ApiClient apiClient = ApiClient.asUser(credentials);
        pushTokenToLocalStorage(apiClient.tokenOrNull());
    }


    private void pushTokenToLocalStorage(String token) {
        Selenide.open("/login");
        Selenide.localStorage().setItem("token", token);
    }
}
