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
import me.moirai.storyengine.core.domain.world.WorldLorebookEntry;
import me.moirai.storyengine.core.port.inbound.world.CreateWorld;
import me.moirai.storyengine.core.port.inbound.world.CreateWorldResult;
import me.moirai.storyengine.core.port.outbound.generation.TextModerationPort;
import me.moirai.storyengine.core.port.outbound.world.WorldLorebookEntryRepository;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;
import reactor.core.publisher.Mono;

@UseCaseHandler
public class CreateWorldHandler extends AbstractUseCaseHandler<CreateWorld, Mono<CreateWorldResult>> {

    private static final String WORLD_FLAGGED_BY_MODERATION = "Persona flagged by moderation";

    private final TextModerationPort moderationPort;
    private final WorldLorebookEntryRepository lorebookEntryRepository;
    private final WorldRepository repository;

    public CreateWorldHandler(
            TextModerationPort moderationPort,
            WorldLorebookEntryRepository lorebookEntryRepository,
            WorldRepository repository) {

        this.moderationPort = moderationPort;
        this.lorebookEntryRepository = lorebookEntryRepository;
        this.repository = repository;
    }

    @Override
    public Mono<CreateWorldResult> execute(CreateWorld command) {

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

                    command.getLorebookEntries().stream()
                            .map(entry -> WorldLorebookEntry.builder()
                                    .name(entry.getName())
                                    .description(entry.getDescription())
                                    .regex(entry.getRegex())
                                    .worldId(world.getId())
                                    .build())
                            .forEach(lorebookEntryRepository::save);

                    return world;
                })
                .map(worldCreated -> CreateWorldResult.build(worldCreated.getId()));
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
