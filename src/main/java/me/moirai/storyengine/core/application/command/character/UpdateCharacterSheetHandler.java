package me.moirai.storyengine.core.application.command.character;

import java.util.Map;

import me.moirai.storyengine.common.annotation.Authorize;
import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.rules.CharacterSheetRules;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.core.domain.character.PlayerCharacter;
import me.moirai.storyengine.core.port.inbound.character.PlayerCharacterDetails;
import me.moirai.storyengine.core.port.inbound.character.UpdateCharacterSheet;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterRepository;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterVectorSearchPort;
import me.moirai.storyengine.core.port.outbound.generation.EmbeddingPort;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@CommandHandler
@Authorize(operation = AuthorizationOperation.UPDATE_CHARACTER_SHEET, fields = "#request.characterId")
public class UpdateCharacterSheetHandler
        extends AbstractCommandHandler<UpdateCharacterSheet, PlayerCharacterDetails> {

    private final PlayerCharacterRepository repository;
    private final UserRepository userRepository;
    private final PlayerCharacterVectorSearchPort vectorSearchPort;
    private final EmbeddingPort embeddingPort;
    private final StoragePort storagePort;

    public UpdateCharacterSheetHandler(
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
    public PlayerCharacterDetails execute(UpdateCharacterSheet command) {

        var character = repository.findByPublicId(command.characterId())
                .orElseThrow(() -> new NotFoundException("Player character not found"));

        var owner = userRepository.findById(character.getPlayerId())
                .orElseThrow(() -> new NotFoundException("Character owner not found"));

        character.updateSheet(command.characterClass(), command.attributes(), command.skills(),
                command.signatureSkill());

        var saved = repository.save(character);

        vectorSearchPort.upsert(saved.getPublicId(), embeddingPort.embed(saved.narrativeDescription()));

        var isOwner = owner.getUsername().equals(command.requesterUsername());

        return mapResult(saved, owner.getUsername(), owner.getDisplayName(), isOwner);
    }

    private PlayerCharacterDetails mapResult(
            PlayerCharacter character,
            String ownerUsername,
            String ownerDisplayName,
            boolean isOwner) {

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
                isOwner);
    }
}
