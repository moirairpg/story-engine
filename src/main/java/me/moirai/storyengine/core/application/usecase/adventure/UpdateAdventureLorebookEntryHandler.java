package me.moirai.storyengine.core.application.usecase.adventure;

import static io.micrometer.common.util.StringUtils.isBlank;
import static io.micrometer.common.util.StringUtils.isNotBlank;
import static java.util.Collections.emptyList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections4.MapUtils.isEmpty;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.exception.ModerationException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntry;
import me.moirai.storyengine.core.domain.adventure.Moderation;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureLorebookEntryDetails;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureLorebookEntry;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureLorebookEntryRepository;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.generation.TextModerationPort;
import reactor.core.publisher.Mono;

@UseCaseHandler
public class UpdateAdventureLorebookEntryHandler
        extends AbstractUseCaseHandler<UpdateAdventureLorebookEntry, Mono<AdventureLorebookEntryDetails>> {

    private static final String ADVENTURE_FLAGGED_BY_MODERATION = "Adventure flagged by moderation";
    private static final String ADVENTURE_TO_BE_UPDATED_WAS_NOT_FOUND = "Adventure to be updated was not found";
    private static final String USER_DOES_NOT_HAVE_PERMISSION_TO_MODIFY_THIS_ADVENTURE = "User does not have permission to modify this adventure";
    private static final String LOREBOOK_ENTRY_TO_BE_UPDATED_WAS_NOT_FOUND = "Lorebook entry to be updated was not found";

    private final TextModerationPort moderationPort;
    private final AdventureLorebookEntryRepository lorebookEntryRepository;
    private final AdventureRepository repository;

    public UpdateAdventureLorebookEntryHandler(
            TextModerationPort moderationPort,
            AdventureLorebookEntryRepository lorebookEntryRepository,
            AdventureRepository repository) {

        this.moderationPort = moderationPort;
        this.lorebookEntryRepository = lorebookEntryRepository;
        this.repository = repository;
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
    public Mono<AdventureLorebookEntryDetails> execute(UpdateAdventureLorebookEntry command) {

        Adventure adventure = repository.findById(command.getAdventureId())
                .orElseThrow(() -> new AssetNotFoundException(ADVENTURE_TO_BE_UPDATED_WAS_NOT_FOUND));

        if (!adventure.canUserWrite(command.getRequesterDiscordId())) {
            throw new AssetAccessDeniedException(USER_DOES_NOT_HAVE_PERMISSION_TO_MODIFY_THIS_ADVENTURE);
        }

        return moderateContent(command.getName(), adventure.getModeration())
                .flatMap(__ -> moderateContent(command.getDescription(), adventure.getModeration()))
                .map(__ -> {
                    AdventureLorebookEntry lorebookEntry = lorebookEntryRepository.findById(command.getId())
                            .orElseThrow(() -> new AssetNotFoundException(LOREBOOK_ENTRY_TO_BE_UPDATED_WAS_NOT_FOUND));

                    if (isNotBlank(command.getName())) {
                        lorebookEntry.updateName(command.getName());
                    }

                    if (isNotBlank(command.getRegex())) {
                        lorebookEntry.updateRegex(command.getRegex());
                    }

                    if (isNotBlank(command.getDescription())) {
                        lorebookEntry.updateDescription(command.getDescription());
                    }

                    if (command.isPlayerCharacter()) {
                        lorebookEntry.assignPlayer(command.getPlayerId());
                    } else {
                        lorebookEntry.unassignPlayer();
                    }

                    return lorebookEntryRepository.save(lorebookEntry);
                })
                .map(this::toResult);
    }

    private AdventureLorebookEntryDetails toResult(AdventureLorebookEntry savedEntry) {

        return AdventureLorebookEntryDetails.builder()
                .id(savedEntry.getId())
                .name(savedEntry.getName())
                .regex(savedEntry.getRegex())
                .description(savedEntry.getDescription())
                .playerId(savedEntry.getPlayerId())
                .isPlayerCharacter(savedEntry.isPlayerCharacter())
                .creationDate(savedEntry.getCreationDate())
                .lastUpdateDate(savedEntry.getLastUpdateDate())
                .build();
    }

    private Mono<List<String>> moderateContent(String content, Moderation moderation) {

        if (isBlank(content)) {
            return Mono.just(emptyList());
        }

        return getTopicsFlaggedByModeration(content, moderation)
                .map(flaggedTopics -> {
                    if (isNotEmpty(flaggedTopics)) {
                        throw new ModerationException(ADVENTURE_FLAGGED_BY_MODERATION, flaggedTopics);
                    }

                    return flaggedTopics;
                });
    }

    private Mono<List<String>> getTopicsFlaggedByModeration(String input, Moderation moderation) {

        return moderationPort.moderate(input)
                .map(result -> {
                    if (moderation.isAbsolute()) {
                        if (result.isContentFlagged()) {
                            return result.getFlaggedTopics();
                        }

                        return emptyList();
                    }

                    return result.getModerationScores()
                            .entrySet()
                            .stream()
                            .filter(entry -> isTopicFlagged(entry, moderation))
                            .map(Map.Entry::getKey)
                            .toList();
                });
    }

    private boolean isTopicFlagged(Entry<String, Double> entry, Moderation moderation) {

        if (isEmpty(moderation.getThresholds())) {
            return false;
        }

        return entry.getValue() > moderation.getThresholds().get(entry.getKey());
    }
}
