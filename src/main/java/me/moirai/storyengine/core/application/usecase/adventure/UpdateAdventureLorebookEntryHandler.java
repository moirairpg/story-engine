package me.moirai.storyengine.core.application.usecase.adventure;

import static io.micrometer.common.util.StringUtils.isBlank;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureLorebookEntry;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureLorebookEntryResult;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntry;
import me.moirai.storyengine.core.domain.adventure.AdventureService;
import reactor.core.publisher.Mono;

@UseCaseHandler
public class UpdateAdventureLorebookEntryHandler
        extends AbstractUseCaseHandler<UpdateAdventureLorebookEntry, Mono<UpdateAdventureLorebookEntryResult>> {

    private final AdventureService service;

    public UpdateAdventureLorebookEntryHandler(AdventureService service) {
        this.service = service;
    }

    @Override
    public void validate(UpdateAdventureLorebookEntry command) {

        if (isBlank(command.getId())) {

            throw new IllegalArgumentException("Lorebook Entry ID cannot be null");
        }

        if (isBlank(command.getAdventureId())) {

            throw new IllegalArgumentException("Adventure ID cannot be null");
        }

        if (isBlank(command.getName())) {

            throw new IllegalArgumentException("Adventure name cannot be null");
        }

        if (isBlank(command.getDescription())) {

            throw new IllegalArgumentException("Adventure description cannot be null");
        }
    }

    @Override
    public Mono<UpdateAdventureLorebookEntryResult> execute(UpdateAdventureLorebookEntry command) {

        return service.updateLorebookEntry(command)
                .map(this::toResult);
    }

    private UpdateAdventureLorebookEntryResult toResult(AdventureLorebookEntry savedEntry) {

        return UpdateAdventureLorebookEntryResult.build(savedEntry.getLastUpdateDate());
    }
}
