package me.moirai.storyengine.core.application.command.adventure;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.port.inbound.adventure.DeleteAdventure;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;

@CommandHandler
public class DeleteAdventureHandler extends AbstractCommandHandler<DeleteAdventure, Void> {

    private static final String USER_NO_PERMISSION = "User does not have permission to delete adventure";
    private static final String ADVENTURE_NOT_FOUND = "Adventure to be deleted was not found";
    private static final String ID_CANNOT_BE_NULL_OR_EMPTY = "Adventure ID cannot be null or empty";

    private final AdventureRepository repository;

    public DeleteAdventureHandler(AdventureRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(DeleteAdventure command) {

        if (command.adventureId() == null) {
            throw new IllegalArgumentException(ID_CANNOT_BE_NULL_OR_EMPTY);
        }
    }

    @Override
    public Void execute(DeleteAdventure command) {

        var adventure = repository.findByPublicId(command.adventureId())
                .orElseThrow(() -> new AssetNotFoundException(ADVENTURE_NOT_FOUND));

        // TODO move to authorizer
        if (!adventure.canUserWrite(command.requesterId())) {
            throw new AssetAccessDeniedException(USER_NO_PERMISSION);
        }

        repository.deleteByPublicId(command.adventureId());

        return null;
    }
}
