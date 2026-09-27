package com.shawarmashop.tests.ui.tests;

import com.shawarmashop.tests.ui.BaseUITest;
import com.shawarmashop.tests.ui.pages.LoginPage;
import com.shawarmashop.tests.ui.pages.MenuPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LoginUITests extends BaseUITest {

    public static final String WRONG_CREDENTIALS_TOAST = "Invalid username or password";

    private final LoginPage loginPage = new LoginPage();

    @Test
    @DisplayName("Логин с валидными учётными данными открывает меню")
    public void loginWithValidCredentials() {
        loginPage
                .openLogin()
                .submit("owner", "owner123");

        new MenuPage().shouldBeOpened();
    }

    @Test
    @DisplayName("Логин с неверным паролем показывает ошибку")
    void loginWithWrongPasswordShowsError() {
        loginPage
                .openLogin()
                .submit("owner", "wrong");

        loginPage.expectToast(WRONG_CREDENTIALS_TOAST);
    }
}
