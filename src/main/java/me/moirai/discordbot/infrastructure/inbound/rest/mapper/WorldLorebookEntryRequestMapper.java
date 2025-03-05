package me.moirai.discordbot.infrastructure.inbound.rest.mapper;

import org.springframework.stereotype.Component;

import me.moirai.discordbot.core.application.usecase.world.request.CreateWorldLorebookEntry;
import me.moirai.discordbot.core.application.usecase.world.request.DeleteWorldLorebookEntry;
import me.moirai.discordbot.core.application.usecase.world.request.UpdateWorldLorebookEntry;
import me.moirai.discordbot.infrastructure.inbound.rest.request.CreateLorebookEntryRequest;
import me.moirai.discordbot.infrastructure.inbound.rest.request.UpdateLorebookEntryRequest;

@Component
public class WorldLorebookEntryRequestMapper {

    public CreateWorldLorebookEntry toCommand(CreateLorebookEntryRequest request,
            String worldId, String requesterDiscordId) {

        return CreateWorldLorebookEntry.builder()
                .name(request.getName())
                .description(request.getDescription())
                .regex(request.getRegex())
                .worldId(worldId)
                .requesterDiscordId(requesterDiscordId)
                .build();
    }

    public UpdateWorldLorebookEntry toCommand(UpdateLorebookEntryRequest request, String entryId,
            String worldId, String requesterDiscordId) {

        return UpdateWorldLorebookEntry.builder()
                .id(entryId)
                .name(request.getName())
                .description(request.getDescription())
                .regex(request.getRegex())
                .requesterDiscordId(requesterDiscordId)
                .worldId(worldId)
                .build();
    }

    public DeleteWorldLorebookEntry toCommand(String entryId, String worldId, String requesterId) {

        return DeleteWorldLorebookEntry.builder()
                .lorebookEntryId(entryId)
                .worldId(worldId)
                .requesterDiscordId(requesterId)
                .build();
    }
}
