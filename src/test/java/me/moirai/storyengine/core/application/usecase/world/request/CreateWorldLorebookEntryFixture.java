package me.moirai.storyengine.core.application.usecase.world.request;

import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.world.CreateWorldLorebookEntry;

public class CreateWorldLorebookEntryFixture {

    public static CreateWorldLorebookEntry sampleLorebookEntry() {

        return new CreateWorldLorebookEntry(
                WorldFixture.PUBLIC_ID,
                "White River",
                "[Ww]hite [Rr]iver",
                "The White River goes through Falkreath, Whiterun and ends in Eastmarch.",
                null);
    }

    public static CreateWorldLorebookEntry samplePlayerCharacterLorebookEntry() {

        return new CreateWorldLorebookEntry(
                WorldFixture.PUBLIC_ID,
                "Volin Habar",
                "[Vv]olin [Hh]abar|[Vv]oha",
                "Volin Habar is a warrior that fights with a sword.",
                null);
    }
}
