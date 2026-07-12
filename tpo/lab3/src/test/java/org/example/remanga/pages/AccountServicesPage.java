package org.example.remanga.pages;

import org.example.remanga.BaseTest;

public class AccountServicesPage extends BasePage {
    public AccountServicesPage(BaseTest test) {
        super(test);
    }

    public AccountServicesPage openChat() {
        open("/chat");
        return this;
    }

    public AccountServicesPage openSubscription() {
        open("/user/subscription");
        return this;
    }

    public AccountServicesPage openCustomizationShop() {
        open("/customization/feed/avatar");
        return this;
    }

    public AccountServicesPage openBilling() {
        open("/user/billing");
        return this;
    }

    public AccountServicesPage openDungeonHunters() {
        open("/dungeon-hunters");
        return this;
    }

    public AccountServicesPage openBattlepassInfo() {
        open("/user/battlepass/info");
        return this;
    }

    public boolean isChatVisible() {
        return isVisible("//*[contains(normalize-space(.), 'Чаты')]")
                && isVisible("//input[@placeholder='Поиск']")
                && isVisible("//*[contains(normalize-space(.), 'Бета-версия чата') or contains(normalize-space(.), 'Выберите чат')]");
    }

    public boolean isSubscriptionVisible() {
        return isVisible("//*[contains(normalize-space(.), 'RePremium')]")
                && isVisible("//button[contains(normalize-space(.), 'Попробовать') or contains(normalize-space(.), 'Подписка')]");
    }

    public boolean isCustomizationShopVisible() {
        return isVisible("//*[normalize-space(.)='Магазин']")
                && isVisible("//button[normalize-space(.)='Аватарки']")
                && isVisible("//button[normalize-space(.)='Рамки']")
                && isVisible("//button[normalize-space(.)='Обои']");
    }

    public boolean isBillingVisible() {
        return isVisible("//*[contains(normalize-space(.), 'История транзакций')]")
                && isVisible("//button[normalize-space(.)='Молнии']")
                && isVisible("//button[normalize-space(.)='Монеты']")
                && isVisible("//button[normalize-space(.)='Тикеты']");
    }

    public boolean isDungeonHuntersVisible() {
        return isVisible("//*[contains(normalize-space(.), 'ОХОТНИКИ')]")
                && isVisible("//button[contains(normalize-space(.), 'Принять вызов')]")
                && isVisible("//button[contains(normalize-space(.), 'Отказаться')]");
    }

    public boolean isBattlepassInfoVisible() {
        return isVisible("//*[contains(normalize-space(.), 'Re:Pass') or contains(normalize-space(.), 'Pass')]")
                && isVisible("//button[contains(normalize-space(.), 'Вступить') or contains(normalize-space(.), 'Получить') or contains(normalize-space(.), 'Открыть')]"
                        + " | //a[contains(@href, '/user/battlepass')]");
    }
}
