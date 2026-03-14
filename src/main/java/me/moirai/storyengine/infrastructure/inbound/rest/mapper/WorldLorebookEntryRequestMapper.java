package me.moirai.storyengine.infrastructure.inbound.rest.mapper;

import org.springframework.stereotype.Component;

import me.moirai.storyengine.core.application.usecase.world.request.CreateWorldLorebookEntry;
import me.moirai.storyengine.core.application.usecase.world.request.DeleteWorldLorebookEntry;
import me.moirai.storyengine.core.application.usecase.world.request.UpdateWorldLorebookEntry;
import me.moirai.storyengine.infrastructure.inbound.rest.request.CreateLorebookEntryRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateLorebookEntryRequest;

@Component
public class WorldLorebookEntryRequestMapper {

    public CreateWorldLorebookEntry toCommand(CreateLorebookEntryRequest request,
            String worldId, String requesterId) {

        return CreateWorldLorebookEntry.builder()
                .name(request.getName())
                .description(request.getDescription())
                .regex(request.getRegex())
                .worldId(worldId)
                .requesterId(requesterId)
                .build();
    }

    public UpdateWorldLorebookEntry toCommand(UpdateLorebookEntryRequest request, String entryId,
            String worldId, String requesterId) {

        return UpdateWorldLorebookEntry.builder()
                .id(entryId)
                .name(request.getName())
                .description(request.getDescription())
                .regex(request.getRegex())
                .requesterId(requesterId)
                .worldId(worldId)
                .build();
    }

    public DeleteWorldLorebookEntry toCommand(String entryId, String worldId, String requesterId) {

        return DeleteWorldLorebookEntry.builder()
                .lorebookEntryId(entryId)
                .worldId(worldId)
                .requesterId(requesterId)
                .build();
    }
}
