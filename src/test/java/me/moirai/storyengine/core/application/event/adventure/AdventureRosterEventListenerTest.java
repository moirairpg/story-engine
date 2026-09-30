package me.moirai.storyengine.core.application.event.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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

import me.moirai.storyengine.common.enums.NotificationKind;
import me.moirai.storyengine.common.enums.NotificationLevel;
import me.moirai.storyengine.common.enums.NotificationType;
import me.moirai.storyengine.core.application.event.notification.NotificationCreatedEvent;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.adventure.EnrolledCharacterDeletedEvent;
import me.moirai.storyengine.core.domain.adventure.PlayerExpelledFromAdventureEvent;
import me.moirai.storyengine.core.domain.adventure.PlayerLeftAdventureEvent;
import me.moirai.storyengine.core.domain.notification.Notification;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.notification.NotificationRepository;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@ExtendWith(MockitoExtension.class)
public class AdventureRosterEventListenerTest {

    private static final Long PLAYER_ID = 10L;
    private static final Long CHARACTER_ID = 1L;
    private static final Long MANAGER_ID = 100L;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private AdventureRepository adventureRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AdventureRosterEventListener listener;

    private Adventure adventureWithEnrolledCharacter() {

        var adventure = AdventureFixture.privateAdventureWithId();
        adventure.enrollPlayerCharacter(CHARACTER_ID, PLAYER_ID);
        adventure.drainEvents();

        return adventure;
    }

    private <T> T drain(Adventure adventure, Class<T> eventType) {

        return adventure.drainEvents().stream()
                .filter(eventType::isInstance)
                .map(eventType::cast)
                .findFirst()
                .orElseThrow();
    }

    private Notification savedNotification() {

        var notification = Notification.builder()
                .type(NotificationType.SYSTEM)
                .level(NotificationLevel.INFO)
                .message("stub")
                .isInteractable(false)
                .recipientUserIds(List.of(PLAYER_ID))
                .build();

        ReflectionTestUtils.setField(notification, "publicId", UUID.randomUUID());

        return notification;
    }

    @Test
    public void shouldNotifyManagersWhenThePlayerLeaves() {

        // given
        var adventure = adventureWithEnrolledCharacter();
        adventure.leave(CHARACTER_ID);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(UserFixture.player().username("bob").displayName("Bob the Brave").build()));
        when(adventureRepository.findManagerUserIdsByAdventureId(anyLong())).thenReturn(List.of(MANAGER_ID));
        when(notificationRepository.save(any())).thenReturn(savedNotification());

        // when
        listener.onPlayerLeft(drain(adventure, PlayerLeftAdventureEvent.class));

        // then
        var captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        assertThat(captor.getValue().getMessage()).contains("Bob the Brave left");
        assertThat(captor.getValue().getMetadata())
                .containsEntry("kind", NotificationKind.ADVENTURE_MEMBER_LEFT.name());
        assertThat(captor.getValue().getMetadata()).containsOnlyKeys("kind", "adventureId");
        assertThat(captor.getValue().getRecipientUserIds()).containsExactly(MANAGER_ID);
        verify(eventPublisher).publishEvent(any(NotificationCreatedEvent.class));
    }

    @Test
    public void shouldNotifyOnlyTheExpelledPlayerWhenAManagerRemovesThem() {

        // given
        var adventure = adventureWithEnrolledCharacter();
        adventure.expel(CHARACTER_ID);

        when(notificationRepository.save(any())).thenReturn(savedNotification());

        // when
        listener.onPlayerExpelled(drain(adventure, PlayerExpelledFromAdventureEvent.class));

        // then
        var captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        assertThat(captor.getValue().getMessage()).contains("removed from");
        assertThat(captor.getValue().getMetadata())
                .containsEntry("kind", NotificationKind.ADVENTURE_MEMBER_REMOVED.name());
        assertThat(captor.getValue().getRecipientUserIds()).containsExactly(PLAYER_ID);
        verify(eventPublisher).publishEvent(any(NotificationCreatedEvent.class));
    }

    @Test
    public void shouldNotifyManagersWithItsOwnKindWhenTheCharacterIsDeleted() {

        // given
        var adventure = adventureWithEnrolledCharacter();
        adventure.withdrawDeletedCharacter(CHARACTER_ID);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(UserFixture.player().username("bob").displayName("Bob the Brave").build()));
        when(adventureRepository.findManagerUserIdsByAdventureId(anyLong())).thenReturn(List.of(MANAGER_ID));
        when(notificationRepository.save(any())).thenReturn(savedNotification());

        // when
        listener.onEnrolledCharacterDeleted(drain(adventure, EnrolledCharacterDeletedEvent.class));

        // then
        var captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        assertThat(captor.getValue().getMessage()).contains("Bob the Brave deleted their character");
        assertThat(captor.getValue().getMetadata()).containsOnlyKeys("kind", "adventureId");
        assertThat(captor.getValue().getMessage()).doesNotContain("left");
        assertThat(captor.getValue().getMetadata())
                .containsEntry("kind", NotificationKind.ADVENTURE_CHARACTER_DELETED.name());
        assertThat(captor.getValue().getMetadata())
                .doesNotContainEntry("kind", NotificationKind.ADVENTURE_MEMBER_LEFT.name());
        assertThat(captor.getValue().getRecipientUserIds()).containsExactly(MANAGER_ID);
        verify(eventPublisher).publishEvent(any(NotificationCreatedEvent.class));
    }
}
