package me.moirai.storyengine.core.application.usecase.adventure;

import static org.apache.commons.lang3.StringUtils.isBlank;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.port.inbound.adventure.DeleteAdventureLorebookEntry;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;

@UseCaseHandler
public class DeleteAdventureLorebookEntryHandler extends AbstractUseCaseHandler<DeleteAdventureLorebookEntry, Void> {

    private static final String ENTRY_ID_CANNOT_BE_NULL_OR_EMPTY = "Lorebook entry ID cannot be null or empty";
    private static final String ADVENTURE_ID_CANNOT_BE_NULL_OR_EMPTY = "Adventure ID cannot be null or empty";
    private static final String ADVENTURE_TO_BE_UPDATED_WAS_NOT_FOUND = "Adventure to be updated was not found";
    private static final String USER_DOES_NOT_HAVE_PERMISSION_TO_MODIFY_THIS_ADVENTURE = "User does not have permission to modify this adventure";

    private final AdventureRepository repository;

    public DeleteAdventureLorebookEntryHandler(AdventureRepository repository) {

        this.repository = repository;
    }

    @Override
    public void validate(DeleteAdventureLorebookEntry command) {

        if (isBlank(command.getLorebookEntryId())) {
            throw new IllegalArgumentException(ENTRY_ID_CANNOT_BE_NULL_OR_EMPTY);
        }

        if (isBlank(command.getAdventureId())) {
            throw new IllegalArgumentException(ADVENTURE_ID_CANNOT_BE_NULL_OR_EMPTY);
        }
    }

    @Override
    public Void execute(DeleteAdventureLorebookEntry command) {

        Adventure adventure = repository.findById(command.getAdventureId())
                .orElseThrow(() -> new AssetNotFoundException(ADVENTURE_TO_BE_UPDATED_WAS_NOT_FOUND));

        if (!adventure.canUserWrite(command.getRequesterDiscordId())) {
            throw new AssetAccessDeniedException(USER_DOES_NOT_HAVE_PERMISSION_TO_MODIFY_THIS_ADVENTURE);
        }

        adventure.removeLorebookEntry(command.getLorebookEntryId());
        repository.save(adventure);

        return null;
    }
}
