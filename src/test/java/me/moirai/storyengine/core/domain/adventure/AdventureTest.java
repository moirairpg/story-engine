package me.moirai.storyengine.core.domain.adventure;

import static me.moirai.storyengine.common.enums.Visibility.PRIVATE;
import static me.moirai.storyengine.common.enums.Visibility.PUBLIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.enums.ArtificialIntelligenceModel;
import me.moirai.storyengine.common.enums.Moderation;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;

public class AdventureTest {

    @Test
    public void createAdventure_whenValidData_thenCreateAdventure() {

        // given
        var adventureBuilder = Adventure.builder();
        var worldId = UUID.randomUUID();
        adventureBuilder.name("Name");
        adventureBuilder.worldId(worldId);
        adventureBuilder.narrator("Aria", "A helpful guide");
        adventureBuilder.moderation(Moderation.STRICT);
        adventureBuilder.visibility(Visibility.fromString("PRIVATE"));
        adventureBuilder.modelConfiguration(ModelConfigurationFixture.gpt4Mini());

        // when
        var adventure = adventureBuilder.build();

        // then
        assertThat(adventure).isNotNull();
        assertThat(adventure.getName()).isEqualTo("Name");
        assertThat(adventure.getWorldId()).isEqualTo(worldId);
        assertThat(adventure.getNarratorName()).isEqualTo("Aria");
        assertThat(adventure.getModeration()).isEqualTo(Moderation.STRICT);
        assertThat(adventure.getVisibility()).isEqualTo(PRIVATE);
    }

    @Test
    public void createAdventure_whenNameIsNull_thenThrowException() {

        // given
        var adventureBuilder = AdventureFixture.privateSingleplayerAdventure().name(null);

        // then
        assertThrows(BusinessRuleViolationException.class, adventureBuilder::build);
    }

    @Test
    public void createAdventure_whenNameIsEmpty_thenThrowException() {

        // given
        var adventureBuilder = AdventureFixture.privateSingleplayerAdventure().name(StringUtils.EMPTY);

        // then
        assertThrows(BusinessRuleViolationException.class, adventureBuilder::build);
    }

    @Test
    public void createAdventure_whenModelConfigurationIsNull_thenThrowException() {

        // given
        var adventureBuilder = AdventureFixture.privateSingleplayerAdventure().modelConfiguration(null);

        // then
        assertThrows(BusinessRuleViolationException.class, adventureBuilder::build);
    }

    @Test
    public void createAdventure_whenModerationIsNull_thenThrowException() {

        // given
        var adventureBuilder = AdventureFixture.privateSingleplayerAdventure().moderation(null);

        // then
        assertThrows(BusinessRuleViolationException.class, adventureBuilder::build);
    }

    @Test
    public void createAdventure_whenVisibilityIsNull_thenThrowException() {

        // given
        var adventureBuilder = AdventureFixture.privateSingleplayerAdventure().visibility(null);

        // then
        assertThrows(BusinessRuleViolationException.class, adventureBuilder::build);
    }

    @Test
    public void grantWritePermission_thenUserCanWriteAndRead() {

        // given
        var userId = 1234567890L;
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        adventure.permissions().add(new Permission(9999L, PermissionLevel.OWNER));

        // when
        adventure.grant(new Permission(userId, PermissionLevel.WRITE));

        // then
        assertThat(adventure.canWrite(userId)).isTrue();
        assertThat(adventure.canRead(userId)).isTrue();
    }

    @Test
    public void grantReadPermission_thenUserCanOnlyRead() {

        // given
        var userId = 1234567890L;
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        adventure.permissions().add(new Permission(9999L, PermissionLevel.OWNER));

        // when
        adventure.grant(new Permission(userId, PermissionLevel.READ));

        // then
        assertThat(adventure.canWrite(userId)).isFalse();
        assertThat(adventure.canRead(userId)).isTrue();
    }

    @Test
    public void revokeReadPermission_thenUserCannotRead() {

        // given
        var userId = 1234567890L;
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        adventure.permissions().add(new Permission(9999L, PermissionLevel.OWNER));
        adventure.grant(new Permission(userId, PermissionLevel.READ));

        // when
        adventure.revoke(userId);

        // then
        assertThat(adventure.canWrite(userId)).isFalse();
        assertThat(adventure.canRead(userId)).isFalse();
    }

    @Test
    public void revokeWritePermission_thenUserCannotWrite() {

        // given
        var userId = 1234567890L;
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        adventure.permissions().add(new Permission(9999L, PermissionLevel.OWNER));
        adventure.grant(new Permission(userId, PermissionLevel.WRITE));

        // when
        adventure.revoke(userId);

        // then
        assertThat(adventure.canWrite(userId)).isFalse();
        assertThat(adventure.canRead(userId)).isFalse();
    }

    @Test
    public void updateAdventure_whenTurningPrivateIntoPublic_thenPermissionShouldBeChangedToPublic() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // when
        adventure.updateVisibility(PUBLIC);

        // then
        assertThat(adventure.isPublic()).isTrue();
    }

    @Test
    public void updateAdventure_whenTurningPublicIntoPrivate_thenPermissionShouldBeChangedToPrivate() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // when
        adventure.updateVisibility(PRIVATE);

        // then
        assertThat(adventure.isPublic()).isFalse();
    }

    @Test
    public void updateAdventure_whenNewNameProvided_thenNameShouldBeUpdated() {

        // given
        var name = "New Name";
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // when
        adventure.updateName(name);

        // then
        assertThat(adventure.getName()).isEqualTo(name);
    }

    @Test
    public void updateAdventure_whenNarratorPersonalitySetButNameNull_thenNameDefaultsToNarrator() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure()
                .narrator(null, "Some personality")
                .build();

        // when / then
        assertThat(adventure.getNarratorName()).isEqualTo("Narrator");
        assertThat(adventure.getNarratorPersonality()).isEqualTo("Some personality");
    }

    @Test
    public void updateAdventure_whenNewModerationProvided_thenModerationShouldBeUpdated() {

        // given
        var moderation = Moderation.DISABLED;
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // when
        adventure.updateModeration(moderation);

        // then
        assertThat(adventure.getModeration()).isEqualTo(moderation);
    }

    @Test
    public void updateAdventure_whenNewAiModelIsProvided_thenAiModelShouldBeUpdated() {

        // given
        var aiModel = ArtificialIntelligenceModel.GPT54;
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // when
        adventure.updateAiModel(aiModel);

        // then
        assertThat(adventure.getModelConfiguration().getAiModel()).isEqualTo(aiModel);
    }

    @Test
    public void updateAdventure_whenNewMaxTokenLimit_thenMaxTokenLimitShouldBeUpdated() {

        // given
        var maxTokenLimit = 100;
        var adventure = AdventureFixture.privateSingleplayerAdventure()
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .build();

        // when
        adventure.updateMaxTokenLimit(maxTokenLimit);

        // then
        assertThat(adventure.getModelConfiguration().getMaxTokenLimit()).isEqualTo(maxTokenLimit);
    }

    @Test
    public void updateAdventure_whenNewMaxTokenLimitGreaterThanAllowed_thenThrowException() {

        // given
        var maxTokenLimit = 500000;
        var adventure = AdventureFixture.privateSingleplayerAdventure()
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .build();

        // then
        assertThrows(BusinessRuleViolationException.class,
                () -> adventure.updateMaxTokenLimit(maxTokenLimit));
    }

    @Test
    public void updateAdventure_whenNewMaxTokenLimitLesserThanAllowed_thenThrowException() {

        // given
        var maxTokenLimit = 10;
        var adventure = AdventureFixture.privateSingleplayerAdventure()
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .build();

        // then
        assertThrows(BusinessRuleViolationException.class,
                () -> adventure.updateMaxTokenLimit(maxTokenLimit));
    }

    @Test
    public void updateAdventure_whenNewTemperature_thenTemperatureShouldBeUpdated() {

        // given
        var temperature = 1.3;
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // when
        adventure.updateTemperature(temperature);

        // then
        assertThat(adventure.getModelConfiguration().getTemperature()).isEqualTo(temperature);
    }

    @Test
    public void updateAdventure_whenNewTemperatureGreaterThanAllowed_thenThrowException() {

        // given
        var temperature = 3.0;
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // then
        assertThrows(BusinessRuleViolationException.class,
                () -> adventure.updateTemperature(temperature));
    }

    @Test
    public void updateAdventure_whenNewTemperatureLesserThanAllowed_thenThrowException() {

        // given
        var temperature = 0.0;
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // then
        assertThrows(BusinessRuleViolationException.class,
                () -> adventure.updateTemperature(temperature));
    }

    @Test
    public void updateWorldDescription() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // when
        adventure.updateDescription("New Description");

        // then
        assertThat(adventure.getDescription()).isEqualTo("New Description");
    }

    @Test
    public void updateWorldInitialPrompt() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // when
        adventure.updateAdventureStart("New Prompt");

        // then
        assertThat(adventure.getAdventureStart()).isEqualTo("New Prompt");
    }

    @Test
    public void adventure_whenMultiplayerAdventure_thenChangeToSingleplayer() {

        // given
        var adventure = AdventureFixture.privateMultiplayerAdventure().build();

        // when
        adventure.makeSinglePlayer();

        // then
        assertThat(adventure.isMultiplayer()).isFalse();
    }

    @Test
    public void adventure_whenSingleplayerAdventure_thenChangeToMultiplayer() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();

        // when
        adventure.makeMultiplayer();

        // then
        assertThat(adventure.isMultiplayer()).isTrue();
    }

    @Test
    public void adventure_whenUpdateNudge_thenNudgeIsUpdated() {

        // given
        var newNudge = "This is the new value";
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        var originalContextAttributes = adventure.getContextAttributes();

        // when
        adventure.updateNudge(newNudge);

        // then
        assertThat(adventure.getContextAttributes()).isNotEqualTo(originalContextAttributes);
        assertThat(adventure.getContextAttributes().nudge()).isEqualTo(newNudge);
    }

    @Test
    public void adventure_whenUpdateScene_thenSceneIsUpdated() {

        // given
        var newScene = "This is the new value";
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        var originalContextAttributes = adventure.getContextAttributes();

        // when
        adventure.updateScene(newScene);

        // then
        assertThat(adventure.getContextAttributes()).isNotEqualTo(originalContextAttributes);
        assertThat(adventure.getContextAttributes().scene()).isEqualTo(newScene);
    }

    @Test
    public void adventure_whenUpdateBump_thenBumpIsUpdated() {

        // given
        var newBump = "This is the new value";
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        var originalContextAttributes = adventure.getContextAttributes();

        // when
        adventure.updateBump(newBump);

        // then
        assertThat(adventure.getContextAttributes()).isNotEqualTo(originalContextAttributes);
        assertThat(adventure.getContextAttributes().bump()).isEqualTo(newBump);
    }

    @Test
    public void adventure_whenUpdateBumpFrequency_thenBumpFrequencyIsUpdated() {

        // given
        var newBumpFrequency = 5;
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        var originalContextAttributes = adventure.getContextAttributes();

        // when
        adventure.updateBumpFrequency(newBumpFrequency);

        // then
        assertThat(adventure.getContextAttributes()).isNotEqualTo(originalContextAttributes);
        assertThat(adventure.getContextAttributes().bumpFrequency()).isEqualTo(newBumpFrequency);
    }

    @Test
    public void adventure_whenUpdateAuthorsNote_thenAuthorsNoteIsUpdated() {

        // given
        var newAuthorsNote = "This is the new value";
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        var originalContextAttributes = adventure.getContextAttributes();

        // when
        adventure.updateAuthorsNote(newAuthorsNote);

        // then
        assertThat(adventure.getContextAttributes()).isNotEqualTo(originalContextAttributes);
        assertThat(adventure.getContextAttributes().authorsNote()).isEqualTo(newAuthorsNote);
    }
}
