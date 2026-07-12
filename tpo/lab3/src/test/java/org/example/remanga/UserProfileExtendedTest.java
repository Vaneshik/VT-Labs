package org.example.remanga;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Tag("auth")
class UserProfileExtendedTest extends BaseTest {
    @Test
    void authenticated_user_opens_notifications() {
        assumeTrue(credentialsProvided(), "Для auth-тестов передайте -Dremanga.login/-Dremanga.password или REMANGA_LOGIN/REMANGA_PASSWORD");

        loginWithConfiguredUser();
        assertTrue(accountPage().openNotifications().areNotificationsVisible(),
                "Должен открыться раздел уведомлений с вкладками");
    }

    @Test
    void authenticated_user_opens_profile_settings() {
        assumeTrue(credentialsProvided(), "Для auth-тестов передайте -Dremanga.login/-Dremanga.password или REMANGA_LOGIN/REMANGA_PASSWORD");

        loginWithConfiguredUser();
        assertTrue(accountPage().openSettings().areSettingsVisible(),
                "Должны открыться настройки профиля с разделами");
    }

    @Test
    void authenticated_user_opens_friends_page() {
        assumeTrue(credentialsProvided(), "Для auth-тестов передайте -Dremanga.login/-Dremanga.password или REMANGA_LOGIN/REMANGA_PASSWORD");

        loginWithConfiguredUser();
        assertTrue(accountPage().openFriends().areFriendsVisible(),
                "Должна открыться страница друзей");
    }

    @Test
    void authenticated_user_opens_profile_social_posts() {
        assumeTrue(credentialsProvided(), "Для auth-тестов передайте -Dremanga.login/-Dremanga.password или REMANGA_LOGIN/REMANGA_PASSWORD");

        loginWithConfiguredUser();
        assertTrue(accountPage().openSocialPosts().areSocialPostsVisible(),
                "Должен открыться социальный раздел с вкладками");
    }

    @Test
    void authenticated_user_opens_achievements_page() {
        assumeTrue(credentialsProvided(), "Для auth-тестов передайте -Dremanga.login/-Dremanga.password или REMANGA_LOGIN/REMANGA_PASSWORD");

        loginWithConfiguredUser();
        assertTrue(accountPage().openAchievements().areAchievementsVisible(),
                "Должна открыться страница достижений");
    }
}
