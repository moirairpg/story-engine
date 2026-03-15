package me.moirai.storyengine.core.application.usecase.adventure;

import static me.moirai.storyengine.core.domain.adventure.ArtificialIntelligenceModel.fromString;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.domain.Permissions;
import me.moirai.storyengine.common.domain.Visibility;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.ContextAttributes;
import me.moirai.storyengine.core.domain.adventure.GameMode;
import me.moirai.storyengine.core.domain.adventure.ModelConfiguration;
import me.moirai.storyengine.core.domain.adventure.Moderation;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventure;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@UseCaseHandler
public class CreateAdventureHandler extends AbstractUseCaseHandler<CreateAdventure, AdventureDetails> {

    private static final String USER_NO_PERMISSION_IN_WORLD = "User does not have permission to view the world to be linked to this adventure";
    private static final String USER_NO_PERMISSION_IN_PERSONA = "User does not have permission to view the persona to be linked to this adventure";
    private static final String WORLD_DOES_NOT_EXIST = "The world to be linked to this adventure does not exist";
    private static final String PERSONA_DOES_NOT_EXIST = "The persona to be linked to this adventure does not exist";

    private final WorldRepository worldRepository;
    private final PersonaRepository personaRepository;
    private final AdventureRepository repository;

    public CreateAdventureHandler(
            WorldRepository worldRepository,
            PersonaRepository personaRepository,
            AdventureRepository repository) {

        this.worldRepository = worldRepository;
        this.personaRepository = personaRepository;
        this.repository = repository;
    }

    @Override
    public AdventureDetails execute(CreateAdventure command) {

        World world = getWorldTobeLinked(command);
        Persona persona = getPersonaToBeLinked(command);

        ModelConfiguration modelConfiguration = buildModelConfiguration(command);
        Permissions permissions = buildPermissions(command);
        ContextAttributes contextAttributes = buildContextAttributes(command);

        Adventure adventure = repository.save(Adventure.builder()
                .modelConfiguration(modelConfiguration)
                .permissions(permissions)
                .name(command.getName())
                .personaId(persona.getId())
                .worldId(world.getId())
                .channelId(command.getChannelId())
                .gameMode(GameMode.fromString(command.getGameMode()))
                .visibility(Visibility.fromString(command.getVisibility()))
                .moderation(Moderation.fromString(command.getModeration()))
                .isMultiplayer(command.isMultiplayer())
                .adventureStart(world.getAdventureStart())
                .contextAttributes(contextAttributes)
                .description(command.getDescription())
                .build());

        world.getLorebook().forEach(worldEntry -> adventure.addLorebookEntry(
                worldEntry.getName(),
                worldEntry.getRegex(),
                worldEntry.getDescription(),
                null));

        repository.save(adventure);

        return mapResult(adventure, persona.getPublicId(), world.getPublicId());
    }

    private AdventureDetails mapResult(Adventure adventure, String personaPublicId, String worldPublicId) {

        return AdventureDetails.builder()
                .id(adventure.getPublicId())
                .name(adventure.getName())
                .worldId(worldPublicId)
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

    private Persona getPersonaToBeLinked(CreateAdventure command) {

        Persona persona = personaRepository.findByPublicId(command.getPersonaId())
                .orElseThrow(() -> new AssetNotFoundException(PERSONA_DOES_NOT_EXIST));

        if (!persona.canUserRead(command.getRequesterDiscordId())) {
            throw new AssetAccessDeniedException(USER_NO_PERMISSION_IN_PERSONA);
        }

        return persona;
    }

    private World getWorldTobeLinked(CreateAdventure command) {

        World world = worldRepository.findByPublicId(command.getWorldId())
                .orElseThrow(() -> new AssetNotFoundException(WORLD_DOES_NOT_EXIST));

        if (!world.canUserRead(command.getRequesterDiscordId())) {
            throw new AssetAccessDeniedException(USER_NO_PERMISSION_IN_WORLD);
        }

        return world;
    }

    private ContextAttributes buildContextAttributes(CreateAdventure command) {

        return ContextAttributes.builder()
                .authorsNote(command.getAuthorsNote())
                .nudge(command.getNudge())
                .remember(command.getRemember())
                .bump(command.getBump())
                .bumpFrequency(command.getBumpFrequency())
                .build();
    }

    private Permissions buildPermissions(CreateAdventure command) {

        return Permissions.builder()
                .ownerId(command.getRequesterDiscordId())
                .usersAllowedToRead(command.getUsersAllowedToRead())
                .usersAllowedToWrite(command.getUsersAllowedToWrite())
                .build();
    }

    private ModelConfiguration buildModelConfiguration(CreateAdventure command) {

        return ModelConfiguration.builder()
                .aiModel(fromString(command.getAiModel()))
                .frequencyPenalty(command.getFrequencyPenalty())
                .presencePenalty(command.getPresencePenalty())
                .temperature(command.getTemperature())
                .logitBias(command.getLogitBias())
                .maxTokenLimit(command.getMaxTokenLimit())
                .stopSequences(command.getStopSequences())
                .build();
    }
}
