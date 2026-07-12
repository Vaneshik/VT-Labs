package org.example.remanga.pages;

import org.example.remanga.BaseTest;

public class CommunityPage extends BasePage {
    public CommunityPage(BaseTest test) {
        super(test);
    }

    public CommunityPage openPublisher() {
        open("/publisher/kartnadzor/about");
        return this;
    }

    public CommunityPage openGuild() {
        open("/guild/testovaja-gi-4435669b/about");
        return this;
    }

    public boolean isPublisherVisible() {
        return isVisible("//*[contains(normalize-space(.), 'Картнадзор')]")
                && isVisible("//*[contains(normalize-space(.), 'Контакты')]")
                && isVisible("//*[contains(normalize-space(.), 'Участники')]")
                && isVisible("//*[contains(normalize-space(.), 'Подписчики')]");
    }

    public boolean isGuildVisible() {
        return isVisible("//*[contains(normalize-space(.), 'НЕОфициальная РЕманга')]")
                && isVisible("//*[contains(normalize-space(.), 'О гильдии')]")
                && isVisible("//*[contains(normalize-space(.), 'Бонусы')]")
                && isVisible("//*[contains(normalize-space(.), 'Состав')]");
    }
}
