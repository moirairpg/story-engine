package me.moirai.storyengine.core.application.command.character;

import java.util.Map;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.rules.CharacterSheetRules;
import me.moirai.storyengine.core.domain.character.PlayerCharacter;
import me.moirai.storyengine.core.port.inbound.character.CreatePlayerCharacter;
import me.moirai.storyengine.core.port.inbound.character.PlayerCharacterDetails;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterRepository;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterVectorSearchPort;
import me.moirai.storyengine.core.port.outbound.generation.EmbeddingPort;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@CommandHandler
public class CreatePlayerCharacterHandler
        extends AbstractCommandHandler<CreatePlayerCharacter, PlayerCharacterDetails> {

    private final PlayerCharacterRepository repository;
    private final UserRepository userRepository;
    private final PlayerCharacterVectorSearchPort vectorSearchPort;
    private final EmbeddingPort embeddingPort;
    private final StoragePort storagePort;

    public CreatePlayerCharacterHandler(
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
    public PlayerCharacterDetails execute(CreatePlayerCharacter command) {

        var newCharacter = PlayerCharacter.builder()
                .name(command.name())
                .characterClass(command.characterClass())
                .personality(command.personality())
                .physicalDescription(command.physicalDescription())
                .background(command.background())
                .attributes(command.attributes())
                .skills(command.skills())
                .signatureSkill(command.signatureSkill())
                .playerId(command.requesterId())
                .build();

        newCharacter.updateUiImagePosition(command.uiImagePositionX(), command.uiImagePositionY());

        var character = repository.save(newCharacter);

        var owner = userRepository.findById(character.getPlayerId())
                .orElseThrow(() -> new NotFoundException("Character owner not found"));

        var vector = embeddingPort.embed(character.narrativeDescription());
        vectorSearchPort.upsert(character.getPublicId(), vector);

        return mapResult(character, owner.getUsername(), owner.getDisplayName());
    }

    private PlayerCharacterDetails mapResult(
            PlayerCharacter character,
            String ownerUsername,
            String ownerDisplayName) {

        return new PlayerCharacterDetails(
                character.getPublicId(),
                ownerUsername,
                ownerDisplayName,
                character.getName(),
                character.getCharacterClass(),
                character.getPersonality(),
                character.getPhysicalDescription(),
                character.getBackground(),
                character.getAttributeLevels().asMap(),
                character.getSkillLevels().asMap(),
                Map.of(character.getCharacterClass().getSignature(), character.getSkillLevels().signature()),
                character.getXp(),
                character.getLevel(),
                character.getUnspentAttributePoints(),
                character.getUnspentSkillPoints(),
                CharacterSheetRules.LEVEL_UP_XP_THRESHOLD,
                storagePort.resolveUrl(character.getImageKey()),
                character.getUiImagePositionX(),
                character.getUiImagePositionY(),
                character.getCreationDate(),
                character.getLastUpdateDate(),
                true,
                true
        );
    }
}