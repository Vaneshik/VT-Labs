package org.example.remanga;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TitleTabsTest extends BaseTest {
    @Test
    void user_opens_title_chapters_tab() {
        assertTrue(titlePage()
                        .openEleceed()
                        .openChapters()
                        .isChaptersTabOpen(),
                "Должна открыться вкладка глав с поиском и списком");
    }

    @Test
    void user_opens_title_discussions_tab() {
        assertTrue(titlePage()
                        .openEleceed()
                        .openDiscussions()
                        .isDiscussionsTabOpen(),
                "Должна открыться вкладка обсуждений");
    }

    @Test
    void user_opens_title_moments_tab() {
        assertTrue(titlePage()
                        .openEleceed()
                        .openMoments()
                        .isMomentsTabOpen(),
                "Должна открыться вкладка моментов");
    }

    @Test
    void user_opens_title_cards_tab() {
        assertTrue(titlePage()
                        .openEleceed()
                        .openCards()
                        .isCardsTabOpen(),
                "Должна открыться вкладка карт");
    }

    @Test
    void user_opens_title_characters_tab() {
        assertTrue(titlePage()
                        .openEleceed()
                        .openCharacters()
                        .isCharactersTabOpen(),
                "Должна открыться вкладка персонажей");
    }

    @Test
    void user_opens_title_voiceover_tab() {
        assertTrue(titlePage()
                        .openEleceed()
                        .openVoiceover()
                        .isVoiceoverTabOpen(),
                "Должна открыться вкладка озвучки");
    }
}
