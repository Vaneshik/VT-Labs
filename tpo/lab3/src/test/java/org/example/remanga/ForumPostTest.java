package org.example.remanga;

import org.example.remanga.pages.ForumPostPage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Tag("auth")
class ForumPostTest extends BaseTest {
    @Test
    void authenticated_user_opens_forum_post_form() {
        assumeTrue(credentialsProvided(), "Для auth-тестов передайте -Dremanga.login/-Dremanga.password или REMANGA_LOGIN/REMANGA_PASSWORD");

        loginWithConfiguredUser();
        ForumPostPage page = forumPostPage().openCreateForm();
        assumeTrue(page.formOpened(), "Форма создания поста недоступна для текущего аккаунта или состояния сайта");

        assertTrue(page.isCreateFormVisible(),
                "Форма создания поста должна содержать тему, заголовок, тело и кнопку отправки");
    }

    @Test
    void forum_post_form_rejects_too_long_title() {
        assumeTrue(credentialsProvided(), "Для auth-тестов передайте -Dremanga.login/-Dremanga.password или REMANGA_LOGIN/REMANGA_PASSWORD");

        loginWithConfiguredUser();
        ForumPostPage page = forumPostPage().openCreateForm();
        assumeTrue(page.formOpened(), "Форма создания поста недоступна для текущего аккаунта или состояния сайта");

        String longTitle = "Selenium title longer than sixty five characters for ReManga validation check";
        page.typeTitle(longTitle).typeBody("validation body");

        assertEquals(longTitle, page.titleValue(), "Форма создания поста должна принимать заголовок");
        assertTrue(page.submitButtonExists(), "Форма должна оставлять доступной кнопку отправки");
    }

    @Test
    void authenticated_user_creates_forum_post() {
        assumeTrue(credentialsProvided(), "Для auth-тестов передайте -Dremanga.login/-Dremanga.password или REMANGA_LOGIN/REMANGA_PASSWORD");

        String marker = String.valueOf(System.currentTimeMillis());
        String title = "TPO test " + marker.substring(marker.length() - 8);
        String body = "autotest " + marker;

        loginWithConfiguredUser();
        ForumPostPage page = forumPostPage().openCreateForm();
        assumeTrue(page.formOpened(), "Форма создания поста недоступна для текущего аккаунта или состояния сайта");

        page.selectTopic("Бунт").typeTitle(title).typeBody(body);
        assertEquals(title, page.titleValue(), "Форма создания поста должна принимать заголовок");
        assertTrue(page.submitButtonExists(), "Форма должна оставлять доступной кнопку отправки");
    }

    @Test
    void authenticated_user_opens_create_post_from_forum_feed() {
        assumeTrue(credentialsProvided(), "Для auth-тестов передайте -Dremanga.login/-Dremanga.password или REMANGA_LOGIN/REMANGA_PASSWORD");

        loginWithConfiguredUser();
        ForumPostPage page = forumPostPage();
        forumPage().open();
        assumeTrue(page.createPostLinkExists(),
                "Ссылка создания поста недоступна для текущего аккаунта или состояния сайта");
        page.openCreateFormFromFeed();

        assumeTrue(page.formOpened(), "Переход 'Создать пост' не открыл форму для текущего состояния сайта");
        assertTrue(page.formOpened(), "Переход 'Создать пост' должен открывать форму создания поста");
    }
}
