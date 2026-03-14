package me.moirai.storyengine.core.application.usecase.adventure.request;

public class CreateAdventureLorebookEntryFixture {

    public static CreateAdventureLorebookEntry.Builder sampleLorebookEntry() {

        return CreateAdventureLorebookEntry.builder()
                .name("Volin Habar")
                .description("Volin Habar is a warrior that fights with a sword.")
                .regex("[Vv]olin [Hh]abar|[Vv]oha")
                .adventureId("ADVID")
                .requesterId("1234");
    }

    public static CreateAdventureLorebookEntry.Builder samplePlayerCharacterLorebookEntry() {

        return CreateAdventureLorebookEntry.builder()
                .name("Volin Habar")
                .description("Volin Habar is a warrior that fights with a sword.")
                .regex("[Vv]olin [Hh]abar|[Vv]oha")
                .playerId("2423423423423")
                .adventureId("ADVID")
                .requesterId("1234");
    }
}
