package me.moirai.storyengine.core.application.usecase.adventure;

import static me.moirai.storyengine.common.domain.Visibility.PRIVATE;
import static me.moirai.storyengine.common.domain.Visibility.PUBLIC;
import static me.moirai.storyengine.core.domain.adventure.ArtificialIntelligenceModel.fromString;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.apache.commons.collections4.MapUtils.emptyIfNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.apache.commons.lang3.StringUtils;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventure;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.GameMode;
import me.moirai.storyengine.core.domain.adventure.Moderation;

@UseCaseHandler
public class UpdateAdventureHandler extends AbstractUseCaseHandler<UpdateAdventure, AdventureDetails> {

    private static final String USER_NO_PERMISSION = "User does not have permission to delete adventure";
    private static final String ADVENTURE_NOT_FOUND = "Adventure to be updated was not found";
    private static final String ID_CANNOT_BE_NULL_OR_EMPTY = "Adventure ID cannot be null or empty";

    private final AdventureRepository repository;

    public UpdateAdventureHandler(AdventureRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(UpdateAdventure command) {

        if (StringUtils.isBlank(command.getId())) {
            throw new IllegalArgumentException(ID_CANNOT_BE_NULL_OR_EMPTY);
        }
    }

    @Override
    public AdventureDetails execute(UpdateAdventure command) {

        Adventure adventure = repository.findById(command.getId())
                .orElseThrow(() -> new AssetNotFoundException(ADVENTURE_NOT_FOUND));

        if (!adventure.canUserWrite(command.getRequesterDiscordId())) {
            throw new AssetAccessDeniedException(USER_NO_PERMISSION);
        }

        if (isNotBlank(command.getName())) {
            adventure.updateName(command.getName());
        }

        if (isNotBlank(command.getWorldId())) {
            adventure.updateWorld(command.getWorldId());
        }

        if (isNotBlank(command.getPersonaId())) {
            adventure.updatePersona(command.getPersonaId());
        }

        if (isNotBlank(command.getAiModel())) {
            adventure.updateAiModel(fromString(command.getAiModel()));
        }

        if (isNotBlank(command.getModeration())) {
            adventure.updateModeration(Moderation.fromString(command.getModeration()));
        }

        if (isNotBlank(command.getChannelId())) {
            adventure.updateChannel(command.getChannelId());
        }

        if (isNotBlank(command.getGameMode())) {
            adventure.updateGameMode(GameMode.fromString(command.getGameMode()));
        }

        if (command.getTemperature() != null) {
            adventure.updateTemperature(command.getTemperature());
        }

        if (command.getFrequencyPenalty() != null) {
            adventure.updateFrequencyPenalty(command.getFrequencyPenalty());
        }

        if (command.getPresencePenalty() != null) {
            adventure.updatePresencePenalty(command.getPresencePenalty());
        }

        if (isNotBlank(command.getAdventureStart())) {
            adventure.updateAdventureStart(command.getAdventureStart());
        }

        if (isNotBlank(command.getDescription())) {
            adventure.updateDescription(command.getDescription());
        }

        if (isNotBlank(command.getNudge())) {
            adventure.updateNudge(command.getNudge());
        }

        if (isNotBlank(command.getRemember())) {
            adventure.updateRemember(command.getRemember());
        }

        if (isNotBlank(command.getAdventureStart())) {
            adventure.updateAdventureStart(command.getAdventureStart());
        }

        if (isNotBlank(command.getAuthorsNote())) {
            adventure.updateAuthorsNote(command.getAuthorsNote());
        }

        if (isNotBlank(command.getBump())) {
            adventure.updateBump(command.getBump());
            adventure.updateBumpFrequency(command.getBumpFrequency());
        }

        if (command.isMultiplayer()) {
            adventure.makeMultiplayer();
        } else {
            adventure.makeSinglePlayer();
        }

        updateStopSequences(command, adventure);
        updateLogitBias(command, adventure);
        updatePermissions(command, adventure);

        return mapResult(repository.save(adventure));
    }

    private void updatePermissions(UpdateAdventure command, Adventure adventure) {

        if (isNotBlank(command.getVisibility())) {
            if (command.getVisibility().equalsIgnoreCase(PUBLIC.name())) {
                adventure.makePublic();
            } else if (command.getVisibility().equalsIgnoreCase(PRIVATE.name())) {
                adventure.makePrivate();
            }
        }

        emptyIfNull(command.getUsersAllowedToReadToAdd())
                .forEach(adventure::addReaderUser);

        emptyIfNull(command.getUsersAllowedToWriteToAdd())
                .forEach(adventure::addWriterUser);

        emptyIfNull(command.getUsersAllowedToReadToRemove())
                .forEach(adventure::removeReaderUser);

        emptyIfNull(command.getUsersAllowedToWriteToRemove())
                .forEach(adventure::removeWriterUser);
    }

    private void updateLogitBias(UpdateAdventure command, Adventure adventure) {

        emptyIfNull(command.getLogitBiasToAdd())
                .entrySet()
                .stream()
                .forEach(entry -> adventure.addLogitBias(entry.getKey(), entry.getValue()));

        emptyIfNull(command.getLogitBiasToRemove())
                .forEach(adventure::removeLogitBias);
    }

    private void updateStopSequences(UpdateAdventure command, Adventure adventure) {

        emptyIfNull(command.getStopSequencesToAdd())
                .stream()
                .forEach(adventure::addStopSequence);

        emptyIfNull(command.getStopSequencesToRemove())
                .forEach(adventure::removeStopSequence);
    }

    private AdventureDetails mapResult(Adventure savedAdventure) {

        return AdventureDetails.builder()
                .id(savedAdventure.getId())
                .name(savedAdventure.getName())
                .worldId(savedAdventure.getWorldId())
                .personaId(savedAdventure.getPersonaId())
                .visibility(savedAdventure.getVisibility().name())
                .aiModel(savedAdventure.getModelConfiguration().getAiModel().toString())
                .moderation(savedAdventure.getModeration().name())
                .maxTokenLimit(savedAdventure.getModelConfiguration().getMaxTokenLimit())
                .temperature(savedAdventure.getModelConfiguration().getTemperature())
                .frequencyPenalty(savedAdventure.getModelConfiguration().getFrequencyPenalty())
                .presencePenalty(savedAdventure.getModelConfiguration().getPresencePenalty())
                .stopSequences(savedAdventure.getModelConfiguration().getStopSequences())
                .logitBias(savedAdventure.getModelConfiguration().getLogitBias())
                .usersAllowedToWrite(savedAdventure.getUsersAllowedToWrite())
                .usersAllowedToRead(savedAdventure.getUsersAllowedToRead())
                .ownerId(savedAdventure.getOwnerId())
                .creationDate(savedAdventure.getCreationDate())
                .lastUpdateDate(savedAdventure.getLastUpdateDate())
                .description(savedAdventure.getDescription())
                .adventureStart(savedAdventure.getAdventureStart())
                .channelId(savedAdventure.getChannelId())
                .gameMode(savedAdventure.getGameMode().name())
                .authorsNote(savedAdventure.getContextAttributes().getAuthorsNote())
                .nudge(savedAdventure.getContextAttributes().getNudge())
                .remember(savedAdventure.getContextAttributes().getRemember())
                .bump(savedAdventure.getContextAttributes().getBump())
                .bumpFrequency(savedAdventure.getContextAttributes().getBumpFrequency())
                .isMultiplayer(savedAdventure.isMultiplayer())
                .build();
    }
}
