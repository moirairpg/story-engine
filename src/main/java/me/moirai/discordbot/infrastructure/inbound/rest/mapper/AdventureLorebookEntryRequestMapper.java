package me.moirai.discordbot.infrastructure.inbound.rest.mapper;

import org.springframework.stereotype.Component;

import me.moirai.discordbot.core.application.usecase.adventure.request.CreateAdventureLorebookEntry;
import me.moirai.discordbot.core.application.usecase.adventure.request.DeleteAdventureLorebookEntry;
import me.moirai.discordbot.core.application.usecase.adventure.request.UpdateAdventureLorebookEntry;
import me.moirai.discordbot.infrastructure.inbound.rest.request.CreateLorebookEntryRequest;
import me.moirai.discordbot.infrastructure.inbound.rest.request.UpdateLorebookEntryRequest;

@Component
public class AdventureLorebookEntryRequestMapper {

    public CreateAdventureLorebookEntry toCommand(CreateLorebookEntryRequest request,
            String adventureId, String requesterDiscordId) {

        return CreateAdventureLorebookEntry.builder()
                .name(request.getName())
                .description(request.getDescription())
                .playerDiscordId(request.getPlayerDiscordId())
                .regex(request.getRegex())
                .adventureId(adventureId)
                .requesterDiscordId(requesterDiscordId)
                .build();
    }

    public UpdateAdventureLorebookEntry toCommand(UpdateLorebookEntryRequest request, String entryId,
            String adventureId, String requesterDiscordId) {

        return UpdateAdventureLorebookEntry.builder()
                .id(entryId)
                .name(request.getName())
                .description(request.getDescription())
                .playerDiscordId(request.getPlayerDiscordId())
                .regex(request.getRegex())
                .isPlayerCharacter(request.isPlayerCharacter())
                .requesterDiscordId(requesterDiscordId)
                .adventureId(adventureId)
                .build();
    }

    public DeleteAdventureLorebookEntry toCommand(String entryId, String adventureId, String requesterId) {

        return DeleteAdventureLorebookEntry.builder()
                .lorebookEntryId(entryId)
                .adventureId(adventureId)
                .requesterDiscordId(requesterId)
                .build();
    }
}
