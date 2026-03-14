package me.moirai.storyengine.core.application.usecase.adventure;

import static org.apache.commons.lang3.StringUtils.isBlank;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntry;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureLorebookEntryById;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureLorebookEntryResult;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureLorebookEntryRepository;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;

@UseCaseHandler
public class GetAdventureLorebookEntryByIdHandler extends AbstractUseCaseHandler<GetAdventureLorebookEntryById, GetAdventureLorebookEntryResult> {

    private static final String ADVENTURE_TO_BE_VIEWED_WAS_NOT_FOUND = "Adventure to be viewed was not found";
    private static final String USER_DOES_NOT_HAVE_PERMISSION_TO_VIEW_THIS_ADVENTURE = "User does not have permission to view this adventure";
    private static final String LOREBOOK_ENTRY_TO_BE_VIEWED_NOT_FOUND = "Lorebook entry to be viewed was not found";

    private final AdventureLorebookEntryRepository lorebookEntryRepository;
    private final AdventureRepository repository;

    public GetAdventureLorebookEntryByIdHandler(
            AdventureLorebookEntryRepository lorebookEntryRepository,
            AdventureRepository repository) {

        this.lorebookEntryRepository = lorebookEntryRepository;
        this.repository = repository;
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

        Adventure adventure = repository.findById(query.getAdventureId())
                .orElseThrow(() -> new AssetNotFoundException(ADVENTURE_TO_BE_VIEWED_WAS_NOT_FOUND));

        if (!adventure.canUserRead(query.getRequesterDiscordId())) {
            throw new AssetAccessDeniedException(USER_DOES_NOT_HAVE_PERMISSION_TO_VIEW_THIS_ADVENTURE);
        }

        AdventureLorebookEntry entry = lorebookEntryRepository.findById(query.getEntryId())
                .orElseThrow(() -> new AssetNotFoundException(LOREBOOK_ENTRY_TO_BE_VIEWED_NOT_FOUND));

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
