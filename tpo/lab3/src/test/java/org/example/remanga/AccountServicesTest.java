package org.example.remanga;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Tag("auth")
class AccountServicesTest extends BaseTest {
    @Test
    void authenticated_user_opens_chat() {
        assumeTrue(credentialsProvided(), "Для auth-тестов передайте -Dremanga.login/-Dremanga.password или REMANGA_LOGIN/REMANGA_PASSWORD");

        loginWithConfiguredUser();
        assertTrue(accountServicesPage().openChat().isChatVisible(),
                "Должен открыться чат с поиском и рабочей областью");
    }

    @Test
    void authenticated_user_opens_subscription_page() {
        assumeTrue(credentialsProvided(), "Для auth-тестов передайте -Dremanga.login/-Dremanga.password или REMANGA_LOGIN/REMANGA_PASSWORD");

        loginWithConfiguredUser();
        assertTrue(accountServicesPage().openSubscription().isSubscriptionVisible(),
                "Должна открыться страница подписки RePremium");
    }

    @Test
    void authenticated_user_opens_customization_shop() {
        assumeTrue(credentialsProvided(), "Для auth-тестов передайте -Dremanga.login/-Dremanga.password или REMANGA_LOGIN/REMANGA_PASSWORD");

        loginWithConfiguredUser();
        assertTrue(accountServicesPage().openCustomizationShop().isCustomizationShopVisible(),
                "Должен открыться магазин кастомизации с вкладками");
    }

    @Test
    void authenticated_user_opens_billing_history() {
        assumeTrue(credentialsProvided(), "Для auth-тестов передайте -Dremanga.login/-Dremanga.password или REMANGA_LOGIN/REMANGA_PASSWORD");

        loginWithConfiguredUser();
        assertTrue(accountServicesPage().openBilling().isBillingVisible(),
                "Должна открыться история транзакций");
    }

    @Test
    void authenticated_user_opens_dungeon_hunters_event() {
        assumeTrue(credentialsProvided(), "Для auth-тестов передайте -Dremanga.login/-Dremanga.password или REMANGA_LOGIN/REMANGA_PASSWORD");

        loginWithConfiguredUser();
        assertTrue(accountServicesPage().openDungeonHunters().isDungeonHuntersVisible(),
                "Должна открыться страница ивента");
    }

    @Test
    void authenticated_user_opens_battlepass_info() {
        assumeTrue(credentialsProvided(), "Для auth-тестов передайте -Dremanga.login/-Dremanga.password или REMANGA_LOGIN/REMANGA_PASSWORD");

        loginWithConfiguredUser();
        assertTrue(accountServicesPage().openBattlepassInfo().isBattlepassInfoVisible(),
                "Должно открыться описание Re:Pass");
    }
}
