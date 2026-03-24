package me.moirai.storyengine.core.application.command.adventure;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.ContextAttributes;
import me.moirai.storyengine.core.domain.adventure.ModelConfiguration;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.ContextAttributesDto;
import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventure;
import me.moirai.storyengine.core.port.inbound.adventure.ModelConfigurationDto;
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

        var world = getWorldTobeLinked(command);
        var persona = getPersonaToBeLinked(command);

        var modelConfiguration = buildModelConfiguration(command);
        var contextAttributes = buildContextAttributes(command);

        var adventure = repository.save(Adventure.builder()
                .modelConfiguration(modelConfiguration)
                .name(command.name())
                .personaId(persona.getId())
                .worldId(world.getId())
                .channelId(command.channelId())
                .gameMode(command.gameMode())
                .visibility(command.visibility())
                .moderation(command.moderation())
                .isMultiplayer(command.isMultiplayer())
                .adventureStart(world.getAdventureStart())
                .contextAttributes(contextAttributes)
                .description(command.description())
                .build());

        emptyIfNull(command.usersAllowedToRead()).forEach(id -> adventure.grant(new Permission(id, PermissionLevel.READ)));
        emptyIfNull(command.usersAllowedToWrite()).forEach(id -> adventure.grant(new Permission(id, PermissionLevel.WRITE)));

        world.getLorebook().forEach(worldEntry -> adventure.addLorebookEntry(
                worldEntry.getName(),
                worldEntry.getRegex(),
                worldEntry.getDescription(),
                null));

        repository.save(adventure);

        return mapResult(adventure, persona.getPublicId(), world.getPublicId());
    }

    private AdventureDetails mapResult(Adventure adventure, UUID personaPublicId, UUID worldPublicId) {

        var modelConfiguration = new ModelConfigurationDto(
                adventure.getModelConfiguration().getAiModel(),
                adventure.getModelConfiguration().getMaxTokenLimit(),
                adventure.getModelConfiguration().getTemperature(),
                adventure.getModelConfiguration().getFrequencyPenalty(),
                adventure.getModelConfiguration().getPresencePenalty(),
                adventure.getModelConfiguration().getStopSequences(),
                adventure.getModelConfiguration().getLogitBias());

        var contextAttributes = new ContextAttributesDto(
                adventure.getContextAttributes().nudge(),
                adventure.getContextAttributes().authorsNote(),
                adventure.getContextAttributes().remember(),
                adventure.getContextAttributes().bump(),
                adventure.getContextAttributes().bumpFrequency());

        return new AdventureDetails(
                adventure.getPublicId(),
                adventure.getName(),
                adventure.getDescription(),
                adventure.getAdventureStart(),
                worldPublicId,
                personaPublicId,
                adventure.getChannelId(),
                adventure.getVisibility().name(),
                adventure.getModeration().name(),
                adventure.getGameMode().name(),
                adventure.isMultiplayer(),
                adventure.getCreationDate(),
                adventure.getLastUpdateDate(),
                modelConfiguration,
                contextAttributes,
                adventure.getPermissions());
    }

    private Persona getPersonaToBeLinked(CreateAdventure command) {

        var persona = personaRepository.findByPublicId(command.personaId())
                .orElseThrow(() -> new AssetNotFoundException(PERSONA_DOES_NOT_EXIST));

        if (!persona.isPublic() && !persona.canRead(getAuthenticatedUserId())) {
            throw new AssetAccessDeniedException(USER_NO_PERMISSION_IN_PERSONA);
        }

        return persona;
    }

    private World getWorldTobeLinked(CreateAdventure command) {

        var world = worldRepository.findByPublicId(command.worldId())
                .orElseThrow(() -> new AssetNotFoundException(WORLD_DOES_NOT_EXIST));

        if (!world.isPublic() && !world.canRead(getAuthenticatedUserId())) {
            throw new AssetAccessDeniedException(USER_NO_PERMISSION_IN_WORLD);
        }

        return world;
    }

    private Long getAuthenticatedUserId() {
        return ((MoiraiPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).id();
    }

    private ContextAttributes buildContextAttributes(CreateAdventure command) {

        return new ContextAttributes(
                command.nudge(),
                command.authorsNote(),
                command.remember(),
                command.bump(),
                command.bumpFrequency());
    }

    private ModelConfiguration buildModelConfiguration(CreateAdventure command) {

        return ModelConfiguration.builder()
                .aiModel(command.aiModel())
                .maxTokenLimit(command.maxTokenLimit())
                .temperature(command.temperature())
                .frequencyPenalty(command.frequencyPenalty())
                .presencePenalty(command.presencePenalty())
                .stopSequences(command.stopSequences())
                .logitBias(command.logitBias())
                .build();
    }
}
