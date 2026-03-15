package me.moirai.storyengine.core.application.usecase.adventure;

import static org.apache.commons.lang3.StringUtils.isBlank;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntry;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureLorebookEntryDetails;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureLorebookEntryById;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;

@UseCaseHandler
public class GetAdventureLorebookEntryByIdHandler extends AbstractUseCaseHandler<GetAdventureLorebookEntryById, AdventureLorebookEntryDetails> {

    private static final String ADVENTURE_TO_BE_VIEWED_WAS_NOT_FOUND = "Adventure to be viewed was not found";
    private static final String USER_DOES_NOT_HAVE_PERMISSION_TO_VIEW_THIS_ADVENTURE = "User does not have permission to view this adventure";

    private final AdventureRepository repository;

    public GetAdventureLorebookEntryByIdHandler(AdventureRepository repository) {

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
    public AdventureLorebookEntryDetails execute(GetAdventureLorebookEntryById query) {

        Adventure adventure = repository.findByPublicId(query.getAdventureId())
                .orElseThrow(() -> new AssetNotFoundException(ADVENTURE_TO_BE_VIEWED_WAS_NOT_FOUND));

        if (!adventure.canUserRead(query.getRequesterDiscordId())) {
            throw new AssetAccessDeniedException(USER_DOES_NOT_HAVE_PERMISSION_TO_VIEW_THIS_ADVENTURE);
        }

        AdventureLorebookEntry entry = adventure.getLorebookEntryById(query.getEntryId());

        return mapResult(entry);
    }

    private AdventureLorebookEntryDetails mapResult(AdventureLorebookEntry entry) {

        return AdventureLorebookEntryDetails.builder()
                .id(entry.getPublicId())
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
