package org.example.remanga;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Tag("auth")
class UserAccountTest extends BaseTest {
    @Test
    void authenticated_user_sees_bookmark_status_filters() {
        assumeTrue(credentialsProvided(), "Для auth-тестов передайте -Dremanga.login/-Dremanga.password или REMANGA_LOGIN/REMANGA_PASSWORD");

        loginWithConfiguredUser();
        assertTrue(accountPage().openBookmarks().areBookmarkFiltersVisible(),
                "В закладках должны быть фильтры статуса и сортировка");
    }

    @Test
    void authenticated_user_opens_reading_history() {
        assumeTrue(credentialsProvided(), "Для auth-тестов передайте -Dremanga.login/-Dremanga.password или REMANGA_LOGIN/REMANGA_PASSWORD");

        loginWithConfiguredUser();
        assertTrue(accountPage().openHistory().isHistoryVisible(),
                "Должна открыться история чтения с командой очистки и ссылками");
    }

    @Test
    void authenticated_user_opens_requests_page() {
        assumeTrue(credentialsProvided(), "Для auth-тестов передайте -Dremanga.login/-Dremanga.password или REMANGA_LOGIN/REMANGA_PASSWORD");

        loginWithConfiguredUser();
        assertTrue(accountPage().openRequests().areRequestsVisible(),
                "Должна открыться страница заявок с фильтрами");
    }

    @Test
    void authenticated_user_opens_own_public_profile() {
        assumeTrue(credentialsProvided(), "Для auth-тестов передайте -Dremanga.login/-Dremanga.password или REMANGA_LOGIN/REMANGA_PASSWORD");

        loginWithConfiguredUser();
        assertTrue(accountPage().openPublicProfile().isPublicProfileVisible(),
                "Должен открыться публичный профиль с именем, ID и вкладками");
    }
}
