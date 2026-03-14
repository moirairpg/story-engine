package me.moirai.storyengine.infrastructure.inbound.rest.request;

import static org.assertj.core.util.Lists.list;

import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;

public class CreateWorldRequestFixture {

    public static CreateWorldRequest createPrivateWorld() {

        World world = WorldFixture.privateWorld().build();

        CreateLorebookEntryRequest lorebookEntryRequest = new CreateLorebookEntryRequest();
        lorebookEntryRequest.setName("Name");
        lorebookEntryRequest.setDescription("Description");
        lorebookEntryRequest.setRegex("Name");

        CreateWorldRequest request = new CreateWorldRequest();

        request.setName(world.getName());
        request.setDescription(world.getDescription());
        request.setAdventureStart(world.getAdventureStart());
        request.setVisibility(world.getVisibility().toString());
        request.setUsersAllowedToWrite(world.getUsersAllowedToWrite());
        request.setUsersAllowedToRead(world.getUsersAllowedToRead());
        request.setUsersAllowedToRead(world.getUsersAllowedToRead());
        request.setUsersAllowedToWrite(world.getUsersAllowedToWrite());
        request.setLorebook(list(lorebookEntryRequest));

        return request;
    }
}
