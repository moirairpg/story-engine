package me.moirai.storyengine.core.domain.world;

import java.time.OffsetDateTime;

import org.springframework.test.util.ReflectionTestUtils;

public class WorldLorebookEntryFixture {

    public static final String PUBLIC_ID = "857345aa-0000-0000-0000-000000000000";
    public static final Long NUMERIC_ID = 1L;

    public static WorldLorebookEntry.Builder sampleLorebookEntry() {

        WorldLorebookEntry.Builder builder = WorldLorebookEntry.builder();
        builder.name("White River");
        builder.description("The White River goes through Falkreath, Whiterun and ends in Eastmarch.");
        builder.regex("[Ww]hite [Rr]iver");
        builder.creatorId("CRTID");
        builder.creationDate(OffsetDateTime.now());
        builder.lastUpdateDate(OffsetDateTime.now());

        return builder;
    }

    public static WorldLorebookEntry.Builder samplePlayerCharacterLorebookEntry() {

        WorldLorebookEntry.Builder builder = WorldLorebookEntry.builder();
        builder.name("Volin Habar");
        builder.description("Volin Habar is a warrior that fights with a sword.");
        builder.regex("[Vv]olin [Hh]abar|[Vv]oha");
        builder.creatorId("CRTID");
        builder.creationDate(OffsetDateTime.now());
        builder.lastUpdateDate(OffsetDateTime.now());

        return builder;
    }

    public static WorldLorebookEntry sampleLorebookEntryWithId() {

        WorldLorebookEntry entry = sampleLorebookEntry().build();
        ReflectionTestUtils.setField(entry, "id", NUMERIC_ID);
        ReflectionTestUtils.setField(entry, "publicId", PUBLIC_ID);
        return entry;
    }
}
