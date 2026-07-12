package org.example.remanga;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CommunityPagesTest extends BaseTest {
    @Test
    void user_opens_publisher_page() {
        assertTrue(communityPage().openPublisher().isPublisherVisible(),
                "Должна открыться страница паблишера с названием и блоками");
    }

    @Test
    void user_opens_guild_page() {
        assertTrue(communityPage().openGuild().isGuildVisible(),
                "Должна открыться страница гильдии с названием и разделами");
    }
}
