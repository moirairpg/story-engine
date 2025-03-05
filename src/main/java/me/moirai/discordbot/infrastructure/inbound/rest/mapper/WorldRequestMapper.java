package me.moirai.discordbot.infrastructure.inbound.rest.mapper;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

import org.springframework.stereotype.Component;

import me.moirai.discordbot.core.application.usecase.world.request.CreateWorld;
import me.moirai.discordbot.core.application.usecase.world.request.CreateWorldLorebookEntry;
import me.moirai.discordbot.core.application.usecase.world.request.UpdateWorld;
import me.moirai.discordbot.infrastructure.inbound.rest.request.CreateWorldRequest;
import me.moirai.discordbot.infrastructure.inbound.rest.request.UpdateWorldRequest;

@Component
public class WorldRequestMapper {

    public CreateWorld toCommand(CreateWorldRequest request, String requesterDiscordId) {

        return CreateWorld.builder()
                .name(request.getName())
                .description(request.getDescription())
                .adventureStart(request.getAdventureStart())
                .visibility(request.getVisibility())
                .usersAllowedToWrite(request.getUsersAllowedToWrite())
                .usersAllowedToRead(request.getUsersAllowedToRead())
                .requesterDiscordId(requesterDiscordId)
                .lorebookEntries(emptyIfNull(request.getLorebook()).stream()
                        .map(entry -> CreateWorldLorebookEntry.builder()
                                .name(entry.getName())
                                .description(entry.getDescription())
                                .regex(entry.getRegex())
                                .build())
                        .toList())
                .build();
    }

    public UpdateWorld toCommand(UpdateWorldRequest request, String worldId, String requesterDiscordId) {

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
