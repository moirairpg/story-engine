package me.moirai.storyengine.core.application.usecase.world;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.enums.Moderation;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.exception.ModerationException;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.port.inbound.world.UpdateWorld;
import me.moirai.storyengine.core.port.inbound.world.WorldDetails;
import me.moirai.storyengine.core.port.outbound.generation.TextModerationPort;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;
import reactor.core.publisher.Mono;

@CommandHandler
public class UpdateWorldHandler extends AbstractCommandHandler<UpdateWorld, Mono<WorldDetails>> {

    private static final String WORLD_FLAGGED_BY_MODERATION = "World flagged by moderation";
    private static final String ID_CANNOT_BE_NULL_OR_EMPTY = "World ID cannot be null or empty";
    private static final String WORLD_NOT_FOUND = "World to be updated was not found";
    private static final String USER_NO_PERMISSION_IN_PERSONA = "User does not have permission to modify the persona";

    private final WorldRepository repository;
    private final TextModerationPort moderationPort;

    public UpdateWorldHandler(WorldRepository repository,
            TextModerationPort moderationPort) {

        this.repository = repository;
        this.moderationPort = moderationPort;
    }

    @Override
    public void validate(UpdateWorld command) {

        if (command.worldId() == null) {
            throw new IllegalArgumentException(ID_CANNOT_BE_NULL_OR_EMPTY);
        }
    }

    @Override
    public Mono<WorldDetails> execute(UpdateWorld command) {

        return moderateContent(command.adventureStart())
                .flatMap(__ -> moderateContent(command.name()))
                .map(__ -> updateWorld(command))
                .map(this::mapResult);
    }

    private WorldDetails mapResult(World world) {

        return new WorldDetails(
                world.getPublicId(),
                world.getName(),
                world.getDescription(),
                world.getAdventureStart(),
                world.getVisibility().name(),
                world.getOwnerId(),
                world.getUsersAllowedToRead(),
                world.getUsersAllowedToWrite(),
                world.getCreationDate(),
                world.getLastUpdateDate());
    }

    public World updateWorld(UpdateWorld command) {

        World world = repository.findByPublicId(command.worldId())
                .orElseThrow(() -> new AssetNotFoundException(WORLD_NOT_FOUND));

        // TODO externalize to authorizer
        if (!world.canUserWrite(command.requesterId())) {
            throw new AssetAccessDeniedException(USER_NO_PERMISSION_IN_PERSONA);
        }

        world.updateName(command.name());
        world.updateDescription(command.description());
        world.updateAdventureStart(command.adventureStart());

        if (command.visibility() != null) {
            switch (command.visibility()) {
                case PUBLIC -> world.makePublic();
                case PRIVATE -> world.makePrivate();
                default -> world.makePrivate();
            }
        }

        emptyIfNull(command.usersAllowedToReadToAdd())
                .forEach(world::addReaderUser);

        emptyIfNull(command.usersAllowedToWriteToAdd())
                .forEach(world::addWriterUser);

        emptyIfNull(command.usersAllowedToReadToRemove())
                .forEach(world::removeReaderUser);

        emptyIfNull(command.usersAllowedToWriteToRemove())
                .forEach(world::removeWriterUser);

        return repository.save(world);
    }

    private Mono<List<String>> moderateContent(String content) {

        if (isBlank(content)) {
            return Mono.just(Collections.emptyList());
        }

        return getTopicsFlaggedByModeration(content)
                .map(flaggedTopics -> {
                    if (isNotEmpty(flaggedTopics)) {
                        throw new ModerationException(WORLD_FLAGGED_BY_MODERATION, flaggedTopics);
                    }

                    return flaggedTopics;
                });
    }

    private Mono<List<String>> getTopicsFlaggedByModeration(String input) {

        return moderationPort.moderate(input)
                .map(result -> result.getModerationScores()
                        .entrySet()
                        .stream()
                        .filter(this::isTopicFlagged)
                        .map(Map.Entry::getKey)
                        .toList());
    }

    private boolean isTopicFlagged(Entry<String, Double> entry) {
        return entry.getValue() > Moderation.PERMISSIVE.getThresholds().get(entry.getKey());
    }
}
