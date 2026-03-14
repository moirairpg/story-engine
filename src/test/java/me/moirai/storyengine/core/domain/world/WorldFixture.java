package me.moirai.storyengine.core.domain.world;

import java.time.OffsetDateTime;

import me.moirai.storyengine.core.domain.Permissions;
import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.domain.Visibility;

public class WorldFixture {

    private static final String OWNER_DISCORD_ID = "586678721356875";

    public static World.Builder publicWorld() {

        World.Builder builder = World.builder();
        builder.id("857345HAA");
        builder.name("MoirAI");
        builder.description("This is an RPG world");
        builder.adventureStart("As you enter the city, people around you start looking at you.");
        builder.visibility(Visibility.fromString("PUBLIC"));
        builder.creatorId(OWNER_DISCORD_ID);
        builder.creationDate(OffsetDateTime.now());
        builder.lastUpdateDate(OffsetDateTime.now());

        Permissions permissions = PermissionsFixture.samplePermissions().build();
        builder.permissions(permissions);

        return builder;
    }

    public static World.Builder privateWorld() {

        World.Builder builder = World.builder();
        builder.id("857345HAA");
        builder.name("MoirAI");
        builder.description("This is an RPG world");
        builder.adventureStart("As you enter the city, people around you start looking at you.");
        builder.visibility(Visibility.fromString("PRIVATE"));
        builder.creatorId(OWNER_DISCORD_ID);
        builder.creationDate(OffsetDateTime.now());
        builder.lastUpdateDate(OffsetDateTime.now());

        Permissions permissions = PermissionsFixture.samplePermissions().build();
        builder.permissions(permissions);

        return builder;
    }
}
