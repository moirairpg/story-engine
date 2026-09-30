package me.moirai.storyengine.core.application.command.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.adventure.UserInvitedToAdventureEvent;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.port.inbound.adventure.InviteUserToAdventure;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@ExtendWith(MockitoExtension.class)
public class InviteUserToAdventureHandlerTest {

    @Mock
    private AdventureRepository adventureRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private InviteUserToAdventureHandler handler;

    private User userWith(Long id, String username) {
        var user = UserFixture.player().username(username).build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    public void shouldInviteAllResolvedUsersAndPublishOneEventEach() {

        // given
        var adventure = AdventureFixture.privateAdventureWithId();
        var command = new InviteUserToAdventure(
                adventure.getPublicId(), List.of("alice", "bob"), AdventureFixture.OWNER_ID);

        when(adventureRepository.findByPublicId(any())).thenReturn(java.util.Optional.of(adventure));
        when(userRepository.findAllByUsernameIn(anyList()))
                .thenReturn(List.of(userWith(10L, "alice"), userWith(20L, "bob")));

        // when
        var result = handler.execute(command);

        // then
        assertThat(result.invited()).containsExactly("alice", "bob");
        verify(adventureRepository).save(adventure);
        verify(eventPublisher, times(2)).publishEvent(any(UserInvitedToAdventureEvent.class));
    }

    @Test
    public void shouldInviteOnlyResolvedUsersWhenSomeUsernamesAreUnknown() {

        // given
        var adventure = AdventureFixture.privateAdventureWithId();
        var command = new InviteUserToAdventure(
                adventure.getPublicId(), List.of("alice", "ghost"), AdventureFixture.OWNER_ID);

        when(adventureRepository.findByPublicId(any())).thenReturn(java.util.Optional.of(adventure));
        when(userRepository.findAllByUsernameIn(anyList()))
                .thenReturn(List.of(userWith(10L, "alice")));

        // when
        var result = handler.execute(command);

        // then
        assertThat(result.invited()).containsExactly("alice");
        verify(eventPublisher, times(1)).publishEvent(any(UserInvitedToAdventureEvent.class));
    }

    @Test
    public void shouldInviteInDatabaseCasingAndNotReportNotFoundWhenNameIsTypedInAnotherCase() {

        // given
        var adventure = AdventureFixture.privateAdventureWithId();
        var command = new InviteUserToAdventure(
                adventure.getPublicId(), List.of("mERLIN"), AdventureFixture.OWNER_ID);

        when(adventureRepository.findByPublicId(any())).thenReturn(java.util.Optional.of(adventure));
        when(userRepository.findAllByUsernameIn(anyList()))
                .thenReturn(List.of(userWith(10L, "Merlin")));

        // when
        var result = handler.execute(command);

        // then
        assertThat(result.invited()).containsExactly("Merlin");
        assertThat(result.notFound()).isEmpty();
    }

    @Test
    public void shouldReportUnknownNameAsTypedWhenItMatchesNoAccount() {

        // given
        var adventure = AdventureFixture.privateAdventureWithId();
        var command = new InviteUserToAdventure(
                adventure.getPublicId(), List.of("alice", "Ghost.Player"), AdventureFixture.OWNER_ID);

        when(adventureRepository.findByPublicId(any())).thenReturn(java.util.Optional.of(adventure));
        when(userRepository.findAllByUsernameIn(anyList()))
                .thenReturn(List.of(userWith(10L, "alice")));

        // when
        var result = handler.execute(command);

        // then
        assertThat(result.notFound()).containsExactly("Ghost.Player");
    }

    @Test
    public void shouldThrowWhenAdventureIsNotFound() {

        // given
        var command = new InviteUserToAdventure(
                AdventureFixture.PUBLIC_ID, List.of("alice"), AdventureFixture.OWNER_ID);

        when(adventureRepository.findByPublicId(any())).thenReturn(java.util.Optional.empty());

        // then
        assertThatThrownBy(() -> handler.execute(command)).isInstanceOf(NotFoundException.class);
        verify(adventureRepository, never()).save(any(Adventure.class));
    }
}
