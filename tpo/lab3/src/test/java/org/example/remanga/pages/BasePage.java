package org.example.remanga.pages;

import org.example.remanga.BaseTest;

abstract class BasePage {
    protected final BaseTest test;

    BasePage(BaseTest test) {
        this.test = test;
    }

    protected BasePage open(String path) {
        test.openPath(path);
        return this;
    }

    protected boolean isVisible(String xpath) {
        return test.isVisible(xpath);
    }

    protected boolean isAnyVisible(String xpath) {
        return test.isAnyVisible(xpath);
    }

    protected void click(String xpath) {
        test.click(xpath);
    }

    protected void type(String xpath, String value) {
        test.type(xpath, value);
    }

    protected String value(String xpath) {
        return test.valueOfFirstVisible(xpath);
    }

    protected String validationMessage(String xpath) {
        return test.validationMessage(xpath);
    }

    protected boolean exists(String xpath) {
        return test.exists(xpath);
    }

    protected boolean urlContains(String... fragments) {
        return test.waitUrlContainsAny(fragments);
    }
}
