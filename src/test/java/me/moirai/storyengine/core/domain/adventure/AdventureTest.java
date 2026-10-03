package me.moirai.storyengine.core.domain.adventure;

import static me.moirai.storyengine.common.enums.Visibility.PRIVATE;
import static me.moirai.storyengine.common.enums.Visibility.PUBLIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.enums.ArtificialIntelligenceModel;
import me.moirai.storyengine.common.enums.Moderation;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.common.enums.InvitationStatus;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
import me.moirai.storyengine.common.exception.NotFoundException;

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
        var adventureBuilder = AdventureFixture.privateAdventureWithoutNarrator().name(null);

        // then
        assertThrows(BusinessRuleViolationException.class, adventureBuilder::build);
    }

    @Test
    public void createAdventure_whenNameIsEmpty_thenThrowException() {

        // given
        var adventureBuilder = AdventureFixture.privateAdventureWithoutNarrator().name(StringUtils.EMPTY);

        // then
        assertThrows(BusinessRuleViolationException.class, adventureBuilder::build);
    }

    @Test
    public void createAdventure_whenModelConfigurationIsNull_thenThrowException() {

        // given
        var adventureBuilder = AdventureFixture.privateAdventureWithoutNarrator().modelConfiguration(null);

        // then
        assertThrows(BusinessRuleViolationException.class, adventureBuilder::build);
    }

    @Test
    public void createAdventure_whenModerationIsNull_thenThrowException() {

        // given
        var adventureBuilder = AdventureFixture.privateAdventureWithoutNarrator().moderation(null);

        // then
        assertThrows(BusinessRuleViolationException.class, adventureBuilder::build);
    }

    @Test
    public void createAdventure_whenVisibilityIsNull_thenThrowException() {

        // given
        var adventureBuilder = AdventureFixture.privateAdventureWithoutNarrator().visibility(null);

        // then
        assertThrows(BusinessRuleViolationException.class, adventureBuilder::build);
    }

    @Test
    public void grantWritePermission_thenUserCanWriteAndRead() {

        // given
        var userId = 1234567890L;
        var adventure = AdventureFixture.privateAdventureWithoutNarrator().build();
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
        var adventure = AdventureFixture.privateAdventureWithoutNarrator().build();
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
        var adventure = AdventureFixture.privateAdventureWithoutNarrator().build();
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
        var adventure = AdventureFixture.privateAdventureWithoutNarrator().build();
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
        var adventure = AdventureFixture.privateAdventureWithoutNarrator().build();

        // when
        adventure.updateVisibility(PUBLIC);

        // then
        assertThat(adventure.isPublic()).isTrue();
    }

    @Test
    public void updateAdventure_whenTurningPublicIntoPrivate_thenPermissionShouldBeChangedToPrivate() {

        // given
        var adventure = AdventureFixture.privateAdventureWithoutNarrator().build();

        // when
        adventure.updateVisibility(PRIVATE);

        // then
        assertThat(adventure.isPublic()).isFalse();
    }

    @Test
    public void updateAdventure_whenNewNameProvided_thenNameShouldBeUpdated() {

        // given
        var name = "New Name";
        var adventure = AdventureFixture.privateAdventureWithoutNarrator().build();

        // when
        adventure.updateName(name);

        // then
        assertThat(adventure.getName()).isEqualTo(name);
    }

    @Test
    public void updateAdventure_whenNarratorPersonalitySetButNameNull_thenNameDefaultsToNarrator() {

        // given
        var adventure = AdventureFixture.privateAdventureWithoutNarrator()
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
        var adventure = AdventureFixture.privateAdventureWithoutNarrator().build();

        // when
        adventure.updateModeration(moderation);

        // then
        assertThat(adventure.getModeration()).isEqualTo(moderation);
    }

    @Test
    public void shouldReplaceEveryModelFieldWhenTheModelConfigurationIsUpdated() {

        // given
        var adventure = AdventureFixture.privateAdventureWithoutNarrator()
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .build();

        // when
        adventure.updateModelConfiguration(ArtificialIntelligenceModel.GPT54, 100000, 1.3);

        // then
        assertThat(adventure.getModelConfiguration().getAiModel())
                .isEqualTo(ArtificialIntelligenceModel.GPT54);
        assertThat(adventure.getModelConfiguration().getMaxTokenLimit()).isEqualTo(100000);
        assertThat(adventure.getModelConfiguration().getTemperature()).isEqualTo(1.3);
    }

    @Test
    public void shouldAllowSwitchingToAModelWithASmallerCapWhenTheLimitIsLoweredInTheSameUpdate() {

        // given
        var adventure = AdventureFixture.privateAdventureWithoutNarrator()
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .build();

        adventure.updateModelConfiguration(ArtificialIntelligenceModel.GPT54, 100000, 1.0);

        // when
        adventure.updateModelConfiguration(ArtificialIntelligenceModel.GPT54_MINI, 40000, 1.0);

        // then
        assertThat(adventure.getModelConfiguration().getAiModel())
                .isEqualTo(ArtificialIntelligenceModel.GPT54_MINI);
        assertThat(adventure.getModelConfiguration().getMaxTokenLimit()).isEqualTo(40000);
    }

    @Test
    public void shouldAllowSwitchingToAModelWithALargerCapWhenTheLimitIsRaisedInTheSameUpdate() {

        // given
        var adventure = AdventureFixture.privateAdventureWithoutNarrator()
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .build();

        adventure.updateModelConfiguration(ArtificialIntelligenceModel.GPT54_MINI, 40000, 1.0);

        // when
        adventure.updateModelConfiguration(ArtificialIntelligenceModel.GPT54, 100000, 1.0);

        // then
        assertThat(adventure.getModelConfiguration().getAiModel())
                .isEqualTo(ArtificialIntelligenceModel.GPT54);
        assertThat(adventure.getModelConfiguration().getMaxTokenLimit()).isEqualTo(100000);
    }

    @Test
    public void shouldThrowExceptionWhenTheTokenLimitExceedsTheModelCap() {

        // given
        var adventure = AdventureFixture.privateAdventureWithoutNarrator()
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .build();

        // then
        assertThrows(BusinessRuleViolationException.class,
                () -> adventure.updateModelConfiguration(ArtificialIntelligenceModel.GPT54_MINI, 500000, 1.0));
    }

    @Test
    public void shouldThrowExceptionWhenTheTokenLimitIsBelowTheMinimum() {

        // given
        var adventure = AdventureFixture.privateAdventureWithoutNarrator()
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .build();

        // then
        assertThrows(BusinessRuleViolationException.class,
                () -> adventure.updateModelConfiguration(ArtificialIntelligenceModel.GPT54_MINI, 10, 1.0));
    }

    @Test
    public void shouldThrowExceptionWhenTheTemperatureIsOutOfRange() {

        // given
        var adventure = AdventureFixture.privateAdventureWithoutNarrator()
                .modelConfiguration(ModelConfigurationFixture.gpt4Mini())
                .build();

        // then
        assertThrows(BusinessRuleViolationException.class,
                () -> adventure.updateModelConfiguration(ArtificialIntelligenceModel.GPT54_MINI, 1000, 3.0));

        assertThrows(BusinessRuleViolationException.class,
                () -> adventure.updateModelConfiguration(ArtificialIntelligenceModel.GPT54_MINI, 1000, 0.0));
    }

    @Test
    public void updateWorldDescription() {

        // given
        var adventure = AdventureFixture.privateAdventureWithoutNarrator().build();

        // when
        adventure.updateDescription("New Description");

        // then
        assertThat(adventure.getDescription()).isEqualTo("New Description");
    }

    @Test
    public void updateWorldInitialPrompt() {

        // given
        var adventure = AdventureFixture.privateAdventureWithoutNarrator().build();

        // when
        adventure.updateAdventureStart("New Prompt");

        // then
        assertThat(adventure.getAdventureStart()).isEqualTo("New Prompt");
    }

    @Test
    public void adventure_whenUpdateNudge_thenNudgeIsUpdated() {

        // given
        var newNudge = "This is the new value";
        var adventure = AdventureFixture.privateAdventureWithoutNarrator().build();
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
        var adventure = AdventureFixture.privateAdventureWithoutNarrator().build();
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
        var adventure = AdventureFixture.privateAdventureWithoutNarrator().build();
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
        var adventure = AdventureFixture.privateAdventureWithoutNarrator().build();
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
        var adventure = AdventureFixture.privateAdventureWithoutNarrator().build();
        var originalContextAttributes = adventure.getContextAttributes();

        // when
        adventure.updateAuthorsNote(newAuthorsNote);

        // then
        assertThat(adventure.getContextAttributes()).isNotEqualTo(originalContextAttributes);
        assertThat(adventure.getContextAttributes().authorsNote()).isEqualTo(newAuthorsNote);
    }

    @Test
    public void shouldEnrollPlayerCharacterWhenRosterHasSpace() {

        // given
        var adventure = AdventureFixture.privateAdventureWithId();

        // when
        adventure.enrollPlayerCharacter(1L, 10L);

        // then
        assertThat(adventure.getRoster())
                .extracting(AdventureMembership::getPlayerCharacterId)
                .containsExactly(1L);
    }

    @Test
    public void shouldThrowExceptionWhenRosterIsFull() {

        // given
        var adventure = AdventureFixture.privateAdventureWithId();

        for (var playerCharacterId = 1L; playerCharacterId <= Adventure.MAX_ROSTER_SIZE; playerCharacterId++) {
            adventure.enrollPlayerCharacter(playerCharacterId, playerCharacterId + 100L);
        }

        // then
        assertThrows(BusinessRuleViolationException.class, () -> adventure.enrollPlayerCharacter(99L, 999L));
    }

    @Test
    public void shouldThrowExceptionWhenCharacterIsAlreadyEnrolled() {

        // given
        var adventure = AdventureFixture.privateAdventureWithId();
        adventure.enrollPlayerCharacter(1L, 10L);

        // then
        assertThrows(BusinessRuleViolationException.class, () -> adventure.enrollPlayerCharacter(1L, 20L));
    }

    @Test
    public void shouldThrowExceptionWhenPlayerAlreadyHasACharacterEnrolled() {

        // given
        var adventure = AdventureFixture.privateAdventureWithId();
        adventure.enrollPlayerCharacter(1L, 10L);

        // then
        assertThrows(BusinessRuleViolationException.class, () -> adventure.enrollPlayerCharacter(2L, 10L));
    }

    @Test
    public void shouldThrowExceptionWhenEnrollingIntoAnUnpersistedAdventure() {

        // given
        var adventure = AdventureFixture.privateAdventure().build();

        // then
        assertThrows(BusinessRuleViolationException.class, () -> adventure.enrollPlayerCharacter(1L, 10L));
    }

    @Test
    public void shouldRemoveOnlyTheGivenCharacterWhenLeaving() {

        // given
        var adventure = AdventureFixture.privateAdventureWithId();
        adventure.enrollPlayerCharacter(1L, 10L);
        adventure.enrollPlayerCharacter(2L, 20L);

        // when
        adventure.leave(1L);

        // then
        assertThat(adventure.getRoster())
                .extracting(AdventureMembership::getPlayerCharacterId)
                .containsExactly(2L);
    }

    @Test
    public void shouldThrowWhenLeavingWithACharacterThatIsNotEnrolled() {

        // given
        var adventure = AdventureFixture.privateAdventureWithId();
        adventure.enrollPlayerCharacter(1L, 10L);

        // when
        var exception = assertThrows(NotFoundException.class, () -> adventure.leave(99L));

        // then
        assertThat(exception.getMessage()).isEqualTo("Character is not enrolled in this adventure");
        assertThat(adventure.getRoster())
                .extracting(AdventureMembership::getPlayerCharacterId)
                .containsExactly(1L);
    }

    @Test
    public void shouldNotRemoveAnotherCharacterWhenTheGivenOneIsOwnedByTheSamePlayerButNotEnrolled() {

        // given
        var adventure = AdventureFixture.privateAdventureWithId();
        adventure.enrollPlayerCharacter(1L, 10L);

        // when
        assertThrows(NotFoundException.class, () -> adventure.leave(2L));

        // then
        assertThat(adventure.getRoster())
                .extracting(AdventureMembership::getPlayerCharacterId)
                .containsExactly(1L);
    }

    @Test
    public void shouldEmitPlayerLeftEventWhenLeaving() {

        // given
        var adventure = AdventureFixture.privateAdventureWithId();
        adventure.enrollPlayerCharacter(1L, 10L);
        adventure.drainEvents();

        // when
        adventure.leave(1L);

        // then
        var event = drainEvent(adventure, PlayerLeftAdventureEvent.class);
        assertThat(event.getPlayerId()).isEqualTo(10L);
        assertThat(event.getPlayerCharacterId()).isEqualTo(1L);
    }

    @Test
    public void shouldEmitPlayerExpelledEventWhenExpelling() {

        // given
        var adventure = AdventureFixture.privateAdventureWithId();
        adventure.enrollPlayerCharacter(1L, 10L);
        adventure.drainEvents();

        // when
        adventure.expel(1L);

        // then
        var event = drainEvent(adventure, PlayerExpelledFromAdventureEvent.class);
        assertThat(event.getPlayerId()).isEqualTo(10L);
        assertThat(event.getPlayerCharacterId()).isEqualTo(1L);
    }

    @Test
    public void shouldEmitEnrolledCharacterDeletedEventWhenWithdrawingADeletedCharacter() {

        // given
        var adventure = AdventureFixture.privateAdventureWithId();
        adventure.enrollPlayerCharacter(1L, 10L);
        adventure.drainEvents();

        // when
        adventure.withdrawDeletedCharacter(1L);

        // then
        var event = drainEvent(adventure, EnrolledCharacterDeletedEvent.class);
        assertThat(event.getPlayerId()).isEqualTo(10L);
        assertThat(event.getPlayerCharacterId()).isEqualTo(1L);
    }

    @Test
    public void shouldNotEmitEventWhenTheCharacterIsNotEnrolled() {

        // given
        var adventure = AdventureFixture.privateAdventureWithId();
        adventure.enrollPlayerCharacter(1L, 10L);
        adventure.drainEvents();

        // when
        assertThrows(NotFoundException.class, () -> adventure.leave(99L));

        // then
        assertThat(adventure.drainEvents()).isEmpty();
    }

    @Test
    public void shouldRevokeReadAccessWhenAnEnrolledReaderLeaves() {

        // given
        var adventure = AdventureFixture.privateAdventureWithId();
        var invitation = adventure.invite(10L, AdventureFixture.OWNER_ID);
        adventure.acceptInvitation(invitation.getPublicId(), 1L, 10L);
        adventure.drainEvents();

        // when
        adventure.leave(1L);

        // then
        assertThat(adventure.canRead(10L)).isFalse();
    }

    @Test
    public void shouldKeepWriteAccessWhenAWriterLeaves() {

        // given
        var adventure = AdventureFixture.privateAdventureWithId();
        adventure.grant(new Permission(10L, PermissionLevel.WRITE));
        adventure.enrollPlayerCharacter(1L, 10L);
        adventure.drainEvents();

        // when
        adventure.leave(1L);

        // then
        assertThat(adventure.canWrite(10L)).isTrue();
    }

    private <T> T drainEvent(Adventure adventure, Class<T> eventType) {

        return adventure.drainEvents().stream()
                .filter(eventType::isInstance)
                .map(eventType::cast)
                .findFirst()
                .orElseThrow();
    }

    @Test
    public void shouldReturnTrueWhenCharacterIsEnrolled() {

        // given
        var adventure = AdventureFixture.privateAdventureWithId();
        adventure.enrollPlayerCharacter(1L, 10L);

        // then
        assertThat(adventure.hasCharacter(1L)).isTrue();
    }

    @Test
    public void shouldReturnFalseWhenCharacterIsNotEnrolled() {

        // given
        var adventure = AdventureFixture.privateAdventureWithId();
        adventure.enrollPlayerCharacter(1L, 10L);

        // then
        assertThat(adventure.hasCharacter(2L)).isFalse();
    }

    @Test
    public void shouldReturnTrueWhenPlayerHasACharacterEnrolled() {

        // given
        var adventure = AdventureFixture.privateAdventureWithId();
        adventure.enrollPlayerCharacter(1L, 10L);

        // then
        assertThat(adventure.hasPlayer(10L)).isTrue();
    }

    @Test
    public void shouldReturnFalseWhenPlayerHasNoCharacterEnrolled() {

        // given
        var adventure = AdventureFixture.privateAdventureWithId();
        adventure.enrollPlayerCharacter(1L, 10L);

        // then
        assertThat(adventure.hasPlayer(20L)).isFalse();
    }

    @Test
    public void shouldInviteUserAndRaiseEvent() {

        // given
        var adventure = AdventureFixture.privateAdventureWithId();

        // when
        var invitation = adventure.invite(10L, AdventureFixture.OWNER_ID);

        // then
        assertThat(adventure.getInvitations()).hasSize(1);
        assertThat(invitation.getUserId()).isEqualTo(10L);
        assertThat(invitation.isPending()).isTrue();
        assertThat(adventure.drainEvents())
                .anyMatch(UserInvitedToAdventureEvent.class::isInstance);
    }

    @Test
    public void shouldThrowWhenInvitingAUserWithAPendingInvitation() {

        // given
        var adventure = AdventureFixture.privateAdventureWithId();
        adventure.invite(10L, AdventureFixture.OWNER_ID);

        // then
        assertThrows(BusinessRuleViolationException.class, () -> adventure.invite(10L, AdventureFixture.OWNER_ID));
    }

    @Test
    public void shouldAllowReinvitingAfterDecline() {

        // given
        var adventure = AdventureFixture.privateAdventureWithId();
        var invitation = adventure.invite(10L, AdventureFixture.OWNER_ID);
        adventure.declineInvitation(invitation.getPublicId());

        // when
        var reinvitation = adventure.invite(10L, AdventureFixture.OWNER_ID);

        // then
        assertThat(reinvitation.isPending()).isTrue();
    }

    @Test
    public void shouldAcceptInvitationEnrollTheCharacterAndRaiseAnsweredEvent() {

        // given
        var adventure = AdventureFixture.privateAdventureWithId();
        var invitation = adventure.invite(10L, AdventureFixture.OWNER_ID);
        adventure.drainEvents();

        // when
        adventure.acceptInvitation(invitation.getPublicId(), 1L, 10L);

        // then
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
        assertThat(adventure.hasCharacter(1L)).isTrue();
        assertThat(adventure.canRead(10L)).isTrue();
        assertThat(adventure.drainEvents())
                .anyMatch(AdventureInvitationAnsweredEvent.class::isInstance);
    }

    @Test
    public void shouldNotDowngradeAnExistingWriterWhenAcceptingAnInvitation() {

        // given
        var adventure = AdventureFixture.privateAdventureWithId();
        adventure.grant(new Permission(10L, PermissionLevel.WRITE));
        var invitation = adventure.invite(10L, AdventureFixture.OWNER_ID);
        adventure.drainEvents();

        // when
        adventure.acceptInvitation(invitation.getPublicId(), 1L, 10L);

        // then
        assertThat(adventure.canWrite(10L)).isTrue();
    }

    @Test
    public void shouldDeclineInvitationWithoutEnrollingAndRaiseAnsweredEvent() {

        // given
        var adventure = AdventureFixture.privateAdventureWithId();
        var invitation = adventure.invite(10L, AdventureFixture.OWNER_ID);
        adventure.drainEvents();

        // when
        adventure.declineInvitation(invitation.getPublicId());

        // then
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.DECLINED);
        assertThat(adventure.getRoster()).isEmpty();
        assertThat(adventure.drainEvents())
                .anyMatch(AdventureInvitationAnsweredEvent.class::isInstance);
    }

    @Test
    public void shouldThrowWhenAnsweringAnUnknownInvitation() {

        // given
        var adventure = AdventureFixture.privateAdventureWithId();

        // then
        assertThrows(NotFoundException.class,
                () -> adventure.declineInvitation(java.util.UUID.randomUUID()));
    }

    @Test
    public void shouldNotTouchInvitationWhenRosterIsFullOnAccept() {

        // given
        var adventure = AdventureFixture.privateAdventureWithId();
        for (var i = 1L; i <= Adventure.MAX_ROSTER_SIZE; i++) {
            adventure.enrollPlayerCharacter(i, i + 100L);
        }
        var invitation = adventure.invite(10L, AdventureFixture.OWNER_ID);
        adventure.drainEvents();

        // then
        assertThrows(BusinessRuleViolationException.class,
                () -> adventure.acceptInvitation(invitation.getPublicId(), 99L, 10L));
        assertThat(invitation.isPending()).isTrue();
        assertThat(adventure.drainEvents()).isEmpty();
    }

    @Test
    public void shouldResolveThePersonaNamePlaceholderWhenReadingTheNarratorPersonality() {

        // given
        var adventure = AdventureFixture.privateAdventure()
                .narrator("Aria", "{name} guides the story")
                .build();

        // when
        var personality = adventure.getNarratorPersonality();

        // then
        assertThat(personality).isEqualTo("Aria guides the story");
    }

    @Test
    public void shouldReturnTheStoredTextWhenReadingTheNarratorPersonalityTemplate() {

        // given
        var adventure = AdventureFixture.privateAdventure()
                .narrator("Aria", "{name} guides the story")
                .build();

        // when
        var template = adventure.getNarratorPersonalityTemplate();

        // then
        assertThat(template).isEqualTo("{name} guides the story");
    }

    @Test
    public void shouldReturnNullWhenReadingTheNarratorPersonalityOfAnAdventureWithoutANarrator() {

        // given
        var adventure = AdventureFixture.privateAdventureWithoutNarrator().build();

        // when / then
        assertThat(adventure.getNarratorPersonality()).isNull();
        assertThat(adventure.getNarratorPersonalityTemplate()).isNull();
    }

    @Test
    public void shouldReturnOnlyTheRequestedEntriesWhenSelectingLorebookEntriesByIds() {

        // given
        var adventure = AdventureFixture.privateAdventure().build();
        var entry = adventure.addLorebookEntry("Winterhold", "A ruined city");

        adventure.addLorebookEntry("Whiterun", "A walled hold");

        // when
        var selected = adventure.getLorebookEntriesByIds(List.of(entry.getPublicId()));

        // then
        assertThat(selected).containsExactly(entry);
    }

    @Test
    public void shouldReturnNothingWhenSelectingLorebookEntriesByUnknownIds() {

        // given
        var adventure = AdventureFixture.privateAdventure().build();

        adventure.addLorebookEntry("Winterhold", "A ruined city");

        // when
        var selected = adventure.getLorebookEntriesByIds(List.of(UUID.randomUUID()));

        // then
        assertThat(selected).isEmpty();
    }

    @Test
    public void shouldRecordTheRemovalWithTheEntryIdWhenALorebookEntryIsRemoved() {

        // given
        var adventure = AdventureFixture.privateAdventure().build();
        var entry = adventure.addLorebookEntry("Winterhold", "A ruined city");

        // when
        adventure.removeLorebookEntry(entry.getPublicId());

        // then
        assertThat(adventure.getLorebook()).isEmpty();

        var events = adventure.drainEvents();

        assertThat(events).singleElement().isInstanceOf(AdventureLorebookEntryRemovedEvent.class);
        assertThat(((AdventureLorebookEntryRemovedEvent) events.getFirst()).getEntryId())
                .isEqualTo(entry.getPublicId());
    }

    @Test
    public void shouldThrowAndRecordNothingWhenRemovingAnUnknownLorebookEntry() {

        // given
        var adventure = AdventureFixture.privateAdventure().build();
        var unknownEntryId = UUID.randomUUID();

        // then
        assertThrows(NotFoundException.class, () -> adventure.removeLorebookEntry(unknownEntryId));
        assertThat(adventure.drainEvents()).isEmpty();
    }

    @Test
    public void shouldReturnNoCharacterIdsWhenTheRosterIsEmpty() {

        // given
        var adventure = AdventureFixture.privateAdventure().build();

        // when / then
        assertThat(adventure.getEnrolledCharacterIds()).isEmpty();
    }

    @Test
    public void shouldRaiseAccessGrantedEventWhenNewMemberIsAdded() {

        // given
        var userId = 4444L;
        var adventure = AdventureFixture.privateAdventureWithId();

        // when
        adventure.updatePermissions(Set.of(new Permission(userId, PermissionLevel.WRITE)));

        // then
        var events = adventure.drainEvents();

        assertThat(events).singleElement().isInstanceOf(AdventureAccessGrantedEvent.class);

        var event = (AdventureAccessGrantedEvent) events.getFirst();

        assertThat(event.getAdventureId()).isEqualTo(AdventureFixture.NUMERIC_ID);
        assertThat(event.getAdventurePublicId()).isEqualTo(AdventureFixture.PUBLIC_ID);
        assertThat(event.getAdventureName()).isEqualTo("Name");
        assertThat(event.getUserId()).isEqualTo(userId);
        assertThat(event.getLevel()).isEqualTo(PermissionLevel.WRITE);
    }

    @Test
    public void shouldRaiseAccessLevelChangedEventWhenMemberLevelChanges() {

        // given
        var userId = 4444L;
        var adventure = AdventureFixture.privateAdventureWithId();
        adventure.permissions().add(new Permission(userId, PermissionLevel.READ));

        // when
        adventure.updatePermissions(Set.of(new Permission(userId, PermissionLevel.WRITE)));

        // then
        var events = adventure.drainEvents();

        assertThat(events).singleElement().isInstanceOf(AdventureAccessLevelChangedEvent.class);

        var event = (AdventureAccessLevelChangedEvent) events.getFirst();

        assertThat(event.getAdventureId()).isEqualTo(AdventureFixture.NUMERIC_ID);
        assertThat(event.getAdventurePublicId()).isEqualTo(AdventureFixture.PUBLIC_ID);
        assertThat(event.getAdventureName()).isEqualTo("Name");
        assertThat(event.getUserId()).isEqualTo(userId);
        assertThat(event.getLevel()).isEqualTo(PermissionLevel.WRITE);
    }

    @Test
    public void shouldRaiseAccessRevokedEventWhenMemberIsRemoved() {

        // given
        var userId = 4444L;
        var adventure = AdventureFixture.privateAdventureWithId();
        adventure.permissions().add(new Permission(userId, PermissionLevel.READ));

        // when
        adventure.updatePermissions(Set.of());

        // then
        var events = adventure.drainEvents();

        assertThat(events).singleElement().isInstanceOf(AdventureAccessRevokedEvent.class);

        var event = (AdventureAccessRevokedEvent) events.getFirst();

        assertThat(event.getAdventureId()).isEqualTo(AdventureFixture.NUMERIC_ID);
        assertThat(event.getAdventurePublicId()).isEqualTo(AdventureFixture.PUBLIC_ID);
        assertThat(event.getAdventureName()).isEqualTo("Name");
        assertThat(event.getUserId()).isEqualTo(userId);
    }

    @Test
    public void shouldRaiseNoEventWhenMemberLevelIsUnchanged() {

        // given
        var userId = 4444L;
        var adventure = AdventureFixture.privateAdventureWithId();
        adventure.permissions().add(new Permission(userId, PermissionLevel.READ));

        // when
        adventure.updatePermissions(Set.of(new Permission(userId, PermissionLevel.READ)));

        // then
        assertThat(adventure.drainEvents()).isEmpty();
    }

    @Test
    public void shouldRaiseNoEventForOwnerLevelEntryWhenPermissionsAreUpdated() {

        // given
        var userId = 4444L;
        var adventure = AdventureFixture.privateAdventureWithId();

        // when
        adventure.updatePermissions(Set.of(new Permission(userId, PermissionLevel.OWNER)));

        // then
        assertThat(adventure.drainEvents()).isEmpty();
        assertThat(adventure.canRead(userId)).isFalse();
    }

    @Test
    public void shouldRaiseASingleAccessGrantedEventAtTheWeakestLevelWhenAUserAppearsTwice() {

        // given
        var userId = 4444L;
        var adventure = AdventureFixture.privateAdventureWithId();

        // when
        adventure.updatePermissions(Set.of(
                new Permission(userId, PermissionLevel.READ),
                new Permission(userId, PermissionLevel.WRITE)));

        // then
        var events = adventure.drainEvents();

        assertThat(events).singleElement().isInstanceOf(AdventureAccessGrantedEvent.class);

        var event = (AdventureAccessGrantedEvent) events.getFirst();

        assertThat(event.getUserId()).isEqualTo(userId);
        assertThat(event.getLevel()).isEqualTo(PermissionLevel.READ);
    }

    @Test
    public void shouldRaiseNoEventWhenUpdateNamesOwner() {

        // given
        var adventure = AdventureFixture.privateAdventureWithId();
        var newPermissions = Set.of(new Permission(AdventureFixture.OWNER_ID, PermissionLevel.WRITE));

        // when
        assertThrows(BusinessRuleViolationException.class, () -> adventure.updatePermissions(newPermissions));

        // then
        assertThat(adventure.drainEvents()).isEmpty();
    }

    @Test
    public void shouldCreateAdventureWithRpgMechanicsEnabledWhenTheBuilderReceivesNoFlag() {

        // given
        var builder = AdventureFixture.privateAdventure();

        // when
        var adventure = builder.build();

        // then
        assertThat(adventure.isRpgMechanicsEnabled()).isTrue();
    }

    @Test
    public void shouldDisableRpgMechanicsWhenUpdatedWithFalse() {

        // given
        var adventure = AdventureFixture.privateAdventure().build();

        // when
        adventure.updateRpgMechanicsEnabled(false);

        // then
        assertThat(adventure.isRpgMechanicsEnabled()).isFalse();
    }

    @Test
    public void shouldEnableRpgMechanicsWhenUpdatedWithNull() {

        // given
        var adventure = AdventureFixture.privateAdventure().build();

        adventure.updateRpgMechanicsEnabled(false);

        // when
        adventure.updateRpgMechanicsEnabled(null);

        // then
        assertThat(adventure.isRpgMechanicsEnabled()).isTrue();
    }
}
