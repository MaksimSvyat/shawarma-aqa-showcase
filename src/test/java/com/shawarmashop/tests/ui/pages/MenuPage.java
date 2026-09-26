package com.shawarmashop.tests.ui.pages;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.shawarmashop.tests.ui.components.MenuRecipeCard;
import com.shawarmashop.tests.ui.components.OrderModal;

import static com.codeborne.selenide.Selenide.$$;

public class MenuPage extends BasePage<MenuPage> {

    private final ElementsCollection cards = $$("[data-testid=recipe-card]");

    public MenuPage openMenu() {
        return open("/menu")
                .shouldBeOpened()
                .shouldHaveRecipes();
    }

    public MenuPage shouldHaveRecipes() {
        cards.should(CollectionCondition.sizeGreaterThan(0));
        return this;
    }

    public MenuRecipeCard cardByName(String name) {
        SelenideElement root = cards
                .find(Condition.attribute("data-recipe-name", name))
                .shouldBe(Condition.visible);

        return new MenuRecipeCard(root);
    }

    public OrderModal startOrderFor(String name) {
        cardByName(name).clickOrder();
        return new OrderModal().shouldOpened();
    }

}
