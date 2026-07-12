package org.example.remanga.pages;

import org.example.remanga.BaseTest;

public class CatalogPage extends BasePage {
    private static final String TITLE_FILTER = "//input[@placeholder='Поиск по названию']";

    public CatalogPage(BaseTest test) {
        super(test);
    }

    public CatalogPage open() {
        open("/catalog");
        return this;
    }

    public CatalogPage openFilters() {
        click("//button[normalize-space(.)='Фильтры']");
        return this;
    }

    public CatalogPage applyManhwaFilter() {
        click("//button[normalize-space(.)='Манхва']");
        return this;
    }

    public CatalogPage typeTitleFilter(String title) {
        type(TITLE_FILTER, title);
        return this;
    }

    public boolean isCatalogVisible() {
        return (urlContains("/catalog") || exists("//*[contains(normalize-space(.), 'Фильтры')]"))
                && isVisible("//*[normalize-space(.)='Каталог']")
                && isVisible("//button[normalize-space(.)='По популярности']")
                && isVisible("//button[normalize-space(.)='Манга']")
                && isVisible("//button[normalize-space(.)='Фильтры']")
                && areTitlesVisible();
    }

    public boolean areTitlesVisible() {
        return isAnyVisible("//a[contains(@href, '/manga/') and contains(@href, '/main') and string-length(normalize-space(.)) > 1]");
    }

    public boolean areAdvancedFiltersVisible() {
        return isVisible(TITLE_FILTER)
                && isVisible("//input[@placeholder='От']")
                && isVisible("//input[@placeholder='До']")
                && isVisible("//button[contains(normalize-space(.), 'Сбросить фильтры')]")
                && isVisible("//*[normalize-space(.)='Жанры']");
    }

    public String titleFilterValue() {
        return value(TITLE_FILTER);
    }
}
