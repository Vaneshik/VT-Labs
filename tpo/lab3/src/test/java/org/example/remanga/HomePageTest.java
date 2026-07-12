package org.example.remanga;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HomePageTest extends BaseTest {
    @Test
    void home_page_contains_navigation_and_title_sections() {
        assertTrue(homePage().open().isMainSectionsVisible(),
                "На главной должны быть навигация, поиск и карточки тайтлов");
    }
}
