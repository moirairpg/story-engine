package me.moirai.storyengine.core.port.inbound;

import java.util.Collections;
import java.util.Set;

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
                world.getNarrator().narratorName(),
                world.getNarrator().narratorPersonality(),
                world.getVisibility(),
                null,
                null,
                Collections.emptyList(),
                Set.of());
    }
}
