package org.example.remanga;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NavigationTest extends BaseTest {
    @Test
    void user_opens_tops_and_first_title() {
        assertTrue(topPage().open().areTopsVisible(),
                "Должен открыться раздел топов с фильтрами и тайтлами");

        topPage().openFirstTitle();
        assertTrue(titlePage().isOpenedFromList(),
                "Из списка должен открываться тайтл");
    }

    @Test
    void user_opens_news_or_forum_section() {
        assertTrue(forumPage().open().isForumVisible(),
                "Должен открыться форум со списком записей");
    }

    @Test
    void user_sees_title_collections() {
        assertTrue(titlePage()
                        .openEleceed()
                        .areCollectionsVisible(),
                "На странице тайтла должны быть коллекции");
    }
}
