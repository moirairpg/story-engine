package me.moirai.storyengine.core.application.command.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.adventure.AdventureAccessGrantedEvent;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.port.inbound.AssetMemberInput;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventurePermissions;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UpdateAdventurePermissionsHandlerTest {

    private static final UUID ADVENTURE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID MEMBER_PUBLIC_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final Long MEMBER_ID = 4444L;

    @Mock
    private AdventureRepository repository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UpdateAdventurePermissionsHandler handler;

    @Test
    void shouldReplaceTheMemberListAndReturnTheSavedMembers() {

        // given
        var adventure = AdventureFixture.privateAdventure().build();
        var member = userWith(MEMBER_ID, MEMBER_PUBLIC_ID, "member");
        var owner = userWith(AdventureFixture.OWNER_ID, UserFixture.PUBLIC_ID, "owner");

        when(repository.findByPublicId(any())).thenReturn(Optional.of(adventure));
        when(userRepository.findByUsername("member")).thenReturn(Optional.of(member));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findAllById(any())).thenReturn(List.of(owner, member));

        var command = new UpdateAdventurePermissions(
                ADVENTURE_ID, Visibility.PRIVATE, List.of(new AssetMemberInput("member", PermissionLevel.WRITE)));

        // when
        var result = handler.execute(command);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).anyMatch(m -> m.username().equals("member") && m.level() == PermissionLevel.WRITE);
        assertThat(result).anyMatch(m -> m.level() == PermissionLevel.OWNER);
        assertThat(result).anyMatch(m -> m.username().equals("member") && m.displayName().equals("The member"));
        assertThat(result).anyMatch(m -> m.username().equals("owner") && m.displayName().equals("The owner"));
        assertThat(adventure.canWrite(MEMBER_ID)).isTrue();
    }

    @Test
    void shouldLeaveOnlyTheOwnerWhenTheListIsEmpty() {

        // given
        var adventure = AdventureFixture.privateAdventure().build();
        var owner = userWith(AdventureFixture.OWNER_ID, UserFixture.PUBLIC_ID, "owner");

        when(repository.findByPublicId(any())).thenReturn(Optional.of(adventure));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findAllById(any())).thenReturn(List.of(owner));

        var command = new UpdateAdventurePermissions(ADVENTURE_ID, Visibility.PRIVATE, List.of());

        // when
        var result = handler.execute(command);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().level()).isEqualTo(PermissionLevel.OWNER);
    }

    @Test
    void shouldThrowExceptionWhenAnEntryNamesTheOwner() {

        // given
        var adventure = AdventureFixture.privateAdventure().build();
        var owner = userWith(AdventureFixture.OWNER_ID, UserFixture.PUBLIC_ID, "owner");

        when(repository.findByPublicId(any())).thenReturn(Optional.of(adventure));
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(owner));

        var command = new UpdateAdventurePermissions(
                ADVENTURE_ID, Visibility.PRIVATE, List.of(new AssetMemberInput("owner", PermissionLevel.WRITE)));

        // when
        var thrown = assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> handler.execute(command));

        // then
        thrown.withMessage("Owner permission cannot be overwritten");
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUsernameIsUnknown() {

        // given
        var adventure = AdventureFixture.privateAdventure().build();

        when(repository.findByPublicId(any())).thenReturn(Optional.of(adventure));
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        var command = new UpdateAdventurePermissions(
                ADVENTURE_ID, Visibility.PRIVATE, List.of(new AssetMemberInput("ghost", PermissionLevel.READ)));

        // when
        var thrown = assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> handler.execute(command));

        // then
        thrown.withMessageContaining("ghost");
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenAdventureIsNotFound() {

        // given
        when(repository.findByPublicId(any())).thenReturn(Optional.empty());

        var command = new UpdateAdventurePermissions(ADVENTURE_ID, Visibility.PRIVATE, List.of());

        // when
        var thrown = assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> handler.execute(command));

        // then
        thrown.withMessage("Adventure to be shared was not found");
        verify(repository, never()).save(any());
    }

    @Test
    void shouldPublishDrainedEventsWhenPermissionsAreUpdated() {

        // given
        var adventure = AdventureFixture.privateAdventure().build();
        var member = userWith(MEMBER_ID, MEMBER_PUBLIC_ID, "member");
        var owner = userWith(AdventureFixture.OWNER_ID, UserFixture.PUBLIC_ID, "owner");

        when(repository.findByPublicId(any())).thenReturn(Optional.of(adventure));
        when(userRepository.findByUsername("member")).thenReturn(Optional.of(member));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findAllById(any())).thenReturn(List.of(owner, member));

        var command = new UpdateAdventurePermissions(
                ADVENTURE_ID, Visibility.PRIVATE, List.of(new AssetMemberInput("member", PermissionLevel.WRITE)));

        // when
        handler.execute(command);

        // then
        var publishedEvent = ArgumentCaptor.forClass(AdventureAccessGrantedEvent.class);
        verify(eventPublisher).publishEvent(publishedEvent.capture());

        assertThat(publishedEvent.getValue().getUserId()).isEqualTo(MEMBER_ID);
        assertThat(publishedEvent.getValue().getLevel()).isEqualTo(PermissionLevel.WRITE);
    }

    @Test
    void shouldPublishNoEventsWhenPermissionsAreUnchanged() {

        // given
        var adventure = AdventureFixture.privateAdventure().build();
        adventure.grant(new Permission(MEMBER_ID, PermissionLevel.READ));

        var member = userWith(MEMBER_ID, MEMBER_PUBLIC_ID, "member");
        var owner = userWith(AdventureFixture.OWNER_ID, UserFixture.PUBLIC_ID, "owner");

        when(repository.findByPublicId(any())).thenReturn(Optional.of(adventure));
        when(userRepository.findByUsername("member")).thenReturn(Optional.of(member));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findAllById(any())).thenReturn(List.of(owner, member));

        var command = new UpdateAdventurePermissions(
                ADVENTURE_ID, Visibility.PRIVATE, List.of(new AssetMemberInput("member", PermissionLevel.READ)));

        // when
        handler.execute(command);

        // then
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void shouldApplyVisibilityWhenPermissionsAreUpdated() {

        // given
        var adventure = AdventureFixture.privateAdventure().build();
        var owner = userWith(AdventureFixture.OWNER_ID, UserFixture.PUBLIC_ID, "owner");

        when(repository.findByPublicId(any())).thenReturn(Optional.of(adventure));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findAllById(any())).thenReturn(List.of(owner));

        var command = new UpdateAdventurePermissions(ADVENTURE_ID, Visibility.PUBLIC, List.of());

        // when
        handler.execute(command);

        // then
        assertThat(adventure.isPublic()).isTrue();
    }

    @Test
    void shouldDefaultVisibilityToPrivateWhenNullIsGiven() {

        // given
        var adventure = AdventureFixture.publicAdventure().build();
        var owner = userWith(AdventureFixture.OWNER_ID, UserFixture.PUBLIC_ID, "owner");

        when(repository.findByPublicId(any())).thenReturn(Optional.of(adventure));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findAllById(any())).thenReturn(List.of(owner));

        var command = new UpdateAdventurePermissions(ADVENTURE_ID, null, List.of());

        // when
        handler.execute(command);

        // then
        assertThat(adventure.isPublic()).isFalse();
    }

    private User userWith(Long id, UUID publicId, String username) {

        var user = UserFixture.player().username(username).displayName("The " + username).build();
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "publicId", publicId);

        return user;
    }
}
