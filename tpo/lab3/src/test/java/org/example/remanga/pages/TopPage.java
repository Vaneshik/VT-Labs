package org.example.remanga.pages;

import org.example.remanga.BaseTest;

public class TopPage extends BasePage {
    public TopPage(BaseTest test) {
        super(test);
    }

    public TopPage open() {
        open("/manga/top");
        return this;
    }

    public TopPage openFirstTitle() {
        test.openFirstTitleFromCurrentPage();
        return this;
    }

    public boolean areTopsVisible() {
        return isVisible("//*[normalize-space(.)='Топы']")
                && isVisible("//a[normalize-space(.)='Новинок' and contains(@href, 'period=new')]")
                && isVisible("//a[normalize-space(.)='Месяца' and contains(@href, 'period=monthly')]")
                && isAnyVisible("//a[contains(@href, '/manga/') and contains(@href, '/main') and string-length(normalize-space(.)) > 1]");
    }
}
