package me.moirai.storyengine.core.application.command.adventure;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import java.util.UUID;
import java.util.stream.Collectors;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.dto.PermissionDto;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.ContextAttributes;
import me.moirai.storyengine.core.domain.adventure.ModelConfiguration;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureLorebookEntryDetails;
import me.moirai.storyengine.core.port.inbound.adventure.ContextAttributesDto;
import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventure;
import me.moirai.storyengine.core.port.inbound.adventure.ModelConfigurationDto;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.generation.EmbeddingPort;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;
import me.moirai.storyengine.core.port.outbound.vectorsearch.LorebookVectorSearchPort;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@CommandHandler
public class CreateAdventureHandler extends AbstractCommandHandler<CreateAdventure, AdventureDetails> {

    private static final String WORLD_DOES_NOT_EXIST = "The world to be linked to this adventure does not exist";
    private static final String PERSONA_DOES_NOT_EXIST = "The persona to be linked to this adventure does not exist";

    private final WorldRepository worldRepository;
    private final PersonaRepository personaRepository;
    private final AdventureRepository adventureRepository;
    private final UserRepository userRepository;
    private final EmbeddingPort embeddingPort;
    private final LorebookVectorSearchPort vectorSearchPort;

    public CreateAdventureHandler(
            WorldRepository worldRepository,
            PersonaRepository personaRepository,
            AdventureRepository adventureRepository,
            UserRepository userRepository,
            EmbeddingPort embeddingPort,
            LorebookVectorSearchPort vectorSearchPort) {

        this.worldRepository = worldRepository;
        this.personaRepository = personaRepository;
        this.adventureRepository = adventureRepository;
        this.userRepository = userRepository;
        this.embeddingPort = embeddingPort;
        this.vectorSearchPort = vectorSearchPort;
    }

    @Override
    public AdventureDetails execute(CreateAdventure command) {

        var world = worldRepository.findByPublicId(command.worldId())
                .orElseThrow(() -> new NotFoundException(WORLD_DOES_NOT_EXIST));

        var persona = personaRepository.findByPublicId(command.personaId())
                .orElseThrow(() -> new NotFoundException(PERSONA_DOES_NOT_EXIST));

        var modelConfiguration = buildModelConfiguration(command);
        var contextAttributes = buildContextAttributes(command);

        var permissions = emptyIfNull(command.permissions()).stream()
                .map(dto -> {
                    var user = userRepository.findByPublicId(dto.userId())
                            .orElseThrow(() -> new NotFoundException("User not found"));

                    return new Permission(user.getId(), dto.level());
                })
                .collect(Collectors.toSet());

        var adventure = adventureRepository.save(Adventure.builder()
                .modelConfiguration(modelConfiguration)
                .name(command.name())
                .personaId(persona.getId())
                .worldId(world.getId())
                .visibility(command.visibility())
                .moderation(command.moderation())
                .isMultiplayer(command.isMultiplayer())
                .adventureStart(world.getAdventureStart())
                .contextAttributes(contextAttributes)
                .description(command.description())
                .permissions(permissions.toArray(Permission[]::new))
                .build());

        world.getLorebook().forEach(worldEntry -> adventure.addLorebookEntry(
                worldEntry.getName(),
                worldEntry.getDescription(),
                null));

        adventureRepository.save(adventure);

        adventure.getLorebook().forEach(entry -> {
            var vector = embeddingPort.embed(entry.getDescription());
            vectorSearchPort.upsert(adventure.getPublicId(), entry.getPublicId(), vector);
        });

        return mapResult(adventure, persona.getPublicId(), world.getPublicId());
    }

    private AdventureDetails mapResult(Adventure adventure, UUID personaPublicId, UUID worldPublicId) {

        var modelConfiguration = new ModelConfigurationDto(
                adventure.getModelConfiguration().getAiModel(),
                adventure.getModelConfiguration().getMaxTokenLimit(),
                adventure.getModelConfiguration().getTemperature());

        var contextAttributes = new ContextAttributesDto(
                adventure.getContextAttributes().nudge(),
                adventure.getContextAttributes().authorsNote(),
                adventure.getContextAttributes().scene(),
                adventure.getContextAttributes().bump(),
                adventure.getContextAttributes().bumpFrequency());

        return new AdventureDetails(
                adventure.getPublicId(),
                adventure.getName(),
                adventure.getDescription(),
                adventure.getAdventureStart(),
                worldPublicId,
                personaPublicId,
                adventure.getVisibility(),
                adventure.getModeration(),
                adventure.isMultiplayer(),
                adventure.getCreationDate(),
                adventure.getLastUpdateDate(),
                modelConfiguration,
                contextAttributes,
                adventure.getPermissions().stream()
                        .map(permission -> {
                            var user = userRepository.findById(permission.userId())
                                    .orElseThrow(() -> new NotFoundException("User not found"));

                            return new PermissionDto(user.getPublicId(), permission.level());
                        })
                        .collect(Collectors.toSet()),
                adventure.getLorebook().stream()
                        .map(entry -> new AdventureLorebookEntryDetails(
                                entry.getPublicId(),
                                adventure.getPublicId(),
                                entry.getName(),
                                entry.getDescription(),
                                entry.getPlayerId(),
                                entry.isPlayerCharacter(),
                                entry.getCreationDate(),
                                entry.getLastUpdateDate()))
                        .collect(Collectors.toSet()));
    }

    private ContextAttributes buildContextAttributes(CreateAdventure command) {

        return new ContextAttributes(
                command.contextAttributes().nudge(),
                command.contextAttributes().authorsNote(),
                command.contextAttributes().scene(),
                command.contextAttributes().bump(),
                command.contextAttributes().bumpFrequency());
    }

    private ModelConfiguration buildModelConfiguration(CreateAdventure command) {

        return ModelConfiguration.builder()
                .aiModel(command.modelConfiguration().aiModel())
                .maxTokenLimit(command.modelConfiguration().maxTokenLimit())
                .temperature(command.modelConfiguration().temperature())
                .build();
    }
}
