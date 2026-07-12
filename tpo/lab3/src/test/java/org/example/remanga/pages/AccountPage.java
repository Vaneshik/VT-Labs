package org.example.remanga.pages;

import org.example.remanga.BaseTest;

public class AccountPage extends BasePage {
    public AccountPage(BaseTest test) {
        super(test);
    }

    public AccountPage openBookmarksFromHeader() {
        click("//a[contains(@href, '/user/bookmarks') and contains(normalize-space(.), 'Закладки')]"
                + " | //button[contains(normalize-space(.), 'Закладки')]");
        return this;
    }

    public AccountPage openBookmarks() {
        open("/user/bookmarks");
        return this;
    }

    public AccountPage openHistory() {
        open("/user/history");
        return this;
    }

    public AccountPage openRequests() {
        open("/user/requests");
        return this;
    }

    public AccountPage openPublicProfile() {
        open("/user/776671/about");
        return this;
    }

    public AccountPage openNotifications() {
        open("/user/notifications/0");
        return this;
    }

    public AccountPage openSettings() {
        open("/user/settings/root");
        return this;
    }

    public AccountPage openFriends() {
        open("/user/776671/friends");
        return this;
    }

    public AccountPage openSocialPosts() {
        open("/user/776671/social/posts");
        return this;
    }

    public AccountPage openAchievements() {
        open("/user/776671/achievements");
        return this;
    }

    public boolean isBookmarksSectionVisible() {
        return isVisible("//*[contains(normalize-space(.), 'Закладки')]"
                + " | //*[contains(normalize-space(.), 'Избранное')]"
                + " | //*[contains(normalize-space(.), 'Читаю')]");
    }

    public boolean isProfileBlockVisible(String login) {
        String loginLower = login.toLowerCase();
        return isVisible("//a[contains(@href, '/user/bookmarks') and contains(normalize-space(.), 'Закладки')]")
                && isVisible("//*[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), "
                        + test.xpathLiteral(loginLower) + ")]")
                && isVisible("//a[contains(@href, '/user/notifications')]"
                        + " | //*[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), "
                        + test.xpathLiteral(loginLower) + ")]");
    }

    public boolean areBookmarkFiltersVisible() {
        return isVisible("//*[normalize-space(.)='Закладки']")
                && isVisible("//button[contains(normalize-space(.), 'Все')]")
                && isVisible("//button[contains(normalize-space(.), 'Читаю')]")
                && isVisible("//button[contains(normalize-space(.), 'Буду читать')]")
                && isVisible("//button[contains(normalize-space(.), 'Прочитано')]")
                && isVisible("//button[contains(normalize-space(.), 'По дате обновлений глав')]");
    }

    public boolean isHistoryVisible() {
        return isVisible("//*[contains(normalize-space(.), 'История чтения')]")
                && isVisible("//button[contains(normalize-space(.), 'Удалить всю историю')]")
                && isAnyVisible("//a[contains(@href, '/manga/') and contains(@href, '/main')]");
    }

    public boolean areRequestsVisible() {
        return isVisible("//*[normalize-space(.)='Заявки']")
                && isVisible("//button[contains(normalize-space(.), 'Все статусы')]")
                && isVisible("//button[contains(normalize-space(.), 'Все типы')]");
    }

    public boolean isPublicProfileVisible() {
        return isVisible("//*[contains(normalize-space(.), 'Vaneshik')]")
                && isVisible("//*[contains(normalize-space(.), 'ID: 776671')]")
                && isVisible("//button[normalize-space(.)='Профиль']")
                && isVisible("//button[normalize-space(.)='Инвентарь']")
                && isVisible("//button[normalize-space(.)='Социальное']")
                && isVisible("//*[normalize-space(.)='Бейджи'] | //*[normalize-space(.)='Ачивки']");
    }

    public boolean areNotificationsVisible() {
        return isVisible("//*[normalize-space(.)='Уведомления']")
                && isVisible("//button[contains(normalize-space(.), 'Обновления')]")
                && isVisible("//button[contains(normalize-space(.), 'Социальное')]")
                && isVisible("//button[contains(normalize-space(.), 'Важное')]");
    }

    public boolean areSettingsVisible() {
        return isVisible("//*[contains(normalize-space(.), 'Vaneshik')]")
                && isVisible("//*[contains(normalize-space(.), 'Профиль')]")
                && isVisible("//*[contains(normalize-space(.), 'Безопасность и вход в аккаунт')]")
                && isVisible("//button[contains(normalize-space(.), 'Сохранить') or contains(normalize-space(.), 'Изменить')]"
                        + " | //input | //textarea");
    }

    public boolean areFriendsVisible() {
        return isVisible("//*[normalize-space(.)='Друзья']")
                && isVisible("//*[contains(normalize-space(.), 'Пусто') or contains(normalize-space(.), 'Vaneshik')]");
    }

    public boolean areSocialPostsVisible() {
        return isVisible("//button[normalize-space(.)='Посты']")
                && isVisible("//button[normalize-space(.)='Комментарии']")
                && isVisible("//button[contains(normalize-space(.), 'Отслеживаемое')]");
    }

    public boolean areAchievementsVisible() {
        return isVisible("//*[normalize-space(.)='Ачивки']")
                && isVisible("//*[contains(normalize-space(.), 'Голос сообщества') or contains(normalize-space(.), 'Покоритель страниц')]");
    }
}
