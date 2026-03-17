package me.moirai.storyengine.core.application.usecase.adventure;

import static me.moirai.storyengine.common.enums.ArtificialIntelligenceModel.fromString;

import java.util.UUID;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.domain.Permissions;
import me.moirai.storyengine.common.enums.GameMode;
import me.moirai.storyengine.common.enums.Moderation;
import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.ContextAttributes;
import me.moirai.storyengine.core.domain.adventure.ModelConfiguration;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventure;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@CommandHandler
public class CreateAdventureHandler extends AbstractCommandHandler<CreateAdventure, AdventureDetails> {

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
                .name(command.name())
                .personaId(persona.getId())
                .worldId(world.getId())
                .channelId(command.channelId())
                .gameMode(GameMode.fromString(command.gameMode()))
                .visibility(Visibility.fromString(command.visibility()))
                .moderation(Moderation.fromString(command.moderation()))
                .isMultiplayer(command.isMultiplayer())
                .adventureStart(world.getAdventureStart())
                .contextAttributes(contextAttributes)
                .description(command.description())
                .build());

        world.getLorebook().forEach(worldEntry -> adventure.addLorebookEntry(
                worldEntry.getName(),
                worldEntry.getRegex(),
                worldEntry.getDescription(),
                null));

        repository.save(adventure);

        return mapResult(adventure, persona.getPublicId(), world.getPublicId());
    }

    private AdventureDetails mapResult(Adventure adventure, UUID personaPublicId, UUID worldPublicId) {

        return new AdventureDetails(
                adventure.getPublicId(),
                adventure.getName(),
                adventure.getDescription(),
                adventure.getAdventureStart(),
                worldPublicId,
                personaPublicId,
                adventure.getChannelId(),
                adventure.getVisibility().name(),
                adventure.getModelConfiguration().aiModel().toString(),
                adventure.getModeration().name(),
                adventure.getGameMode().name(),
                adventure.getOwnerId(),
                adventure.getContextAttributes().nudge(),
                adventure.getContextAttributes().remember(),
                adventure.getContextAttributes().authorsNote(),
                adventure.getContextAttributes().bump(),
                adventure.getContextAttributes().bumpFrequency(),
                adventure.getModelConfiguration().maxTokenLimit(),
                adventure.getModelConfiguration().temperature(),
                adventure.getModelConfiguration().frequencyPenalty(),
                adventure.getModelConfiguration().presencePenalty(),
                adventure.isMultiplayer(),
                adventure.getCreationDate(),
                adventure.getLastUpdateDate(),
                adventure.getModelConfiguration().logitBias(),
                adventure.getModelConfiguration().stopSequences(),
                adventure.getUsersAllowedToRead(),
                adventure.getUsersAllowedToWrite());
    }

    private Persona getPersonaToBeLinked(CreateAdventure command) {

        Persona persona = personaRepository.findByPublicId(command.personaId())
                .orElseThrow(() -> new AssetNotFoundException(PERSONA_DOES_NOT_EXIST));

        if (!persona.canUserRead(command.requesterId())) {
            throw new AssetAccessDeniedException(USER_NO_PERMISSION_IN_PERSONA);
        }

        return persona;
    }

    private World getWorldTobeLinked(CreateAdventure command) {

        World world = worldRepository.findByPublicId(command.worldId())
                .orElseThrow(() -> new AssetNotFoundException(WORLD_DOES_NOT_EXIST));

        if (!world.canUserRead(command.requesterId())) {
            throw new AssetAccessDeniedException(USER_NO_PERMISSION_IN_WORLD);
        }

        return world;
    }

    private ContextAttributes buildContextAttributes(CreateAdventure command) {

        return new ContextAttributes(
                command.nudge(),
                command.authorsNote(),
                command.remember(),
                command.bump(),
                command.bumpFrequency());
    }

    private Permissions buildPermissions(CreateAdventure command) {

        return Permissions.builder()
                .ownerId(command.requesterId())
                .usersAllowedToRead(command.usersAllowedToRead())
                .usersAllowedToWrite(command.usersAllowedToWrite())
                .build();
    }

    private ModelConfiguration buildModelConfiguration(CreateAdventure command) {

        return new ModelConfiguration(
                fromString(command.aiModel()),
                command.maxTokenLimit(),
                command.temperature(),
                command.frequencyPenalty(),
                command.presencePenalty(),
                command.stopSequences(),
                command.logitBias());
    }
}
