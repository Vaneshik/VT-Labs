package org.example.remanga.pages;

import org.example.remanga.BaseTest;

public class LoginPage extends BasePage {
    public LoginPage(BaseTest test) {
        super(test);
    }

    public LoginPage openDialog() {
        test.openLoginDialog();
        return this;
    }

    public LoginPage submit(String login, String password) {
        test.fillLoginForm(login, password);
        return this;
    }

    public boolean isInvalidCredentialsErrorVisible() {
        return isVisible("//*[contains(normalize-space(.), 'Произошла ошибка') "
                + "or contains(normalize-space(.), 'Введен неверный логин/почта или пароль')]");
    }
}
