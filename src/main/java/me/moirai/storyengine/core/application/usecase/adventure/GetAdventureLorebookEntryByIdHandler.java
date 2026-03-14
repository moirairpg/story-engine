package me.moirai.storyengine.core.application.usecase.adventure;

import static org.apache.commons.lang3.StringUtils.isBlank;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureLorebookEntryById;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureLorebookEntryResult;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntry;
import me.moirai.storyengine.core.domain.adventure.AdventureService;

@UseCaseHandler
public class GetAdventureLorebookEntryByIdHandler extends AbstractUseCaseHandler<GetAdventureLorebookEntryById, GetAdventureLorebookEntryResult> {

    private final AdventureService domainService;

    public GetAdventureLorebookEntryByIdHandler(AdventureService domainService) {
        this.domainService = domainService;
    }

    @Override
    public void validate(GetAdventureLorebookEntryById query) {

        if (isBlank(query.getEntryId())) {
            throw new IllegalArgumentException("Lorebook entry ID cannot be null");
        }

        if (isBlank(query.getAdventureId())) {
            throw new IllegalArgumentException("Adventure ID cannot be null");
        }
    }

    @Override
    public GetAdventureLorebookEntryResult execute(GetAdventureLorebookEntryById query) {

        AdventureLorebookEntry entry = domainService.findLorebookEntryById(query);

        return mapResult(entry);
    }

    private GetAdventureLorebookEntryResult mapResult(AdventureLorebookEntry entry) {

        return GetAdventureLorebookEntryResult.builder()
                .id(entry.getId())
                .name(entry.getName())
                .regex(entry.getRegex())
                .description(entry.getDescription())
                .playerId(entry.getPlayerId())
                .isPlayerCharacter(entry.isPlayerCharacter())
                .creationDate(entry.getCreationDate())
                .lastUpdateDate(entry.getLastUpdateDate())
                .build();
    }
}
