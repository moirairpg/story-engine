package me.moirai.storyengine.core.application.usecase.adventure;

import static org.apache.commons.lang3.StringUtils.isBlank;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.port.inbound.adventure.DeleteAdventureLorebookEntry;
import me.moirai.storyengine.core.domain.adventure.AdventureService;

@UseCaseHandler
public class DeleteAdventureLorebookEntryHandler extends AbstractUseCaseHandler<DeleteAdventureLorebookEntry, Void> {

    private static final String ENTRY_ID_CANNOT_BE_NULL_OR_EMPTY = "Lorebook entry ID cannot be null or empty";
    private static final String ADVENTURE_ID_CANNOT_BE_NULL_OR_EMPTY = "Adventure ID cannot be null or empty";

    private final AdventureService domainService;

    public DeleteAdventureLorebookEntryHandler(AdventureService domainService) {
        this.domainService = domainService;
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

        domainService.deleteLorebookEntry(command);

        return null;
    }
}
