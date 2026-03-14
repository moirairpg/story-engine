package me.moirai.storyengine.core.domain.world;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;

import io.micrometer.common.util.StringUtils;
import me.moirai.storyengine.common.annotation.DomainService;
import me.moirai.storyengine.common.domain.Permissions;
import me.moirai.storyengine.common.domain.Visibility;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.exception.ModerationException;
import me.moirai.storyengine.core.port.inbound.world.CreateWorld;
import me.moirai.storyengine.core.port.inbound.world.CreateWorldLorebookEntry;
import me.moirai.storyengine.core.port.inbound.world.DeleteWorldLorebookEntry;
import me.moirai.storyengine.core.port.inbound.world.GetWorldLorebookEntryById;
import me.moirai.storyengine.core.port.inbound.world.UpdateWorldLorebookEntry;
import me.moirai.storyengine.core.domain.adventure.Moderation;
import me.moirai.storyengine.core.port.TextModerationPort;
import reactor.core.publisher.Mono;

@DomainService
public class WorldServiceImpl implements WorldService {

    private static final String WORLD_FLAGGED_BY_MODERATION = "Persona flagged by moderation";
    private static final String WORLD_TO_BE_UPDATED_WAS_NOT_FOUND = "World to be updated was not found";
    private static final String WORLD_TO_BE_VIEWED_WAS_NOT_FOUND = "World to be viewed was not found";
    private static final String USER_DOES_NOT_HAVE_PERMISSION_TO_MODIFY_THIS_WORLD = "User does not have permission to modify this world";
    private static final String USER_DOES_NOT_HAVE_PERMISSION_TO_VIEW_THIS_WORLD = "User does not have permission to view this world";
    private static final String LOREBOOK_ENTRY_TO_BE_UPDATED_WAS_NOT_FOUND = "Lorebook entry to be updated was not found";
    private static final String LOREBOOK_ENTRY_TO_BE_DELETED_WAS_NOT_FOUND = "Lorebook entry to be deleted was not found";
    private static final String LOREBOOK_ENTRY_TO_BE_VIEWED_NOT_FOUND = "Lorebook entry to be viewed was not found";

    private final TextModerationPort moderationPort;
    private final WorldLorebookEntryRepository lorebookEntryRepository;
    private final WorldRepository repository;

    public WorldServiceImpl(TextModerationPort moderationPort,
            WorldLorebookEntryRepository lorebookEntryRepository,
            WorldRepository repository) {

        this.moderationPort = moderationPort;
        this.lorebookEntryRepository = lorebookEntryRepository;
        this.repository = repository;
    }

    @Override
    public Mono<World> createFrom(CreateWorld command) {

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
                });
    }

    @Override
    public WorldLorebookEntry findLorebookEntryById(GetWorldLorebookEntryById query) {

        World world = repository.findById(query.getWorldId())
                .orElseThrow(() -> new AssetNotFoundException(WORLD_TO_BE_VIEWED_WAS_NOT_FOUND));

        if (!world.canUserRead(query.getRequesterDiscordId())) {
            throw new AssetAccessDeniedException(USER_DOES_NOT_HAVE_PERMISSION_TO_VIEW_THIS_WORLD);
        }

        return lorebookEntryRepository.findById(query.getEntryId())
                .orElseThrow(() -> new AssetNotFoundException(LOREBOOK_ENTRY_TO_BE_VIEWED_NOT_FOUND));
    }

    @Override
    public WorldLorebookEntry createLorebookEntry(CreateWorldLorebookEntry command) {

        World world = repository.findById(command.getWorldId())
                .orElseThrow(() -> new AssetNotFoundException(WORLD_TO_BE_UPDATED_WAS_NOT_FOUND));

        if (!world.canUserWrite(command.getRequesterDiscordId())) {
            throw new AssetAccessDeniedException(USER_DOES_NOT_HAVE_PERMISSION_TO_MODIFY_THIS_WORLD);
        }

        WorldLorebookEntry lorebookEntry = WorldLorebookEntry.builder()
                .name(command.getName())
                .regex(command.getRegex())
                .description(command.getDescription())
                .worldId(world.getId())
                .creatorId(command.getRequesterDiscordId())
                .build();

        return lorebookEntryRepository.save(lorebookEntry);
    }

    @Override
    public WorldLorebookEntry updateLorebookEntry(UpdateWorldLorebookEntry command) {

        World world = repository.findById(command.getWorldId())
                .orElseThrow(() -> new AssetNotFoundException(WORLD_TO_BE_UPDATED_WAS_NOT_FOUND));

        if (!world.canUserWrite(command.getRequesterDiscordId())) {
            throw new AssetAccessDeniedException(USER_DOES_NOT_HAVE_PERMISSION_TO_MODIFY_THIS_WORLD);
        }

        WorldLorebookEntry lorebookEntry = lorebookEntryRepository.findById(command.getId())
                .orElseThrow(() -> new AssetNotFoundException(LOREBOOK_ENTRY_TO_BE_UPDATED_WAS_NOT_FOUND));

        if (StringUtils.isNotBlank(command.getName())) {
            lorebookEntry.updateName(command.getName());
        }

        if (StringUtils.isNotBlank(command.getRegex())) {
            lorebookEntry.updateRegex(command.getRegex());
        }

        if (StringUtils.isNotBlank(command.getDescription())) {
            lorebookEntry.updateDescription(command.getDescription());
        }

        return lorebookEntryRepository.save(lorebookEntry);
    }

    @Override
    public void deleteLorebookEntry(DeleteWorldLorebookEntry command) {

        World world = repository.findById(command.getWorldId())
                .orElseThrow(() -> new AssetNotFoundException(WORLD_TO_BE_UPDATED_WAS_NOT_FOUND));

        if (!world.canUserWrite(command.getRequesterDiscordId())) {
            throw new AssetAccessDeniedException(USER_DOES_NOT_HAVE_PERMISSION_TO_MODIFY_THIS_WORLD);
        }

        lorebookEntryRepository.findById(command.getLorebookEntryId())
                .orElseThrow(() -> new AssetNotFoundException(LOREBOOK_ENTRY_TO_BE_DELETED_WAS_NOT_FOUND));

        lorebookEntryRepository.deleteById(command.getLorebookEntryId());
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
