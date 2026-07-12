package org.example.remanga.pages;

import org.example.remanga.BaseTest;

public class ForumPage extends BasePage {
    private static final String FORUM_PATH = "/forum/feed?ordering=-id&week=0";

    public ForumPage(BaseTest test) {
        super(test);
    }

    public ForumPage open() {
        open(FORUM_PATH);
        return this;
    }

    public ForumPage filterByNews() {
        click("//button[normalize-space(.)='Новости сайта']");
        return this;
    }

    public ForumPage search(String query) {
        type("//input[@name='search_term_string' and @placeholder='Поиск постов']", query);
        return this;
    }

    public ForumPage openFirstPost() {
        click("(//a[contains(@href, '/forum/') and not(contains(@href, '/forum/feed')) and string-length(normalize-space(.)) > 1])[1]");
        return this;
    }

    public boolean isForumVisible() {
        return isVisible("//a[contains(normalize-space(.), 'Форум') and contains(@href, '/forum/feed')]")
                && isVisible("//*[normalize-space(.)='Популярное за неделю'] | //button[normalize-space(.)='Новости сайта']")
                && arePostsVisible();
    }

    public boolean isNewsFilterAndPostsVisible() {
        return isVisible("//button[normalize-space(.)='Новости сайта']") && arePostsVisible();
    }

    public boolean arePostsVisible() {
        return isAnyVisible("//a[contains(@href, '/forum/') and string-length(normalize-space(.)) > 1]");
    }

    public boolean isPostPageVisible() {
        return isVisible("//a[contains(@href, '/forum/feed') and contains(normalize-space(.), 'Форум')]"
                + " | //button[contains(normalize-space(.), 'Ответить')]"
                + " | //*[contains(normalize-space(.), 'Комментарии')]");
    }
}
