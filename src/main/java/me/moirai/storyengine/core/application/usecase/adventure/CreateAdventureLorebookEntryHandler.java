package me.moirai.storyengine.core.application.usecase.adventure;

import static io.micrometer.common.util.StringUtils.isBlank;
import static io.micrometer.common.util.StringUtils.isEmpty;
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
import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventureLorebookEntry;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureLorebookEntryRepository;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.generation.TextModerationPort;
import reactor.core.publisher.Mono;

@UseCaseHandler
public class CreateAdventureLorebookEntryHandler
        extends AbstractUseCaseHandler<CreateAdventureLorebookEntry, Mono<AdventureLorebookEntryDetails>> {

    private static final String ADVENTURE_FLAGGED_BY_MODERATION = "Adventure flagged by moderation";
    private static final String ADVENTURE_TO_BE_UPDATED_WAS_NOT_FOUND = "Adventure to be updated was not found";
    private static final String USER_DOES_NOT_HAVE_PERMISSION_TO_MODIFY_THIS_ADVENTURE = "User does not have permission to modify this adventure";

    private final TextModerationPort moderationPort;
    private final AdventureLorebookEntryRepository lorebookEntryRepository;
    private final AdventureRepository repository;

    public CreateAdventureLorebookEntryHandler(
            TextModerationPort moderationPort,
            AdventureLorebookEntryRepository lorebookEntryRepository,
            AdventureRepository repository) {

        this.moderationPort = moderationPort;
        this.lorebookEntryRepository = lorebookEntryRepository;
        this.repository = repository;
    }

    @Override
    public void validate(CreateAdventureLorebookEntry command) {

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
    public Mono<AdventureLorebookEntryDetails> execute(CreateAdventureLorebookEntry command) {

        Adventure adventure = repository.findById(command.getAdventureId())
                .orElseThrow(() -> new AssetNotFoundException(ADVENTURE_TO_BE_UPDATED_WAS_NOT_FOUND));

        if (!adventure.canUserWrite(command.getRequesterDiscordId())) {
            throw new AssetAccessDeniedException(USER_DOES_NOT_HAVE_PERMISSION_TO_MODIFY_THIS_ADVENTURE);
        }

        return moderateContent(command.getName(), adventure.getModeration())
                .flatMap(__ -> moderateContent(command.getDescription(), adventure.getModeration()))
                .map(__ -> {
                    AdventureLorebookEntry lorebookEntry = AdventureLorebookEntry.builder()
                            .name(command.getName())
                            .regex(command.getRegex())
                            .description(command.getDescription())
                            .playerId(command.getPlayerId())
                            .isPlayerCharacter(isEmpty(command.getPlayerId()))
                            .adventureId(adventure.getId())
                            .creatorId(command.getRequesterDiscordId())
                            .build();

                    return lorebookEntryRepository.save(lorebookEntry);
                })
                .map(this::mapResult);
    }

    private AdventureLorebookEntryDetails mapResult(AdventureLorebookEntry entry) {

        return AdventureLorebookEntryDetails.builder()
                .id(entry.getId())
                .name(entry.getName())
                .regex(entry.getRegex())
                .description(entry.getDescription())
                .playerId(entry.getPlayerId())
                .isPlayerCharacter(entry.isPlayerCharacter())
                .creationDate(entry.getCreationDate())
                .lastUpdateDate(entry.getLastUpdateDate())
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
