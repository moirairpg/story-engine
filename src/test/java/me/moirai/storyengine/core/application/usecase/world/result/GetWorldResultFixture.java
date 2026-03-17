package me.moirai.storyengine.core.application.usecase.world.result;

import me.moirai.storyengine.core.port.inbound.world.WorldDetails;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;

public class GetWorldResultFixture {

    public static WorldDetails publicWorld() {

        World world = WorldFixture.publicWorld().build();

        return new WorldDetails(
                world.getPublicId(),
                world.getName(),
                world.getDescription(),
                world.getAdventureStart(),
                world.getVisibility().name(),
                world.getOwnerId(),
                world.getUsersAllowedToRead(),
                world.getUsersAllowedToWrite(),
                world.getCreationDate(),
                null);
    }

    public static WorldDetails privateWorld() {

        World world = WorldFixture.privateWorld().build();

        return new WorldDetails(
                world.getPublicId(),
                world.getName(),
                world.getDescription(),
                world.getAdventureStart(),
                world.getVisibility().name(),
                world.getOwnerId(),
                world.getUsersAllowedToRead(),
                world.getUsersAllowedToWrite(),
                world.getCreationDate(),
                null);
    }
}
