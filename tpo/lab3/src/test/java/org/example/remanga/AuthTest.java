package org.example.remanga;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Tag("auth")
class AuthTest extends BaseTest {
    @Test
    void authenticated_user_opens_bookmarks() {
        assumeTrue(credentialsProvided(), "Для auth-тестов передайте -Dremanga.login/-Dremanga.password или REMANGA_LOGIN/REMANGA_PASSWORD");

        loginWithConfiguredUser();
        assertTrue(accountPage()
                        .openBookmarksFromHeader()
                        .isBookmarksSectionVisible(),
                "После авторизации должен открываться раздел закладок");
    }

    @Test
    void authenticated_user_sees_profile_block() {
        assumeTrue(credentialsProvided(), "Для auth-тестов передайте -Dremanga.login/-Dremanga.password или REMANGA_LOGIN/REMANGA_PASSWORD");

        loginWithConfiguredUser();

        assertTrue(accountPage().isProfileBlockVisible(configuredLogin()),
                "После входа должен отображаться пользовательский блок");
    }

    @Test
    void authenticated_user_can_open_bookmark_action_on_title_page() {
        assumeTrue(credentialsProvided(), "Для auth-тестов передайте -Dremanga.login/-Dremanga.password или REMANGA_LOGIN/REMANGA_PASSWORD");

        loginWithConfiguredUser();

        assertTrue(titlePage()
                        .openEleceed()
                        .isBookmarkActionVisible(),
                "Авторизованному пользователю должно быть доступно действие 'В закладки'");
    }
}
