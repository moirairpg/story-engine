package me.moirai.storyengine.core.port.inbound;

import me.moirai.storyengine.core.port.inbound.world.WorldDetails;
import me.moirai.storyengine.core.domain.world.WorldFixture;

public class GetWorldResultFixture {

    public static WorldDetails publicWorld() {

        var world = WorldFixture.publicWorldWithIdAndPermissions();
        return new WorldDetails(
                world.getPublicId(),
                world.getName(),
                world.getDescription(),
                world.getAdventureStart(),
                world.getVisibility().name(),
                world.getPermissions(),
                world.getCreationDate(),
                world.getLastUpdateDate());
    }

    public static WorldDetails privateWorld() {

        var world = WorldFixture.privateWorldWithIdAndPermissions();
        return new WorldDetails(
                world.getPublicId(),
                world.getName(),
                world.getDescription(),
                world.getAdventureStart(),
                world.getVisibility().name(),
                world.getPermissions(),
                world.getCreationDate(),
                world.getLastUpdateDate());
    }
}
