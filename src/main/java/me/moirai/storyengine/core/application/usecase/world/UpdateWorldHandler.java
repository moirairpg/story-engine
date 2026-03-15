package me.moirai.storyengine.core.application.usecase.world;

import static me.moirai.storyengine.common.domain.Visibility.PRIVATE;
import static me.moirai.storyengine.common.domain.Visibility.PUBLIC;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.exception.ModerationException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.port.inbound.world.UpdateWorld;
import me.moirai.storyengine.core.port.inbound.world.WorldDetails;
import me.moirai.storyengine.core.port.outbound.generation.TextModerationPort;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;
import me.moirai.storyengine.core.domain.adventure.Moderation;
import me.moirai.storyengine.core.domain.world.World;
import reactor.core.publisher.Mono;

@UseCaseHandler
public class UpdateWorldHandler extends AbstractUseCaseHandler<UpdateWorld, Mono<WorldDetails>> {

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

        if (isBlank(command.getId())) {
            throw new IllegalArgumentException(ID_CANNOT_BE_NULL_OR_EMPTY);
        }
    }

    @Override
    public Mono<WorldDetails> execute(UpdateWorld command) {

        return moderateContent(command.getAdventureStart())
                .flatMap(__ -> moderateContent(command.getName()))
                .map(__ -> updateWorld(command))
                .map(this::mapResult);
    }

    private WorldDetails mapResult(World world) {

        return WorldDetails.builder()
                .id(world.getPublicId())
                .name(world.getName())
                .description(world.getDescription())
                .adventureStart(world.getAdventureStart())
                .visibility(world.getVisibility().name())
                .ownerId(world.getOwnerId())
                .usersAllowedToRead(world.getUsersAllowedToRead())
                .usersAllowedToWrite(world.getUsersAllowedToWrite())
                .creationDate(world.getCreationDate())
                .lastUpdateDate(world.getLastUpdateDate())
                .build();
    }

    public World updateWorld(UpdateWorld command) {

        World world = repository.findByPublicId(command.getId())
                .orElseThrow(() -> new AssetNotFoundException(WORLD_NOT_FOUND));

        if (!world.canUserWrite(command.getRequesterDiscordId())) {
            throw new AssetAccessDeniedException(USER_NO_PERMISSION_IN_PERSONA);
        }

        if (isNotBlank(command.getName())) {
            world.updateName(command.getName());
        }

        if (isNotBlank(command.getDescription())) {
            world.updateDescription(command.getDescription());
        }

        if (isNotBlank(command.getAdventureStart())) {
            world.updateAdventureStart(command.getAdventureStart());
        }

        if (isNotBlank(command.getVisibility())) {
            if (command.getVisibility().equalsIgnoreCase(PUBLIC.name())) {
                world.makePublic();
            } else if (command.getVisibility().equalsIgnoreCase(PRIVATE.name())) {
                world.makePrivate();
            }
        }

        emptyIfNull(command.getUsersAllowedToReadToAdd())
                .forEach(world::addReaderUser);

        emptyIfNull(command.getUsersAllowedToWriteToAdd())
                .forEach(world::addWriterUser);

        emptyIfNull(command.getUsersAllowedToReadToRemove())
                .forEach(world::removeReaderUser);

        emptyIfNull(command.getUsersAllowedToWriteToRemove())
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
