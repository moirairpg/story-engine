package me.moirai.storyengine.core.domain.adventure;

import java.time.OffsetDateTime;

import org.springframework.test.util.ReflectionTestUtils;

public class AdventureLorebookEntryFixture {

    public static final String PUBLIC_ID = "857345aa-3333-0000-0000-000000000000";
    public static final Long NUMERIC_ID = 3L;

    public static AdventureLorebookEntry.Builder sampleLorebookEntry() {

        AdventureLorebookEntry.Builder builder = AdventureLorebookEntry.builder();
        builder.name("White River");
        builder.description("The White River goes through Falkreath, Whiterun and ends in Eastmarch.");
        builder.regex("[Ww]hite [Rr]iver");
        builder.creatorId("CRTID");
        builder.creationDate(OffsetDateTime.now());
        builder.lastUpdateDate(OffsetDateTime.now());
        builder.isPlayerCharacter(false);
        builder.version(1);

        return builder;
    }

    public static AdventureLorebookEntry.Builder samplePlayerCharacterLorebookEntry() {

        AdventureLorebookEntry.Builder builder = AdventureLorebookEntry.builder();
        builder.name("Volin Habar");
        builder.description("Volin Habar is a warrior that fights with a sword.");
        builder.regex("[Vv]olin [Hh]abar|[Vv]oha");
        builder.playerId("2423423423423");
        builder.creatorId("CRTID");
        builder.creationDate(OffsetDateTime.now());
        builder.lastUpdateDate(OffsetDateTime.now());
        builder.isPlayerCharacter(true);
        builder.version(1);

        return builder;
    }

    public static AdventureLorebookEntry sampleLorebookEntryWithId() {

        AdventureLorebookEntry entry = sampleLorebookEntry().build();
        ReflectionTestUtils.setField(entry, "id", NUMERIC_ID);
        ReflectionTestUtils.setField(entry, "publicId", PUBLIC_ID);
        return entry;
    }
}
