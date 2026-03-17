package me.moirai.storyengine.core.domain.world;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.common.domain.Permissions;
import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.core.domain.PermissionsFixture;

public class WorldFixture {

    private static final String OWNER_DISCORD_ID = "586678721356875";
    public static final UUID PUBLIC_ID = UUID.fromString("857345aa-0000-0000-0000-000000000000");
    public static final Long NUMERIC_ID = 1L;

    public static World.Builder publicWorld() {

        World.Builder builder = World.builder();
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

    public static World publicWorldWithId() {

        World world = publicWorld().build();
        ReflectionTestUtils.setField(world, "id", NUMERIC_ID);
        ReflectionTestUtils.setField(world, "publicId", PUBLIC_ID);
        return world;
    }

    public static World privateWorldWithId() {

        World world = privateWorld().build();
        ReflectionTestUtils.setField(world, "id", NUMERIC_ID);
        ReflectionTestUtils.setField(world, "publicId", PUBLIC_ID);
        return world;
    }
}
