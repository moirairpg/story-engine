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
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;
import me.moirai.storyengine.core.port.outbound.vectorsearch.LorebookVectorSearchPort;

@CommandHandler
public class CreateAdventureHandler extends AbstractCommandHandler<CreateAdventure, AdventureDetails> {

    private final AdventureRepository adventureRepository;
    private final UserRepository userRepository;
    private final EmbeddingPort embeddingPort;
    private final LorebookVectorSearchPort vectorSearchPort;

    public CreateAdventureHandler(
            AdventureRepository adventureRepository,
            UserRepository userRepository,
            EmbeddingPort embeddingPort,
            LorebookVectorSearchPort vectorSearchPort) {

        this.adventureRepository = adventureRepository;
        this.userRepository = userRepository;
        this.embeddingPort = embeddingPort;
        this.vectorSearchPort = vectorSearchPort;
    }

    @Override
    public AdventureDetails execute(CreateAdventure command) {

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
                .narrator(command.narratorName(), command.narratorPersonality())
                .worldId(command.worldId())
                .visibility(command.visibility())
                .moderation(command.moderation())
                .isMultiplayer(command.isMultiplayer())
                .adventureStart(command.adventureStart())
                .contextAttributes(contextAttributes)
                .description(command.description())
                .permissions(permissions.toArray(Permission[]::new))
                .build());

        emptyIfNull(command.lorebookEntries()).forEach(entry ->
                adventure.addLorebookEntry(entry.name(), entry.description(), entry.playerId()));

        adventureRepository.save(adventure);

        var entries = adventure.getLorebook().stream().toList();

        if (!entries.isEmpty()) {
            var texts = entries.stream()
                    .map(e -> e.getName() + ": " + e.getDescription())
                    .toList();

            var vectors = embeddingPort.embedAll(texts);
            var vectorIterator = vectors.iterator();

            entries.forEach(entry -> vectorSearchPort.upsert(adventure.getPublicId(), entry.getPublicId(), vectorIterator.next()));
        }

        return mapResult(adventure, command.worldId());
    }

    private AdventureDetails mapResult(Adventure adventure, UUID worldPublicId) {

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
                adventure.getNarratorName(),
                adventure.getNarratorPersonality(),
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
