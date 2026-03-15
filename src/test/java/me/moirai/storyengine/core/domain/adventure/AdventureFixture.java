package me.moirai.storyengine.core.domain.adventure;

import java.time.OffsetDateTime;

import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.common.domain.Visibility;
import me.moirai.storyengine.core.domain.PermissionsFixture;

public class AdventureFixture {

    private static final String OWNER_DISCORD_ID = "586678721356875";
    public static final String PUBLIC_ID = "857345aa-2222-0000-0000-000000000000";
    public static final Long NUMERIC_ID = 2L;

    public static Adventure.Builder privateSingleplayerAdventure() {

        Adventure.Builder builder = Adventure.builder();
        builder.name("Name");
        builder.description("This is an RPG world");
        builder.adventureStart("As you enter the city, people around you start looking at you.");
        builder.worldId(1L);
        builder.personaId(1L);
        builder.channelId("CHNLID");
        builder.moderation(Moderation.STRICT);
        builder.visibility(Visibility.fromString("PRIVATE"));
        builder.modelConfiguration(ModelConfigurationFixture.gpt4Mini().build());
        builder.permissions(PermissionsFixture.samplePermissions().build());
        builder.creatorId(OWNER_DISCORD_ID);
        builder.creationDate(OffsetDateTime.now());
        builder.lastUpdateDate(OffsetDateTime.now());
        builder.channelId("12345");
        builder.gameMode(GameMode.RPG);
        builder.isMultiplayer(false);
        builder.contextAttributes(ContextAttributesFixture.sample().build());
        builder.version(1);

        return builder;
    }

    public static Adventure.Builder privateMultiplayerAdventure() {

        Adventure.Builder builder = Adventure.builder();
        builder.name("Name");
        builder.description("This is an RPG world");
        builder.adventureStart("As you enter the city, people around you start looking at you.");
        builder.worldId(1L);
        builder.personaId(1L);
        builder.channelId("CHNLID");
        builder.moderation(Moderation.STRICT);
        builder.visibility(Visibility.fromString("PRIVATE"));
        builder.modelConfiguration(ModelConfigurationFixture.gpt4Mini().build());
        builder.permissions(PermissionsFixture.samplePermissions().build());
        builder.creatorId(OWNER_DISCORD_ID);
        builder.creationDate(OffsetDateTime.now());
        builder.lastUpdateDate(OffsetDateTime.now());
        builder.channelId("12345");
        builder.gameMode(GameMode.RPG);
        builder.isMultiplayer(true);
        builder.contextAttributes(ContextAttributesFixture.sample().build());
        builder.version(1);

        return builder;
    }

    public static Adventure.Builder publicSingleplayerAdventure() {

        Adventure.Builder builder = Adventure.builder();
        builder.name("Name");
        builder.description("This is an RPG world");
        builder.adventureStart("As you enter the city, people around you start looking at you.");
        builder.worldId(1L);
        builder.personaId(1L);
        builder.channelId("CHNLID");
        builder.moderation(Moderation.STRICT);
        builder.visibility(Visibility.fromString("PUBLIC"));
        builder.modelConfiguration(ModelConfigurationFixture.gpt4Mini().build());
        builder.permissions(PermissionsFixture.samplePermissions().build());
        builder.creatorId(OWNER_DISCORD_ID);
        builder.creationDate(OffsetDateTime.now());
        builder.lastUpdateDate(OffsetDateTime.now());
        builder.channelId("12345");
        builder.gameMode(GameMode.RPG);
        builder.isMultiplayer(false);
        builder.contextAttributes(ContextAttributesFixture.sample().build());
        builder.version(1);

        return builder;
    }

    public static Adventure.Builder publicMultiplayerAdventure() {

        Adventure.Builder builder = Adventure.builder();
        builder.name("Name");
        builder.description("This is an RPG world");
        builder.adventureStart("As you enter the city, people around you start looking at you.");
        builder.worldId(1L);
        builder.personaId(1L);
        builder.channelId("CHNLID");
        builder.moderation(Moderation.STRICT);
        builder.visibility(Visibility.fromString("PUBLIC"));
        builder.modelConfiguration(ModelConfigurationFixture.gpt4Mini().build());
        builder.permissions(PermissionsFixture.samplePermissions().build());
        builder.creatorId(OWNER_DISCORD_ID);
        builder.creationDate(OffsetDateTime.now());
        builder.lastUpdateDate(OffsetDateTime.now());
        builder.channelId("12345");
        builder.gameMode(GameMode.RPG);
        builder.isMultiplayer(true);
        builder.contextAttributes(ContextAttributesFixture.sample().build());
        builder.version(1);

        return builder;
    }

    public static Adventure privateMultiplayerAdventureWithId() {

        Adventure adventure = privateMultiplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", PUBLIC_ID);
        return adventure;
    }

    public static Adventure publicMultiplayerAdventureWithId() {

        Adventure adventure = publicMultiplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", PUBLIC_ID);
        return adventure;
    }
}
