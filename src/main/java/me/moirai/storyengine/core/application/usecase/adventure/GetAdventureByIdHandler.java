package me.moirai.storyengine.core.application.usecase.adventure;

import org.apache.commons.lang3.StringUtils;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureById;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;

@UseCaseHandler
public class GetAdventureByIdHandler extends AbstractUseCaseHandler<GetAdventureById, AdventureDetails> {

    private static final String ADVENTURE_NOT_FOUND = "Adventure to be viewed was not found";
    private static final String ID_CANNOT_BE_NULL_OR_EMPTY = "Adventure ID cannot be null or empty";
    private static final String USER_NO_PERMISSION = "User does not have permission to view adventure";
    private static final String PERSONA_NOT_FOUND = "Persona not found";

    private final AdventureRepository queryRepository;
    private final PersonaRepository personaRepository;

    public GetAdventureByIdHandler(AdventureRepository queryRepository, PersonaRepository personaRepository) {
        this.queryRepository = queryRepository;
        this.personaRepository = personaRepository;
    }

    @Override
    public void validate(GetAdventureById command) {

        if (StringUtils.isBlank(command.getId())) {
            throw new IllegalArgumentException(ID_CANNOT_BE_NULL_OR_EMPTY);
        }
    }

    @Override
    public AdventureDetails execute(GetAdventureById query) {

        Adventure adventure = queryRepository.findById(query.getId())
                .orElseThrow(() -> new AssetNotFoundException(ADVENTURE_NOT_FOUND));

        if (!adventure.canUserRead(query.getRequesterDiscordId())) {
            throw new AssetAccessDeniedException(USER_NO_PERMISSION);
        }

        Persona persona = personaRepository.findById(adventure.getPersonaId())
                .orElseThrow(() -> new AssetNotFoundException(PERSONA_NOT_FOUND));

        return mapResult(adventure, persona.getPublicId());
    }

    private AdventureDetails mapResult(Adventure adventure, String personaPublicId) {

        return AdventureDetails.builder()
                .id(adventure.getId())
                .name(adventure.getName())
                .worldId(adventure.getWorldId())
                .personaId(personaPublicId)
                .visibility(adventure.getVisibility().name())
                .aiModel(adventure.getModelConfiguration().getAiModel().toString())
                .moderation(adventure.getModeration().name())
                .maxTokenLimit(adventure.getModelConfiguration().getMaxTokenLimit())
                .temperature(adventure.getModelConfiguration().getTemperature())
                .frequencyPenalty(adventure.getModelConfiguration().getFrequencyPenalty())
                .presencePenalty(adventure.getModelConfiguration().getPresencePenalty())
                .stopSequences(adventure.getModelConfiguration().getStopSequences())
                .logitBias(adventure.getModelConfiguration().getLogitBias())
                .usersAllowedToWrite(adventure.getUsersAllowedToWrite())
                .usersAllowedToRead(adventure.getUsersAllowedToRead())
                .ownerId(adventure.getOwnerId())
                .creationDate(adventure.getCreationDate())
                .lastUpdateDate(adventure.getLastUpdateDate())
                .description(adventure.getDescription())
                .adventureStart(adventure.getAdventureStart())
                .channelId(adventure.getChannelId())
                .gameMode(adventure.getGameMode().name())
                .authorsNote(adventure.getContextAttributes().getAuthorsNote())
                .nudge(adventure.getContextAttributes().getNudge())
                .remember(adventure.getContextAttributes().getRemember())
                .bump(adventure.getContextAttributes().getBump())
                .bumpFrequency(adventure.getContextAttributes().getBumpFrequency())
                .isMultiplayer(adventure.isMultiplayer())
                .build();
    }
}
