package me.moirai.storyengine.infrastructure.inbound.rest.mapper;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

import org.springframework.stereotype.Component;

import me.moirai.storyengine.core.port.inbound.world.CreateWorld;
import me.moirai.storyengine.core.port.inbound.world.CreateWorldLorebookEntry;
import me.moirai.storyengine.core.port.inbound.world.UpdateWorld;
import me.moirai.storyengine.infrastructure.inbound.rest.request.CreateWorldRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateWorldRequest;

@Component
public class WorldRequestMapper {

    public CreateWorld toCommand(CreateWorldRequest request, String requesterId) {

        return CreateWorld.builder()
                .name(request.getName())
                .description(request.getDescription())
                .adventureStart(request.getAdventureStart())
                .visibility(request.getVisibility())
                .usersAllowedToWrite(request.getUsersAllowedToWrite())
                .usersAllowedToRead(request.getUsersAllowedToRead())
                .requesterId(requesterId)
                .lorebookEntries(emptyIfNull(request.getLorebook()).stream()
                        .map(entry -> CreateWorldLorebookEntry.builder()
                                .name(entry.getName())
                                .description(entry.getDescription())
                                .regex(entry.getRegex())
                                .build())
                        .toList())
                .build();
    }

    public UpdateWorld toCommand(UpdateWorldRequest request, String worldId, String requesterId) {

        return UpdateWorld.builder()
                .id(worldId)
                .name(request.getName())
                .description(request.getDescription())
                .adventureStart(request.getAdventureStart())
                .visibility(request.getVisibility())
                .usersAllowedToWriteToAdd(request.getUsersAllowedToWriteToAdd())
                .usersAllowedToWriteToRemove(request.getUsersAllowedToWriteToRemove())
                .usersAllowedToReadToAdd(request.getUsersAllowedToReadToAdd())
                .usersAllowedToReadToRemove(request.getUsersAllowedToReadToRemove())
                .build();
    }
}
