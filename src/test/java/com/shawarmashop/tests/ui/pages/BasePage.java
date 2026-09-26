package com.shawarmashop.tests.ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public abstract class BasePage<T extends BasePage<T>> {

    protected final SelenideElement toast = $("div[role=status]");
    protected final SelenideElement navLogo = $("nav a[href='/menu']");
    protected final SelenideElement pageHeading = $("h1.arcade-title");

    public T open(String path) {
        Selenide.open(path);
        return self();
    }

    public T shouldBeOpened() {
        navLogo.shouldBe(Condition.visible);
        pageHeading.shouldBe(Condition.visible);
        return self();
    }

    public T expectToast(String value) {
        toast.shouldBe(Condition.visible).shouldBe(Condition.text(value));
        return self();
    }

    @SuppressWarnings("unchecked")
    private T self() {
        return (T) this;
    }
}
