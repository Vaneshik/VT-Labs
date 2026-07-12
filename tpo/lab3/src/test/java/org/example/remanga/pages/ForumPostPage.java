package org.example.remanga.pages;

import org.example.remanga.BaseTest;

public class ForumPostPage extends BasePage {
    private static final String TOPIC_INPUT = "//input[@placeholder='Темы' and @role='combobox']";
    private static final String TITLE_INPUT = "//input[@name='header' and @placeholder='Заголовок']";
    private static final String BODY_EDITOR = "//*[@role='textbox' and @contenteditable='true']";
    private static final String SUBMIT_BUTTON = "//button[normalize-space(.)='Отправить']";

    public ForumPostPage(BaseTest test) {
        super(test);
    }

    public ForumPostPage openCreateForm() {
        open("/forum/create");
        return this;
    }

    public ForumPostPage openCreateFormFromFeed() {
        open("/forum/feed?ordering=-id&week=0");
        click("//a[contains(@href, '/forum/create') and contains(normalize-space(.), 'Создать пост')]");
        return this;
    }

    public ForumPostPage selectTopic(String topic) {
        click(TOPIC_INPUT);
        click("//*[normalize-space(.)=" + test.xpathLiteral(topic) + "]");
        return this;
    }

    public ForumPostPage typeTitle(String title) {
        type(TITLE_INPUT, title);
        return this;
    }

    public ForumPostPage typeBody(String body) {
        type(BODY_EDITOR, body);
        return this;
    }

    public ForumPostPage submit() {
        click(SUBMIT_BUTTON);
        return this;
    }

    public boolean createPostLinkExists() {
        return exists("//a[contains(@href, '/forum/create') and contains(normalize-space(.), 'Создать пост')]");
    }

    public boolean formOpened() {
        return exists(TITLE_INPUT);
    }

    public boolean isCreateFormVisible() {
        return urlContains("/forum/create")
                && isVisible(TOPIC_INPUT)
                && isVisible(TITLE_INPUT)
                && isVisible(BODY_EDITOR)
                && isVisible(SUBMIT_BUTTON);
    }

    public String titleValidationMessage() {
        return validationMessage(TITLE_INPUT);
    }

    public String titleValue() {
        return value(TITLE_INPUT);
    }

    public boolean submitButtonExists() {
        return exists(SUBMIT_BUTTON);
    }

    public boolean isCreatedPostOpened() {
        return urlContains("/forum/")
                && isVisible("//*[contains(normalize-space(.), 'Автотест')] | //*[contains(normalize-space(.), 'Тестовый пост')]");
    }
}
