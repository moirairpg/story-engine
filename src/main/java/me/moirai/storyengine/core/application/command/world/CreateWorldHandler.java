package me.moirai.storyengine.core.application.command.world;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;

import io.micrometer.common.util.StringUtils;
import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.domain.Permissions;
import me.moirai.storyengine.common.enums.Moderation;
import me.moirai.storyengine.common.exception.ModerationException;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.port.inbound.world.CreateWorld;
import me.moirai.storyengine.core.port.inbound.world.WorldDetails;
import me.moirai.storyengine.core.port.outbound.generation.TextModerationPort;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;
import reactor.core.publisher.Mono;

@CommandHandler
public class CreateWorldHandler extends AbstractCommandHandler<CreateWorld, Mono<WorldDetails>> {

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

        return moderateContent(command.adventureStart())
                .flatMap(__ -> moderateContent(command.name()))
                .flatMap(__ -> moderateContent(command.description()))
                .map(__ -> {
                    var permissions = Permissions.builder()
                            .ownerId(command.requesterId())
                            .usersAllowedToRead(command.usersAllowedToRead())
                            .usersAllowedToWrite(command.usersAllowedToWrite())
                            .build();

                    var world = repository.save(World.builder()
                            .name(command.name())
                            .description(command.description())
                            .adventureStart(command.adventureStart())
                            .visibility(command.visibility())
                            .permissions(permissions)
                            .creatorId(command.requesterId())
                            .build());

                    command.lorebookEntries().forEach(entry -> world.addLorebookEntry(
                            entry.name(),
                            entry.regex(),
                            entry.description()));

                    repository.save(world);

                    return world;
                })
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
