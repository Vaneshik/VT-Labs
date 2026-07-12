package org.example.remanga.pages;

import org.example.remanga.BaseTest;

public class TitlePage extends BasePage {
    static final String ELECEED_PATH = "/manga/%3C29.04.2026%3Eeleceed_/main";
    private static final String TAB_TEMPLATE = "//button[@role='tab' and contains(normalize-space(.), '%s')]";
    private static final String TITLE_HEADER = "//h1[normalize-space(.)='Элисед']";

    public TitlePage(BaseTest test) {
        super(test);
    }

    public TitlePage openEleceed() {
        open(ELECEED_PATH);
        return this;
    }

    public TitlePage openFirstFromCurrentPage() {
        test.openFirstTitleFromCurrentPage();
        return this;
    }

    public TitlePage openChapters() {
        click(tab("Главы"));
        return this;
    }

    public TitlePage openDiscussions() {
        click(tab("Обсуждения"));
        return this;
    }

    public TitlePage openMoments() {
        click(tab("Моменты"));
        return this;
    }

    public TitlePage openCards() {
        click(tab("Карты"));
        return this;
    }

    public TitlePage openCharacters() {
        click(tab("Персонажи"));
        return this;
    }

    public TitlePage openVoiceover() {
        click(tab("Озвучка"));
        return this;
    }

    public boolean isTitlePageVisible() {
        return !test.browserTitle().isBlank()
                && isVisible(TITLE_HEADER)
                && isVisible("//a[normalize-space(.)='Читать' and contains(@href, '/manga/')]")
                && isVisible("//button[contains(normalize-space(.), 'Главы')]")
                && isVisible("//*[contains(normalize-space(.), 'Лайков:') and contains(normalize-space(.), 'Просмотров:')]");
    }

    public boolean isOpenedFromList() {
        return isVisible("//h1 | //a[normalize-space(.)='Читать'] | //button[contains(normalize-space(.), 'Главы')]");
    }

    public boolean isBookmarkActionVisible() {
        return isVisible("//button[contains(normalize-space(.), 'В закладки')]"
                + " | //button[contains(normalize-space(.), 'Заклад')]");
    }

    public boolean areCollectionsVisible() {
        return isVisible("//*[normalize-space(.)='Коллекции по произведению']")
                && isAnyVisible("//a[contains(@href, '/collections/') and string-length(normalize-space(.)) > 1]");
    }

    public boolean isChaptersTabOpen() {
        return urlContains("/chapters")
                && isVisible(TITLE_HEADER)
                && isVisible("//input[@placeholder='Поиск']")
                && isAnyVisible("//a[contains(@href, '/manga/') and contains(normalize-space(.), 'Глава')]");
    }

    public boolean isDiscussionsTabOpen() {
        return urlContains("/posts")
                && isVisible(TITLE_HEADER)
                && isVisible("//a[contains(@href, '/forum/create') and contains(normalize-space(.), 'Создать обсуждение')]"
                        + " | //*[contains(normalize-space(.), 'Создать обсуждение')]")
                && isVisible("//a[contains(@href, '/forum/feed') and contains(normalize-space(.), 'На форум')]"
                        + " | //*[contains(normalize-space(.), 'На форум')]");
    }

    public boolean isMomentsTabOpen() {
        return urlContains("/moments")
                && isVisible(TITLE_HEADER)
                && isVisible(tab("Моменты"))
                && isAnyVisible("//*[string-length(normalize-space(.)) > 20 and not(self::script)]");
    }

    public boolean isCardsTabOpen() {
        return urlContains("/cards")
                && isVisible("//*[contains(normalize-space(.), 'Прогресс коллекции карт')]")
                && isVisible(tab("Карты"));
    }

    public boolean isCharactersTabOpen() {
        return urlContains("/characters")
                && isVisible(TITLE_HEADER)
                && isAnyVisible("//a[contains(@href, '/characters') or contains(normalize-space(.), 'Кайден') or contains(normalize-space(.), 'Со Джи У')]");
    }

    public boolean isVoiceoverTabOpen() {
        return urlContains("/voiceover")
                && isVisible(TITLE_HEADER)
                && isVisible("//button[@role='tab' and normalize-space(.)='Озвучка']")
                && isVisible("//*[contains(normalize-space(.), 'Оставить комментарий') or contains(normalize-space(.), 'Sygest')]");
    }

    private static String tab(String label) {
        return String.format(TAB_TEMPLATE, label);
    }
}
