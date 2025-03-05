package me.moirai.discordbot.core.domain.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.assertj.core.util.Maps;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.discordbot.common.exception.AssetNotFoundException;
import me.moirai.discordbot.core.application.usecase.notification.request.SendNotification;
import me.moirai.discordbot.core.application.usecase.notification.request.SendNotificationFixture;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
public class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl service;

    @Test
    public void sendNotification_whenValidData_thenSendNotification() {

        // Given
        SendNotification request = SendNotificationFixture.targetedUnreadInfo()
                .metadata(Maps.newHashMap("something", "something"))
                .build();

        Notification notification = NotificationFixture.targetedUnreadInfo()
                .creatorDiscordId("12345")
                .creationDate(OffsetDateTime.now())
                .lastUpdateDate(OffsetDateTime.now())
                .version(1)
                .metadata(Maps.newHashMap("something", "something"))
                .build();

        when(notificationRepository.save(any())).thenReturn(notification);

        // When
        Notification result = service.sendNotification(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(notification.getId());
        assertThat(result.getMessage()).isEqualTo(notification.getMessage());
        assertThat(result.getSenderDiscordId()).isEqualTo(notification.getSenderDiscordId());
        assertThat(result.getReceiverDiscordId()).isEqualTo(notification.getReceiverDiscordId());
        assertThat(result.getType()).isEqualTo(notification.getType());
        assertThat(result.isGlobal()).isEqualTo(notification.isGlobal());
        assertThat(result.isInteractable()).isEqualTo(notification.isInteractable());
        assertThat(result.getCreatorDiscordId()).isEqualTo(notification.getCreatorDiscordId());
        assertThat(result.getCreationDate()).isEqualTo(notification.getCreationDate());
        assertThat(result.getLastUpdateDate()).isEqualTo(notification.getLastUpdateDate());
    }

    @Test
    public void streamNotifications_whenNotificationNotFound_thenThrowException() {

        // Given
        String userId = "123456";

        Flux<String> notificationFlux = Flux.just("12345");
        Sinks.Many<String> sink = mock(Sinks.Many.class);
        ReflectionTestUtils.setField(service, "sink", sink);

        when(sink.asFlux()).thenReturn(notificationFlux);
        when(notificationRepository.findById(anyString())).thenReturn(Optional.empty());

        // When
        Flux<Notification> result = service.streamNotificationsForUser(userId);

        // Then
        StepVerifier.create(result)
                .verifyError(AssetNotFoundException.class);
    }

    @Test
    public void streamNotifications_whenNotificationsNotGlobal_andForAnotherUser_thenEmptyReturn() {

        // Given
        String userId = "567890";
        Notification notification = NotificationFixture.targetedUnreadInfo().build();

        Flux<String> notificationFlux = Flux.just(notification.getId());
        Sinks.Many<String> sink = mock(Sinks.Many.class);
        ReflectionTestUtils.setField(service, "sink", sink);

        when(sink.asFlux()).thenReturn(notificationFlux);
        when(notificationRepository.findById(anyString())).thenReturn(Optional.of(notification));

        // When
        Flux<Notification> result = service.streamNotificationsForUser(userId);

        // Then
        StepVerifier.create(result).verifyComplete();
    }

    @Test
    public void streamNotifications_whenGlobalNotificationsExist_thenReturnNotifications() {

        // Given
        String userId = "567890";
        Notification notification = NotificationFixture.globalUnreadInfo().build();

        Flux<String> notificationFlux = Flux.just(notification.getId());
        Sinks.Many<String> sink = mock(Sinks.Many.class);
        ReflectionTestUtils.setField(service, "sink", sink);

        when(sink.asFlux()).thenReturn(notificationFlux);
        when(notificationRepository.findById(anyString())).thenReturn(Optional.of(notification));

        // Then
        StepVerifier.create(service.streamNotificationsForUser(userId))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getMessage()).isEqualTo(notification.getMessage());
                })
                .verifyComplete();
    }

    @Test
    public void streamNotifications_whenNotificationIsNotGlobal_andForUserExist_thenReturnNotifications() {

        // Given
        String userId = "567890";
        Notification notification = NotificationFixture.targetedUnreadInfo()
                .receiverDiscordId(userId)
                .build();

        Flux<String> notificationFlux = Flux.just(notification.getId());
        Sinks.Many<String> sink = mock(Sinks.Many.class);
        ReflectionTestUtils.setField(service, "sink", sink);

        when(sink.asFlux()).thenReturn(notificationFlux);
        when(notificationRepository.findById(anyString())).thenReturn(Optional.of(notification));

        // Then
        StepVerifier.create(service.streamNotificationsForUser(userId))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getMessage()).isEqualTo(notification.getMessage());
                })
                .verifyComplete();
    }

    @Test
    public void streamNotifications_whenNotificationIsNotGlobal_andForUserExist_andIsRead_thenReturnEmptyResult() {

        // Given
        String userId = "567890";
        Notification notification = NotificationFixture.targetedUnreadInfo()
                .receiverDiscordId(userId)
                .build();

        notification.markAsRead(userId);

        Flux<String> notificationFlux = Flux.just(notification.getId());
        Sinks.Many<String> sink = mock(Sinks.Many.class);
        ReflectionTestUtils.setField(service, "sink", sink);

        when(sink.asFlux()).thenReturn(notificationFlux);
        when(notificationRepository.findById(anyString())).thenReturn(Optional.of(notification));

        // Then
        StepVerifier.create(service.streamNotificationsForUser(userId))
                .verifyComplete();
    }

    @Test
    public void streamNotifications_whenNotificationIsNotGlobal_andTargetIsBlank_thenReturnEmptyResult() {

        // Given
        String userId = null;
        Notification notification = NotificationFixture.targetedUnreadInfo()
                .receiverDiscordId(userId)
                .build();

        Flux<String> notificationFlux = Flux.just(notification.getId());
        Sinks.Many<String> sink = mock(Sinks.Many.class);
        ReflectionTestUtils.setField(service, "sink", sink);

        when(sink.asFlux()).thenReturn(notificationFlux);
        when(notificationRepository.findById(anyString())).thenReturn(Optional.of(notification));

        // Then
        StepVerifier.create(service.streamNotificationsForUser(userId))
                .verifyComplete();
    }
}
