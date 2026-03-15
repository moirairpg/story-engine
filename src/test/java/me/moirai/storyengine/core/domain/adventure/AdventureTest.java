package me.moirai.storyengine.core.domain.adventure;

import static me.moirai.storyengine.common.domain.Visibility.PRIVATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import me.moirai.storyengine.common.domain.Permissions;
import me.moirai.storyengine.common.domain.Visibility;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
import me.moirai.storyengine.core.domain.PermissionsFixture;

public class AdventureTest {

    @Test
    public void createAdventure_whenValidData_thenCreateAdventure() {

        // Given
        Adventure.Builder adventureBuilder = Adventure.builder();
        adventureBuilder.id("ID");
        adventureBuilder.name("Name");
        adventureBuilder.worldId("WRLDID");
        adventureBuilder.personaId(1L);
        adventureBuilder.channelId("CHNLID");
        adventureBuilder.moderation(Moderation.STRICT);
        adventureBuilder.visibility(Visibility.fromString("PRIVATE"));
        adventureBuilder.modelConfiguration(ModelConfigurationFixture.gpt4Mini().build());
        adventureBuilder.permissions(PermissionsFixture.samplePermissions().build());
        adventureBuilder.gameMode(GameMode.RPG);

        // When
        Adventure adventure = adventureBuilder.build();

        // Then
        assertThat(adventure).isNotNull();
        assertThat(adventure.getId()).isEqualTo("ID");
        assertThat(adventure.getName()).isEqualTo("Name");
        assertThat(adventure.getWorldId()).isEqualTo("WRLDID");
        assertThat(adventure.getPersonaId()).isEqualTo(1L);
        assertThat(adventure.getModeration()).isEqualTo(Moderation.STRICT);
        assertThat(adventure.getVisibility()).isEqualTo(PRIVATE);
    }

    @Test
    public void createAdventure_whenNameIsNull_thenThrowException() {

        // Given
        Adventure.Builder adventureBuilder = AdventureFixture.privateSingleplayerAdventure().name(null);

        // Then
        assertThrows(BusinessRuleViolationException.class, adventureBuilder::build);
    }

    @Test
    public void createAdventure_whenNameIsEmpty_thenThrowException() {

        // Given
        Adventure.Builder adventureBuilder = AdventureFixture.privateSingleplayerAdventure().name(StringUtils.EMPTY);

        // Then
        assertThrows(BusinessRuleViolationException.class, adventureBuilder::build);
    }

    @Test
    public void createAdventure_whenModelConfigurationIsNull_thenThrowException() {

        // Given
        Adventure.Builder adventureBuilder = AdventureFixture.privateSingleplayerAdventure().modelConfiguration(null);

        // Then
        assertThrows(BusinessRuleViolationException.class, adventureBuilder::build);
    }

    @Test
    public void createAdventure_whenModerationIsNull_thenThrowException() {

        // Given
        Adventure.Builder adventureBuilder = AdventureFixture.privateSingleplayerAdventure().moderation(null);

        // Then
        assertThrows(BusinessRuleViolationException.class, adventureBuilder::build);
    }

    @Test
    public void createAdventure_whenVisibilityIsNull_thenThrowException() {

        // Given
        Adventure.Builder adventureBuilder = AdventureFixture.privateSingleplayerAdventure().visibility(null);

        // Then
        assertThrows(BusinessRuleViolationException.class, adventureBuilder::build);
    }

    @Test
    public void createAdventure_whenPermissionsIsNull_thenThrowException() {

        // Given
        Adventure.Builder adventureBuilder = AdventureFixture.privateSingleplayerAdventure().permissions(null);

        // Then
        assertThrows(BusinessRuleViolationException.class, adventureBuilder::build);
    }

    @Test
    public void updateAdventure_whenAddNewWriter_thenTheyShouldHaveReadAndWritePermission() {

        // Given
        String userId = "1234567890";
        Adventure.Builder adventureBuilder = AdventureFixture.privateSingleplayerAdventure();
        Permissions permissions = PermissionsFixture.samplePermissions()
                .usersAllowedToWrite(new HashSet<>()).build();

        adventureBuilder.permissions(permissions);

        Adventure adventure = adventureBuilder.build();

        // When
        adventure.addWriterUser(userId);

        // Then
        assertThat(adventure.getUsersAllowedToWrite()).contains(userId);
        assertThat(adventure.canUserWrite(userId)).isTrue();
        assertThat(adventure.canUserRead(userId)).isTrue();
    }

    @Test
    public void updateAdventure_whenAddNewReader_thenTheyShouldHaveOnlyReadPermission() {

        // Given
        String userId = "1234567890";
        Adventure.Builder adventureBuilder = AdventureFixture.privateSingleplayerAdventure();
        Permissions permissions = PermissionsFixture.samplePermissions()
                .usersAllowedToRead(new HashSet<>()).build();

        adventureBuilder.permissions(permissions);

        Adventure adventure = adventureBuilder.build();

        // When
        adventure.addReaderUser(userId);

        // Then
        assertThat(adventure.getUsersAllowedToRead()).contains(userId);
        assertThat(adventure.canUserWrite(userId)).isFalse();
        assertThat(adventure.canUserRead(userId)).isTrue();
    }

    @Test
    public void updateAdventure_whenRemoveReader_thenReadPermissionShouldBeRevoked() {

        // Given
        String userId = "1234567890";
        Adventure.Builder adventureBuilder = AdventureFixture.privateSingleplayerAdventure();

        Set<String> usersAllowedToRead = new HashSet<>();
        usersAllowedToRead.add(userId);

        Permissions permissions = PermissionsFixture.samplePermissions()
                .usersAllowedToRead(usersAllowedToRead).build();

        adventureBuilder.permissions(permissions);

        Adventure adventure = adventureBuilder.build();

        // When
        adventure.removeReaderUser(userId);

        // Then
        assertThat(adventure.getUsersAllowedToRead()).doesNotContain(userId);
        assertThat(adventure.canUserWrite(userId)).isFalse();
        assertThat(adventure.canUserRead(userId)).isFalse();
    }

    @Test
    public void updateAdventure_whenRemoveWriter_thenReadAndWritePermissionShouldBeRevoked() {

        // Given
        String userId = "1234567890";
        Adventure.Builder adventureBuilder = AdventureFixture.privateSingleplayerAdventure();

        Set<String> usersAllowedToWrite = new HashSet<>();
        usersAllowedToWrite.add(userId);

        Permissions permissions = PermissionsFixture.samplePermissions()
                .usersAllowedToWrite(usersAllowedToWrite).build();

        adventureBuilder.permissions(permissions);

        Adventure adventure = adventureBuilder.build();

        // When
        adventure.removeWriterUser(userId);

        // Then
        assertThat(adventure.getUsersAllowedToWrite()).doesNotContain(userId);
        assertThat(adventure.canUserWrite(userId)).isFalse();
        assertThat(adventure.canUserRead(userId)).isFalse();
    }

    @Test
    public void updateAdventure_whenTurningPrivateIntoPublic_thenPermissionShouldBeChangedToPublic() {

        // Given
        Adventure adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // When
        adventure.makePublic();

        // Then
        assertThat(adventure.isPublic()).isTrue();
    }

    @Test
    public void updateAdventure_whenTurningPublicIntoPrivate_thenPermissionShouldBeChangedToPrivate() {

        // Given
        Adventure adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // When
        adventure.makePrivate();

        // Then
        assertThat(adventure.isPublic()).isFalse();
    }

    @Test
    public void updateAdventure_whenNewNameProvided_thenNameShouldBeUpdated() {

        // Given
        String name = "New Name";
        Adventure adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // When
        adventure.updateName(name);

        // Then
        assertThat(adventure.getName()).isEqualTo(name);
    }

    @Test
    public void updateAdventure_whenNewPersonaIdProvided_thenPersonaIdShouldBeUpdated() {

        // Given
        Long personaId = 42L;
        Adventure adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // When
        adventure.updatePersona(personaId);

        // Then
        assertThat(adventure.getPersonaId()).isEqualTo(personaId);
    }

    @Test
    public void updateAdventure_whenNewModerationProvided_thenModerationShouldBeUpdated() {

        // Given
        Moderation moderation = Moderation.DISABLED;
        Adventure adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // When
        adventure.updateModeration(moderation);

        // Then
        assertThat(adventure.getModeration()).isEqualTo(moderation);
    }

    @Test
    public void updateAdventure_whenNewAiModelIsProvided_thenAiModelShouldBeUpdated() {

        // Given
        ArtificialIntelligenceModel aiModel = ArtificialIntelligenceModel.GPT4_OMNI;
        Adventure adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // When
        adventure.updateAiModel(aiModel);

        // Then
        assertThat(adventure.getModelConfiguration().getAiModel()).isEqualTo(aiModel);
    }

    @Test
    public void updateAdventure_whenNewMaxTokenLimit_thenMaxTokenLimitShouldBeUpdated() {

        // Given
        int maxTokenLimit = 100;
        ArtificialIntelligenceModel aiModel = ArtificialIntelligenceModel.GPT4_MINI;
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini().aiModel(aiModel).build();
        Adventure adventure = AdventureFixture.privateSingleplayerAdventure().modelConfiguration(modelConfiguration)
                .build();

        // When
        adventure.updateMaxTokenLimit(maxTokenLimit);

        // Then
        assertThat(adventure.getModelConfiguration().getMaxTokenLimit()).isEqualTo(maxTokenLimit);
    }

    @Test
    public void updateAdventure_whenNewMaxTokenLimitGreaterThanAllowed_thenThrowException() {

        // Given
        int maxTokenLimit = 500000;
        ArtificialIntelligenceModel aiModel = ArtificialIntelligenceModel.GPT4_MINI;
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini().aiModel(aiModel).build();
        Adventure adventure = AdventureFixture.privateSingleplayerAdventure().modelConfiguration(modelConfiguration)
                .build();

        // Then
        assertThrows(BusinessRuleViolationException.class,
                () -> adventure.updateMaxTokenLimit(maxTokenLimit));
    }

    @Test
    public void updateAdventure_whenNewMaxTokenLimitLesserThanAllowed_thenThrowException() {

        // Given
        int maxTokenLimit = 10;
        ArtificialIntelligenceModel aiModel = ArtificialIntelligenceModel.GPT4_MINI;
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini().aiModel(aiModel).build();
        Adventure adventure = AdventureFixture.privateSingleplayerAdventure().modelConfiguration(modelConfiguration)
                .build();

        // Then
        assertThrows(BusinessRuleViolationException.class,
                () -> adventure.updateMaxTokenLimit(maxTokenLimit));
    }

    @Test
    public void updateAdventure_whenNewTemperature_thenTemperatureShouldBeUpdated() {

        // Given
        double temperature = 1.3;
        Adventure adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // When
        adventure.updateTemperature(temperature);

        // Then
        assertThat(adventure.getModelConfiguration().getTemperature()).isEqualTo(temperature);
    }

    @Test
    public void updateAdventure_whenNewTemperatureGreaterThanAllowed_thenThrowException() {

        // Given
        double temperature = 3;
        Adventure adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // Then
        assertThrows(BusinessRuleViolationException.class,
                () -> adventure.updateTemperature(temperature));
    }

    @Test
    public void updateAdventure_whenNewTemperatureLesserThanAllowed_thenThrowException() {

        // Given
        double temperature = 0;
        Adventure adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // Then
        assertThrows(BusinessRuleViolationException.class,
                () -> adventure.updateTemperature(temperature));
    }

    @Test
    public void updateAdventure_whenNewPresencePenalty_thenPresencePenaltyIsUpdated() {

        // Given
        double presencePenalty = 1.3;
        Adventure adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // When
        adventure.updatePresencePenalty(presencePenalty);

        // Then
        assertThat(adventure.getModelConfiguration().getPresencePenalty()).isEqualTo(presencePenalty);
    }

    @Test
    public void updateAdventure_whenPresencePenaltyGreaterThanAllowed_thenThrowException() {

        // Given
        double presencePenalty = 3;
        Adventure adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // Then
        assertThrows(BusinessRuleViolationException.class,
                () -> adventure.updatePresencePenalty(presencePenalty));
    }

    @Test
    public void updateAdventure_whenPresencePenaltyLesserThanAllowed_thenThrowException() {

        // Given
        double presencePenalty = -3;
        Adventure adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // Then
        assertThrows(BusinessRuleViolationException.class,
                () -> adventure.updatePresencePenalty(presencePenalty));
    }

    @Test
    public void updateAdventure_whenNewFrequencyPenalty_thenFrequencyPenaltyIsUpdated() {

        // Given
        double frequencyPenalty = 1.3;
        Adventure adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // When
        adventure.updateFrequencyPenalty(frequencyPenalty);

        // Then
        assertThat(adventure.getModelConfiguration().getFrequencyPenalty()).isEqualTo(frequencyPenalty);
    }

    @Test
    public void updateAdventure_whenFrequencyPenaltyGreaterThanAllowed_thenThrowException() {

        // Given
        double frequencyPenalty = 3;
        Adventure adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // Then
        assertThrows(BusinessRuleViolationException.class,
                () -> adventure.updateFrequencyPenalty(frequencyPenalty));
    }

    @Test
    public void updateAdventure_whenPresenceFrequencyLesserThanAllowed_thenThrowException() {

        // Given
        double frequencyPenalty = -3;
        Adventure adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // Then
        assertThrows(BusinessRuleViolationException.class,
                () -> adventure.updateFrequencyPenalty(frequencyPenalty));
    }

    @Test
    public void updateAdventure_whenNewLogitBias_thenLogitBiasShouldBeUpdated() {

        // Given
        String token = "TOKEN";
        double bias = 1.3;
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini()
                .logitBias(new HashMap<>())
                .build();

        Adventure adventure = AdventureFixture.privateSingleplayerAdventure()
                .modelConfiguration(modelConfiguration)
                .build();

        // When
        adventure.addLogitBias(token, bias);

        // Then
        assertThat(adventure.getModelConfiguration().getLogitBias()).containsEntry(token, bias);
    }

    @Test
    public void updateAdventure_whenLogitBiasGreaterThanAllowed_thenThrowException() {

        // Given
        String token = "TOKEN";
        double bias = 200;
        Adventure adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // Then
        assertThrows(BusinessRuleViolationException.class,
                () -> adventure.addLogitBias(token, bias));
    }

    @Test
    public void updateAdventure_whenLogitBiasLesserThanAllowed_thenThrowException() {

        // Given
        String token = "TOKEN";
        double bias = -200;
        Adventure adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // Then
        assertThrows(BusinessRuleViolationException.class,
                () -> adventure.addLogitBias(token, bias));
    }

    @Test
    public void updateAdventure_whenNewStopSequence_thenStopSequenceShouldBeAdded() {

        // Given
        String token = "TOKEN";
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini()
                .stopSequences(new HashSet<>())
                .build();

        Adventure adventure = AdventureFixture.privateSingleplayerAdventure()
                .modelConfiguration(modelConfiguration)
                .build();

        // When
        adventure.addStopSequence(token);

        // Then
        assertThat(adventure.getModelConfiguration().getStopSequences()).containsExactly(token);
    }

    @Test
    public void updateAdventure_whenRemovedStopSequence_thenStopSequenceShouldBeRemoved() {

        // Given
        String token = "TOKEN";
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini()
                .stopSequences(Collections.singleton(token))
                .build();

        Adventure adventure = AdventureFixture.privateSingleplayerAdventure()
                .modelConfiguration(modelConfiguration)
                .build();

        // When
        adventure.removeStopSequence(token);

        // Then
        assertThat(adventure.getModelConfiguration().getStopSequences()).doesNotContain(token);
    }

    @Test
    public void updateAdventure_whenRemovedLogitBias_thenLogitBiasShouldBeRemoved() {

        // Given
        String token = "TOKEN";
        double bias = 1.3;
        ModelConfiguration modelConfiguration = ModelConfigurationFixture.gpt4Mini()
                .logitBias(Collections.singletonMap(token, bias))
                .build();

        Adventure adventure = AdventureFixture.privateSingleplayerAdventure()
                .modelConfiguration(modelConfiguration)
                .build();

        // When
        adventure.removeLogitBias(token);

        // Then
        assertThat(adventure.getModelConfiguration().getLogitBias()).doesNotContainKey(token);
    }

    @Test
    public void createAdventure_whenDiscordChannelIdIsNull_thenThrowException() {

        // Given
        Adventure.Builder adventureBuilder = AdventureFixture.privateSingleplayerAdventure()
                .channelId(null);

        // Then
        assertThrows(BusinessRuleViolationException.class, adventureBuilder::build);
    }

    @Test
    public void createPersona_whenNullGameMode_thenThrowException() {

        // Given
        Adventure.Builder adventureBuilder = AdventureFixture.privateSingleplayerAdventure().gameMode(null);

        // Then
        assertThrows(BusinessRuleViolationException.class, adventureBuilder::build);
    }

    @Test
    public void updateWorldDescription() {

        // Given
        Adventure adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // When
        adventure.updateDescription("New Description");

        // Then
        assertThat(adventure.getDescription()).isEqualTo("New Description");
    }

    @Test
    public void updateWorldInitialPrompt() {

        // Given
        Adventure adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // When
        adventure.updateAdventureStart("New Prompt");

        // Then
        assertThat(adventure.getAdventureStart()).isEqualTo("New Prompt");
    }

    @Test
    public void adventure_whenUpdateWorldId_thenWorldIdIsUpdated() {

        // Given
        String newWorldId = "12345";
        Adventure adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // When
        adventure.updateWorld(newWorldId);

        // Then
        assertThat(adventure.getWorldId()).isEqualTo(newWorldId);
    }

    @Test
    public void adventure_whenUpdateDiscordChannelId_thenDiscordChannelIdIsUpdated() {

        // Given
        String newDiscordChannelId = "12345";
        Adventure adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // When
        adventure.updateChannel(newDiscordChannelId);

        // Then
        assertThat(adventure.getChannelId()).isEqualTo(newDiscordChannelId);
    }

    @Test
    public void adventure_whenUpdateGameMode_thenGameModeIsUpdated() {

        // Given
        GameMode newGameMode = GameMode.AUTHOR;
        Adventure adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // When
        adventure.updateGameMode(newGameMode);

        // Then
        assertThat(adventure.getGameMode()).isEqualTo(newGameMode);
    }

    @Test
    public void adventure_whenMultiplayerAdventure_thenChangeToSingleplayer() {

        // Given
        Adventure adventure = AdventureFixture.privateMultiplayerAdventure().build();

        // When
        adventure.makeSinglePlayer();

        // Then
        assertThat(adventure.isMultiplayer()).isFalse();
    }

    @Test
    public void adventure_whenSingleplayerAdventure_thenChangeToMultiplayer() {

        // Given
        Adventure adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // When
        adventure.makeMultiplayer();

        // Then
        assertThat(adventure.isMultiplayer()).isTrue();
    }

    @Test
    public void adventure_whenUpdateNudge_thenNudgeIsUpdated() {

        // Given
        String newNudge = "This is the new value";
        Adventure adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ContextAttributes originalContextAttributes = adventure.getContextAttributes();

        // When
        adventure.updateNudge(newNudge);

        // Then
        assertThat(adventure.getContextAttributes()).isNotEqualTo(originalContextAttributes);
        assertThat(adventure.getContextAttributes().getNudge()).isEqualTo(newNudge);
    }

    @Test
    public void adventure_whenUpdateRemember_thenRememberIsUpdated() {

        // Given
        String newRemember = "This is the new value";
        Adventure adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ContextAttributes originalContextAttributes = adventure.getContextAttributes();

        // When
        adventure.updateRemember(newRemember);

        // Then
        assertThat(adventure.getContextAttributes()).isNotEqualTo(originalContextAttributes);
        assertThat(adventure.getContextAttributes().getRemember()).isEqualTo(newRemember);
    }

    @Test
    public void adventure_whenUpdateBump_thenBumpIsUpdated() {

        // Given
        String newBump = "This is the new value";
        Adventure adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ContextAttributes originalContextAttributes = adventure.getContextAttributes();

        // When
        adventure.updateBump(newBump);

        // Then
        assertThat(adventure.getContextAttributes()).isNotEqualTo(originalContextAttributes);
        assertThat(adventure.getContextAttributes().getBump()).isEqualTo(newBump);
    }

    @Test
    public void adventure_whenUpdateBumpFrequency_thenBumpFrequencyIsUpdated() {

        // Given
        int newBumpFrequency = 5;
        Adventure adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ContextAttributes originalContextAttributes = adventure.getContextAttributes();

        // When
        adventure.updateBumpFrequency(newBumpFrequency);

        // Then
        assertThat(adventure.getContextAttributes()).isNotEqualTo(originalContextAttributes);
        assertThat(adventure.getContextAttributes().getBumpFrequency()).isEqualTo(newBumpFrequency);
    }

    @Test
    public void adventure_whenUpdateAuthorsNote_thenAuthorsNoteIsUpdated() {

        // Given
        String newAuthorsNote = "This is the new value";
        Adventure adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ContextAttributes originalContextAttributes = adventure.getContextAttributes();

        // When
        adventure.updateAuthorsNote(newAuthorsNote);

        // Then
        assertThat(adventure.getContextAttributes()).isNotEqualTo(originalContextAttributes);
        assertThat(adventure.getContextAttributes().getAuthorsNote()).isEqualTo(newAuthorsNote);
    }
}
