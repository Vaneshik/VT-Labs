package org.example.remanga;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchAndTitleTest extends BaseTest {
    @Test
    void user_opens_search_and_types_query() {
        homePage().open();

        String query = "Элисед";
        String actual = searchPage()
                .openFromHome()
                .search(query)
                .currentQuery();

        assertEquals(query, actual, "Поисковое поле должно принимать запрос");
    }

    @Test
    void user_opens_title_card_from_home_page() {
        assertTrue(titlePage().openEleceed().isTitlePageVisible(),
                "На странице тайтла должны быть заголовок, кнопка чтения и статистика");
    }
}
