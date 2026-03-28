package me.moirai.storyengine.core.domain.world;

import java.util.UUID;

import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.core.domain.PermissionFixture;

public class WorldFixture {

    public static final Long OWNER_ID = 1111L;
    public static final UUID PUBLIC_ID = UUID.fromString("857345aa-0000-0000-0000-000000000000");
    public static final Long NUMERIC_ID = 1L;

    public static World.Builder publicWorld() {

        var builder = World.builder();
        builder.name("MoirAI");
        builder.description("This is an RPG world");
        builder.adventureStart("As you enter the city, people around you start looking at you.");
        builder.visibility(Visibility.fromString("PUBLIC"));
        builder.permissions(new Permission(OWNER_ID, PermissionLevel.OWNER));

        return builder;
    }

    public static World.Builder privateWorld() {

        var builder = World.builder();
        builder.name("MoirAI");
        builder.description("This is an RPG world");
        builder.adventureStart("As you enter the city, people around you start looking at you.");
        builder.visibility(Visibility.fromString("PRIVATE"));
        builder.permissions(new Permission(OWNER_ID, PermissionLevel.OWNER));

        return builder;
    }

    public static World publicWorldWithId() {

        var world = publicWorld().build();
        ReflectionTestUtils.setField(world, "id", NUMERIC_ID);
        ReflectionTestUtils.setField(world, "publicId", PUBLIC_ID);

        return world;
    }

    public static World privateWorldWithId() {

        var world = privateWorld().build();
        ReflectionTestUtils.setField(world, "id", NUMERIC_ID);
        ReflectionTestUtils.setField(world, "publicId", PUBLIC_ID);

        return world;
    }

    public static World publicWorldWithIdAndPermissions() {

        var world = publicWorldWithId();
        world.permissions().addAll(PermissionFixture.samplePermissions());

        return world;
    }

    public static World privateWorldWithIdAndPermissions() {

        var world = privateWorldWithId();
        world.permissions().addAll(PermissionFixture.samplePermissions());

        return world;
    }
}
