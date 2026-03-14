package me.moirai.storyengine.infrastructure.inbound.rest.mapper;

import org.springframework.stereotype.Component;

import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventureLorebookEntry;
import me.moirai.storyengine.core.port.inbound.adventure.DeleteAdventureLorebookEntry;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureLorebookEntry;
import me.moirai.storyengine.infrastructure.inbound.rest.request.CreateLorebookEntryRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateLorebookEntryRequest;

@Component
public class AdventureLorebookEntryRequestMapper {

    public CreateAdventureLorebookEntry toCommand(CreateLorebookEntryRequest request,
            String adventureId, String requesterId) {

        return CreateAdventureLorebookEntry.builder()
                .name(request.getName())
                .description(request.getDescription())
                .playerId(request.getPlayerId())
                .regex(request.getRegex())
                .adventureId(adventureId)
                .requesterId(requesterId)
                .build();
    }

    public UpdateAdventureLorebookEntry toCommand(UpdateLorebookEntryRequest request, String entryId,
            String adventureId, String requesterId) {

        return UpdateAdventureLorebookEntry.builder()
                .id(entryId)
                .name(request.getName())
                .description(request.getDescription())
                .playerId(request.getPlayerId())
                .regex(request.getRegex())
                .isPlayerCharacter(request.isPlayerCharacter())
                .requesterId(requesterId)
                .adventureId(adventureId)
                .build();
    }

    public DeleteAdventureLorebookEntry toCommand(String entryId, String adventureId, String requesterId) {

        return DeleteAdventureLorebookEntry.builder()
                .lorebookEntryId(entryId)
                .adventureId(adventureId)
                .requesterId(requesterId)
                .build();
    }
}
