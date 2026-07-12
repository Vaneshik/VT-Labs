package org.example.remanga;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogAdvancedTest extends BaseTest {
    @Test
    void user_opens_advanced_catalog_filters() {
        assertTrue(catalogPage()
                        .open()
                        .openFilters()
                        .areAdvancedFiltersVisible(),
                "В расширенных фильтрах должны быть поиск, годы, жанры и сброс");
    }

    @Test
    void user_types_title_into_catalog_filter() {
        String title = "Элисед";
        String actual = catalogPage()
                .open()
                .openFilters()
                .typeTitleFilter(title)
                .titleFilterValue();

        assertEquals(title, actual, "Поле поиска по каталогу должно принимать название тайтла");
    }
}
