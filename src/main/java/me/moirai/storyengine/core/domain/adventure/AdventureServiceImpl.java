package me.moirai.storyengine.core.domain.adventure;

import static io.micrometer.common.util.StringUtils.isBlank;
import static io.micrometer.common.util.StringUtils.isEmpty;
import static io.micrometer.common.util.StringUtils.isNotBlank;
import static java.util.Collections.emptyList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections4.MapUtils.isEmpty;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.moirai.storyengine.common.annotation.DomainService;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.exception.ModerationException;
import me.moirai.storyengine.core.application.usecase.adventure.request.CreateAdventureLorebookEntry;
import me.moirai.storyengine.core.application.usecase.adventure.request.DeleteAdventureLorebookEntry;
import me.moirai.storyengine.core.application.usecase.adventure.request.GetAdventureLorebookEntryById;
import me.moirai.storyengine.core.application.usecase.adventure.request.UpdateAdventureLorebookEntry;
import me.moirai.storyengine.core.port.TextModerationPort;
import reactor.core.publisher.Mono;

@DomainService
public class AdventureServiceImpl implements AdventureService {

    private static final String ADVENTURE_FLAGGED_BY_MODERATION = "Adventure flagged by moderation";
    private static final String ADVENTURE_TO_BE_UPDATED_WAS_NOT_FOUND = "Adventure to be updated was not found";
    private static final String ADVENTURE_TO_BE_VIEWED_WAS_NOT_FOUND = "Adventure to be viewed was not found";
    private static final String USER_DOES_NOT_HAVE_PERMISSION_TO_MODIFY_THIS_ADVENTURE = "User does not have permission to modify this adventure";
    private static final String USER_DOES_NOT_HAVE_PERMISSION_TO_VIEW_THIS_ADVENTURE = "User does not have permission to view this adventure";
    private static final String LOREBOOK_ENTRY_TO_BE_UPDATED_WAS_NOT_FOUND = "Lorebook entry to be updated was not found";
    private static final String LOREBOOK_ENTRY_TO_BE_DELETED_WAS_NOT_FOUND = "Lorebook entry to be deleted was not found";
    private static final String LOREBOOK_ENTRY_TO_BE_VIEWED_NOT_FOUND = "Lorebook entry to be viewed was not found";

    private final TextModerationPort moderationPort;
    private final AdventureLorebookEntryRepository lorebookEntryRepository;
    private final AdventureRepository repository;

    public AdventureServiceImpl(TextModerationPort moderationPort,
            AdventureLorebookEntryRepository lorebookEntryRepository,
            AdventureRepository repository) {

        this.moderationPort = moderationPort;
        this.lorebookEntryRepository = lorebookEntryRepository;
        this.repository = repository;
    }

    @Override
    public AdventureLorebookEntry findLorebookEntryByPlayerDiscordId(String playerId, String adventureId) {

        repository.findById(adventureId)
                .orElseThrow(() -> new AssetNotFoundException(ADVENTURE_TO_BE_VIEWED_WAS_NOT_FOUND));

        return lorebookEntryRepository.findByPlayerId(playerId, adventureId)
                .orElseThrow(() -> new AssetNotFoundException(LOREBOOK_ENTRY_TO_BE_VIEWED_NOT_FOUND));
    }

    @Override
    public List<AdventureLorebookEntry> findAllLorebookEntriesByRegex(String valueToMatch, String adventureId) {

        repository.findById(adventureId)
                .orElseThrow(() -> new AssetNotFoundException(ADVENTURE_TO_BE_VIEWED_WAS_NOT_FOUND));

        return lorebookEntryRepository.findAllByRegex(valueToMatch, adventureId);
    }

    @Override
    public AdventureLorebookEntry findLorebookEntryById(GetAdventureLorebookEntryById query) {

        Adventure adventure = repository.findById(query.getAdventureId())
                .orElseThrow(() -> new AssetNotFoundException(ADVENTURE_TO_BE_VIEWED_WAS_NOT_FOUND));

        if (!adventure.canUserRead(query.getRequesterDiscordId())) {
            throw new AssetAccessDeniedException(USER_DOES_NOT_HAVE_PERMISSION_TO_VIEW_THIS_ADVENTURE);
        }

        return lorebookEntryRepository.findById(query.getEntryId())
                .orElseThrow(() -> new AssetNotFoundException(LOREBOOK_ENTRY_TO_BE_VIEWED_NOT_FOUND));
    }

    @Override
    public Mono<AdventureLorebookEntry> createLorebookEntry(CreateAdventureLorebookEntry command) {

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
                });
    }

    @Override
    public Mono<AdventureLorebookEntry> updateLorebookEntry(UpdateAdventureLorebookEntry command) {

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
                });
    }

    @Override
    public void deleteLorebookEntry(DeleteAdventureLorebookEntry command) {

        Adventure adventure = repository.findById(command.getAdventureId())
                .orElseThrow(() -> new AssetNotFoundException(ADVENTURE_TO_BE_UPDATED_WAS_NOT_FOUND));

        if (!adventure.canUserWrite(command.getRequesterDiscordId())) {
            throw new AssetAccessDeniedException(USER_DOES_NOT_HAVE_PERMISSION_TO_MODIFY_THIS_ADVENTURE);
        }

        lorebookEntryRepository.findById(command.getLorebookEntryId())
                .orElseThrow(() -> new AssetNotFoundException(LOREBOOK_ENTRY_TO_BE_DELETED_WAS_NOT_FOUND));

        lorebookEntryRepository.deleteById(command.getLorebookEntryId());
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
