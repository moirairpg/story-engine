package me.moirai.storyengine.core.application.usecase.world.result;

import me.moirai.storyengine.core.port.inbound.world.WorldDetails;

import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;

public class GetWorldResultFixture {

    public static WorldDetails.Builder publicWorld() {

        World world = WorldFixture.publicWorld().build();

        return WorldDetails.builder()
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

    public static WorldDetails.Builder privateWorld() {

        World world = WorldFixture.privateWorld().build();

        return WorldDetails.builder()
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
