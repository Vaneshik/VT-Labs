package org.example.remanga.pages;

import org.example.remanga.BaseTest;

public class SearchPage extends BasePage {
    private static final String SEARCH_BUTTON = "//button[normalize-space(.)='Что ищем, семпай?']";
    private static final String SEARCH_INPUT = "//input[@placeholder='Что ищем, семпай?']";

    public SearchPage(BaseTest test) {
        super(test);
    }

    public SearchPage openFromHome() {
        click(SEARCH_BUTTON);
        return this;
    }

    public SearchPage search(String query) {
        type(SEARCH_INPUT, query);
        return this;
    }

    public String currentQuery() {
        return value(SEARCH_INPUT);
    }
}
