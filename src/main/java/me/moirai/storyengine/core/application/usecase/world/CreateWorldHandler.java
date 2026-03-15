package me.moirai.storyengine.core.application.usecase.world;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;

import io.micrometer.common.util.StringUtils;
import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.domain.Permissions;
import me.moirai.storyengine.common.domain.Visibility;
import me.moirai.storyengine.common.exception.ModerationException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.domain.adventure.Moderation;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.port.inbound.world.CreateWorld;
import me.moirai.storyengine.core.port.inbound.world.WorldDetails;
import me.moirai.storyengine.core.port.outbound.generation.TextModerationPort;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;
import reactor.core.publisher.Mono;

@UseCaseHandler
public class CreateWorldHandler extends AbstractUseCaseHandler<CreateWorld, Mono<WorldDetails>> {

    private static final String WORLD_FLAGGED_BY_MODERATION = "Persona flagged by moderation";

    private final TextModerationPort moderationPort;
    private final WorldRepository repository;

    public CreateWorldHandler(
            TextModerationPort moderationPort,
            WorldRepository repository) {

        this.moderationPort = moderationPort;
        this.repository = repository;
    }

    @Override
    public Mono<WorldDetails> execute(CreateWorld command) {

        return moderateContent(command.getAdventureStart())
                .flatMap(__ -> moderateContent(command.getName()))
                .flatMap(__ -> moderateContent(command.getDescription()))
                .map(__ -> {
                    Permissions permissions = Permissions.builder()
                            .ownerId(command.getRequesterDiscordId())
                            .usersAllowedToRead(command.getUsersAllowedToRead())
                            .usersAllowedToWrite(command.getUsersAllowedToWrite())
                            .build();

                    World world = repository.save(World.builder()
                            .name(command.getName())
                            .description(command.getDescription())
                            .adventureStart(command.getAdventureStart())
                            .visibility(Visibility.fromString(command.getVisibility()))
                            .permissions(permissions)
                            .creatorId(command.getRequesterDiscordId())
                            .build());

                    command.getLorebookEntries().forEach(entry -> world.addLorebookEntry(
                            entry.getName(),
                            entry.getRegex(),
                            entry.getDescription()));

                    repository.save(world);

                    return world;
                })
                .map(this::mapResult);
    }

    private WorldDetails mapResult(World world) {

        return WorldDetails.builder()
                .id(world.getId())
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

    private Mono<List<String>> moderateContent(String personality) {

        if (StringUtils.isBlank(personality)) {
            return Mono.just(Collections.emptyList());
        }

        return getTopicsFlaggedByModeration(personality)
                .map(flaggedTopics -> {
                    if (CollectionUtils.isNotEmpty(flaggedTopics)) {
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
