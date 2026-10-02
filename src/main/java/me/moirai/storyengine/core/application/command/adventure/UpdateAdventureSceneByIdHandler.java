package me.moirai.storyengine.core.application.command.adventure;

import me.moirai.storyengine.common.annotation.Authorize;
import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureSceneById;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;

@CommandHandler
@Authorize(operation = AuthorizationOperation.UPDATE_ADVENTURE, fields = "#request.adventureId")
public class UpdateAdventureSceneByIdHandler
        extends AbstractCommandHandler<UpdateAdventureSceneById, Void> {

    private static final String ADVENTURE_NOT_FOUND = "Adventure to be updated was not found";

    private AdventureRepository repository;

    public UpdateAdventureSceneByIdHandler(AdventureRepository repository) {
        this.repository = repository;
    }

    @Override
    public Void execute(UpdateAdventureSceneById useCase) {

        repository.findByPublicId(useCase.adventureId())
                .orElseThrow(() -> new NotFoundException(ADVENTURE_NOT_FOUND));

        repository.updateSceneByPublicId(useCase.scene(), useCase.adventureId());

        return null;
    }
}
