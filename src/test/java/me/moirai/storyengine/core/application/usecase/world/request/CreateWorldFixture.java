package me.moirai.storyengine.core.application.usecase.world.request;

import java.util.Collections;

import me.moirai.storyengine.core.port.inbound.world.CreateWorld;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;

public class CreateWorldFixture {

    public static CreateWorld createPrivateWorld() {

        World world = WorldFixture.privateWorld().build();
        return new CreateWorld(
                world.getName(),
                world.getDescription(),
                world.getAdventureStart(),
                world.getVisibility(),
                Collections.emptyList(),
                world.getUsersAllowedToWrite(),
                world.getUsersAllowedToRead(),
                world.getOwnerId());
    }
}
