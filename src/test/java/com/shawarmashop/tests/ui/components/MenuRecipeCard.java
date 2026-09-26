package com.shawarmashop.tests.ui.components;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

public class MenuRecipeCard {

    private final SelenideElement root;
    private final SelenideElement title;
    private final ElementsCollection sizeChips;
    private final SelenideElement infoBtn;
    private final SelenideElement orderBtn;

    public MenuRecipeCard(SelenideElement root) {
        this.root = root;
        this.title = root.$x(".//h3[@data-testid='recipe-name']");
        this.sizeChips = root.$$(".arcade-chip");
        this.infoBtn = root.$x(".//button[contains(.,'INFO')]");
        this.orderBtn = root.$x(".//button[contains(.,'INSERT COIN')]");
    }

    public SelenideElement sizeChip(String size) {
        return sizeChips.find(Condition.exactText(size));
    }

    @Step("Карточка рецепта: переключить размер на {0}")
    public MenuRecipeCard pickSize(String size) {
        sizeChip(size).click();
        return this;
    }

    @Step("Карточка рецепта: нажать INSERT COIN")
    public MenuRecipeCard clickOrder() {
        orderBtn.click();
        return this;
    }

    @Step("Карточка рецепта: нажать INFO")
    public MenuRecipeCard openInfo() {
        infoBtn.click();
        return this;
    }

}
