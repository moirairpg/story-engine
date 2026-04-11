package me.moirai.storyengine.core.domain.adventure;

import java.util.UUID;

import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.enums.Moderation;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.core.domain.PermissionFixture;

public class AdventureFixture {

    public static final Long OWNER_ID = 1111L;
    public static final UUID PUBLIC_ID = UUID.fromString("857345aa-2222-0000-0000-000000000000");
    public static final Long NUMERIC_ID = 2L;

    public static Adventure.Builder privateSingleplayerAdventure() {

        var builder = Adventure.builder();
        builder.name("Name");
        builder.description("This is an RPG world");
        builder.adventureStart("As you enter the city, people around you start looking at you.");
        builder.worldId(UUID.randomUUID());
        builder.personaId(1L);
        builder.moderation(Moderation.STRICT);
        builder.visibility(Visibility.fromString("PRIVATE"));
        builder.modelConfiguration(ModelConfigurationFixture.gpt4Mini());
        builder.isMultiplayer(false);
        builder.contextAttributes(ContextAttributesFixture.sample());
        builder.permissions(new Permission(OWNER_ID, PermissionLevel.OWNER));

        return builder;
    }

    public static Adventure.Builder privateMultiplayerAdventure() {

        var builder = Adventure.builder();
        builder.name("Name");
        builder.description("This is an RPG world");
        builder.adventureStart("As you enter the city, people around you start looking at you.");
        builder.worldId(UUID.randomUUID());
        builder.personaId(1L);
        builder.moderation(Moderation.STRICT);
        builder.visibility(Visibility.fromString("PRIVATE"));
        builder.modelConfiguration(ModelConfigurationFixture.gpt4Mini());
        builder.isMultiplayer(true);
        builder.contextAttributes(ContextAttributesFixture.sample());
        builder.permissions(new Permission(OWNER_ID, PermissionLevel.OWNER));

        return builder;
    }

    public static Adventure.Builder publicSingleplayerAdventure() {

        var builder = Adventure.builder();
        builder.name("Name");
        builder.description("This is an RPG world");
        builder.adventureStart("As you enter the city, people around you start looking at you.");
        builder.worldId(UUID.randomUUID());
        builder.personaId(1L);
        builder.moderation(Moderation.STRICT);
        builder.visibility(Visibility.fromString("PUBLIC"));
        builder.modelConfiguration(ModelConfigurationFixture.gpt4Mini());
        builder.isMultiplayer(false);
        builder.contextAttributes(ContextAttributesFixture.sample());
        builder.permissions(new Permission(OWNER_ID, PermissionLevel.OWNER));

        return builder;
    }

    public static Adventure.Builder publicMultiplayerAdventure() {

        var builder = Adventure.builder();
        builder.name("Name");
        builder.description("This is an RPG world");
        builder.adventureStart("As you enter the city, people around you start looking at you.");
        builder.worldId(UUID.randomUUID());
        builder.personaId(1L);
        builder.moderation(Moderation.STRICT);
        builder.visibility(Visibility.fromString("PUBLIC"));
        builder.modelConfiguration(ModelConfigurationFixture.gpt4Mini());
        builder.isMultiplayer(true);
        builder.contextAttributes(ContextAttributesFixture.sample());
        builder.permissions(new Permission(OWNER_ID, PermissionLevel.OWNER));

        return builder;
    }

    public static Adventure privateMultiplayerAdventureWithId() {

        var adventure = privateMultiplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", PUBLIC_ID);
        return adventure;
    }

    public static Adventure publicMultiplayerAdventureWithId() {

        var adventure = publicMultiplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", PUBLIC_ID);
        return adventure;
    }

    public static Adventure publicMultiplayerAdventureWithIdAndPermissions() {

        var adventure = publicMultiplayerAdventureWithId();
        adventure.permissions().addAll(PermissionFixture.samplePermissions());
        return adventure;
    }

    public static Adventure privateMultiplayerAdventureWithIdAndPermissions() {

        var adventure = privateMultiplayerAdventureWithId();
        adventure.permissions().addAll(PermissionFixture.samplePermissions());
        return adventure;
    }
}
