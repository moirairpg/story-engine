package me.moirai.storyengine.core.application.usecase.adventure.request;

import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureLorebookEntry;

public class UpdateAdventureLorebookEntryFixture {

    public static UpdateAdventureLorebookEntry.Builder sampleLorebookEntry() {

        return UpdateAdventureLorebookEntry.builder()
                .name("Volin Habar")
                .description("Volin Habar is a warrior that fights with a sword.")
                .regex("[Vv]olin [Hh]abar|[Vv]oha")
                .adventureId("ADVID")
                .requesterId("1234")
                .adventureId("123123")
                .id("123123");
    }

    public static UpdateAdventureLorebookEntry.Builder samplePlayerCharacterLorebookEntry() {

        return UpdateAdventureLorebookEntry.builder()
                .name("Volin Habar")
                .description("Volin Habar is a warrior that fights with a sword.")
                .regex("[Vv]olin [Hh]abar|[Vv]oha")
                .playerId("2423423423423")
                .adventureId("ADVID")
                .requesterId("1234")
                .adventureId("123123")
                .id("123123");
    }
}
