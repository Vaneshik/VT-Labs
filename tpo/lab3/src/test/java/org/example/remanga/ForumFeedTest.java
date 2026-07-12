package org.example.remanga;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ForumFeedTest extends BaseTest {
    @Test
    void user_filters_forum_by_category() {
        assertTrue(forumPage()
                        .open()
                        .filterByNews()
                        .isNewsFilterAndPostsVisible(),
                "После выбора категории форум должен показывать выбранную категорию и записи");
    }

    @Test
    void user_searches_forum_posts() {
        assertTrue(forumPage()
                        .open()
                        .search("обмен")
                        .arePostsVisible(),
                "После поиска форум должен показывать список записей");
    }

    @Test
    void user_opens_forum_post() {
        assertTrue(forumPage()
                        .open()
                        .openFirstPost()
                        .isPostPageVisible(),
                "Должна открыться страница записи форума");
    }
}
