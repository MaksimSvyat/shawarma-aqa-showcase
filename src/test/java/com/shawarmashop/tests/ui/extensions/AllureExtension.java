package com.shawarmashop.tests.ui.extensions;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import com.shawarmashop.tests.support.Json;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.Cookie;

import java.util.Map;
import java.util.Set;

public class AllureExtension implements AfterEachCallback {
    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        if (context.getExecutionException().isPresent() && WebDriverRunner.hasWebDriverStarted()) {
            WebDriverRunner.getWebDriver().switchTo().defaultContent();

            Set<Cookie> cookies = WebDriverRunner.getWebDriver().manage().getCookies();
            Allure.addAttachment("Cookies", "application/json", Json.toJson(cookies));

            String perfLogs = Selenide.executeJavaScript("return JSON.stringify(window.performance.getEntries())");
            Allure.addAttachment("Performance Logs", "application/json", perfLogs);

            Map<String, String> items = Selenide.localStorage().getItems();
            Allure.addAttachment("Local Storage", "application/json", Json.toJson(items));
        }
    }
}
