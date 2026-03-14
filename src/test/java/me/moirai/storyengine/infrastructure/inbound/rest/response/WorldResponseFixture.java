package me.moirai.storyengine.infrastructure.inbound.rest.response;

import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;

public class WorldResponseFixture {

    public static WorldResponse.Builder publicWorld() {

        World world = WorldFixture.publicWorld().build();

        return WorldResponse.builder()
                .id(world.getId())
                .name(world.getName())
                .description(world.getDescription())
                .adventureStart(world.getAdventureStart())
                .visibility(world.getVisibility().toString())
                .ownerId(world.getOwnerId())
                .creationDate(world.getCreationDate())
                .usersAllowedToRead(world.getUsersAllowedToRead())
                .usersAllowedToWrite(world.getUsersAllowedToWrite());
    }

    public static WorldResponse.Builder privateWorld() {

        World world = WorldFixture.privateWorld().build();

        return WorldResponse.builder()
                .id(world.getId())
                .name(world.getName())
                .description(world.getDescription())
                .adventureStart(world.getAdventureStart())
                .visibility(world.getVisibility().toString())
                .ownerId(world.getOwnerId())
                .creationDate(world.getCreationDate())
                .usersAllowedToRead(world.getUsersAllowedToRead())
                .usersAllowedToWrite(world.getUsersAllowedToWrite());
    }
}
