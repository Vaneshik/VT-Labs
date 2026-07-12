package org.example.remanga;

import org.example.remanga.pages.LoginPage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginTest extends BaseTest {
    @Test
    void user_sees_error_for_invalid_login() {
        homePage().open();

        LoginPage page = loginPage()
                .openDialog()
                .submit("wrong-user-" + System.currentTimeMillis(), "wrong-password");

        assertTrue(page.isInvalidCredentialsErrorVisible(),
                "При неверных данных должна отображаться ошибка авторизации");
    }
}
