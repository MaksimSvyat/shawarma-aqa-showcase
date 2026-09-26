package com.shawarmashop.tests.ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Selenide.$;

public class LoginPage extends BasePage<LoginPage> {

    private final SelenideElement form = $("form");
    private final SelenideElement usernameInput = $("input[data-testid=login-username]");
    private final SelenideElement passwordInput = $("input[data-testid=login-password]");
    private final SelenideElement submitBtn = $("button[data-testid=login-submit]");

    @Step("Открыть /login и дождаться формы")
    public LoginPage openLogin() {
        open("/login");
        form.shouldBe(Condition.visible);
        return this;
    }

    public LoginPage submit(String username, String password) {
        usernameInput.sendKeys(username);
        passwordInput.sendKeys(password);
        submitBtn.click();
        return this;
    }
}
