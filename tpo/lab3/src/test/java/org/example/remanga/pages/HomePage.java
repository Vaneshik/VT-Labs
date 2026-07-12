package org.example.remanga.pages;

import org.example.remanga.BaseTest;

public class HomePage extends BasePage {
    public HomePage(BaseTest test) {
        super(test);
    }

    public HomePage open() {
        test.openHomePage();
        return this;
    }

    public boolean isMainSectionsVisible() {
        return isVisible("//a[contains(normalize-space(.), 'Каталог')]")
                && isVisible("//a[contains(normalize-space(.), 'Топы') and contains(@href, '/manga/top')]")
                && isVisible("//a[contains(normalize-space(.), 'Форум') and contains(@href, '/forum/feed')]")
                && isVisible("//button[normalize-space(.)='Что ищем, семпай?']")
                && isVisible("//*[contains(normalize-space(.), 'Горячие новинки') or contains(normalize-space(.), 'Твой чит-код на ReManga')]")
                && isAnyVisible("//a[contains(@href, '/manga/') and contains(@href, '/main') and string-length(normalize-space(.)) > 1]");
    }
}
