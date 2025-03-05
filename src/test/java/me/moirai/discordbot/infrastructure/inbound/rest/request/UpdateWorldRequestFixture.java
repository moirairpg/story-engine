package me.moirai.discordbot.infrastructure.inbound.rest.request;

import me.moirai.discordbot.core.domain.world.World;
import me.moirai.discordbot.core.domain.world.WorldFixture;

public class UpdateWorldRequestFixture {

    public static UpdateWorldRequest createPrivateWorld() {

        World world = WorldFixture.privateWorld().build();
        UpdateWorldRequest request = new UpdateWorldRequest();

        request.setName(world.getName());
        request.setDescription(world.getDescription());
        request.setAdventureStart(world.getAdventureStart());
        request.setVisibility(world.getVisibility().toString());
        request.setUsersAllowedToWriteToAdd(world.getUsersAllowedToWrite());
        request.setUsersAllowedToReadToAdd(world.getUsersAllowedToRead());
        request.setUsersAllowedToWriteToRemove(world.getUsersAllowedToWrite());
        request.setUsersAllowedToReadToRemove(world.getUsersAllowedToRead());

        return request;
    }
}
