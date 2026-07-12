package org.example.remanga;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogTest extends BaseTest {
    @Test
    void user_opens_catalog_and_sees_filters_with_titles() {
        assertTrue(catalogPage().open().isCatalogVisible(),
                "После перехода должен открыться каталог с сортировкой, фильтрами и тайтлами");
    }

    @Test
    void user_applies_available_catalog_filter() {
        assertTrue(catalogPage()
                        .open()
                        .applyManhwaFilter()
                        .areTitlesVisible(),
                "После применения фильтра каталог должен показывать тайтлы");
    }
}
