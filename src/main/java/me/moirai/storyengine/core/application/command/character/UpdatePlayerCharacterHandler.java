package me.moirai.storyengine.core.application.command.character;

import static org.apache.commons.lang3.StringUtils.isBlank;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.character.PlayerCharacter;
import me.moirai.storyengine.core.port.inbound.character.PlayerCharacterDetails;
import me.moirai.storyengine.core.port.inbound.character.UpdatePlayerCharacter;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterRepository;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterVectorSearchPort;
import me.moirai.storyengine.core.port.outbound.generation.EmbeddingPort;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@CommandHandler
public class UpdatePlayerCharacterHandler
        extends AbstractCommandHandler<UpdatePlayerCharacter, PlayerCharacterDetails> {

    private static final String RAG_EMBEDDING_TEXT = "%s: %s; %s; %s";

    private final PlayerCharacterRepository repository;
    private final UserRepository userRepository;
    private final PlayerCharacterVectorSearchPort vectorSearchPort;
    private final EmbeddingPort embeddingPort;
    private final StoragePort storagePort;

    public UpdatePlayerCharacterHandler(
            PlayerCharacterRepository repository,
            UserRepository userRepository,
            PlayerCharacterVectorSearchPort vectorSearchPort,
            EmbeddingPort embeddingPort,
            StoragePort storagePort) {

        this.repository = repository;
        this.userRepository = userRepository;
        this.vectorSearchPort = vectorSearchPort;
        this.embeddingPort = embeddingPort;
        this.storagePort = storagePort;
    }

    @Override
    public void validate(UpdatePlayerCharacter command) {

        if (isBlank(command.name())) {
            throw new BusinessRuleViolationException("Character name cannot be null or empty");
        }

        if (isBlank(command.personality())) {
            throw new BusinessRuleViolationException("Character personality cannot be null or empty");
        }

        if (isBlank(command.physicalDescription())) {
            throw new BusinessRuleViolationException("Character physical description cannot be null or empty");
        }

        if (command.characterClass() == null) {
            throw new BusinessRuleViolationException("Character class cannot be null");
        }
    }

    @Override
    public PlayerCharacterDetails execute(UpdatePlayerCharacter command) {

        var character = repository.findByPublicId(command.characterId())
                .orElseThrow(() -> new NotFoundException("Player character not found"));

        var owner = userRepository.findById(character.getPlayerId())
                .orElseThrow(() -> new NotFoundException("Character owner not found"));

        character.updateName(command.name());
        character.updatePersonality(command.personality());
        character.updatePhysicalDescription(command.physicalDescription());
        character.updateCharacterClass(command.characterClass());

        var saved = repository.save(character);

        vectorSearchPort.upsert(saved.getPublicId(), embeddingPort.embed(buildEmbeddingText(saved)));

        return mapResult(saved, owner.getUsername());
    }

    private String buildEmbeddingText(PlayerCharacter character) {
        return String.format(RAG_EMBEDDING_TEXT,
                character.getName(),
                character.getCharacterClass().name(),
                character.getPersonality(),
                character.getPhysicalDescription());
    }

    private PlayerCharacterDetails mapResult(PlayerCharacter character, String ownerUsername) {

        return new PlayerCharacterDetails(
                character.getPublicId(),
                ownerUsername,
                character.getName(),
                character.getCharacterClass(),
                character.getPersonality(),
                character.getPhysicalDescription(),
                storagePort.resolveUrl(character.getImageKey()),
                character.getCreationDate(),
                character.getLastUpdateDate());
    }
}