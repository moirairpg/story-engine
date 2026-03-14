package me.moirai.storyengine.core.application.usecase.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.application.usecase.notification.request.StreamNotificationsForUser;
import me.moirai.storyengine.core.domain.notification.Notification;
import me.moirai.storyengine.core.domain.notification.NotificationFixture;
import me.moirai.storyengine.core.domain.notification.NotificationService;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class StreamNotificationsForUserHandlerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private StreamNotificationsForUserHandler handler;

    @Test
    public void streamNotifications_whenTargetUserIdIsEmpty_thenThrowException() {

        // Given
        String targetUserId = null;
        StreamNotificationsForUser request = StreamNotificationsForUser.create(targetUserId);

        // Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(request));
    }

    @Test
    public void streamNotifications_whenValidRequest_thenStreamNotifications() {

        // Given
        String targetUserId = "12345";
        StreamNotificationsForUser request = StreamNotificationsForUser.create(targetUserId);
        Notification expectedNotification = NotificationFixture.globalUnreadInfo().build();

        when(notificationService.streamNotificationsForUser(anyString()))
                .thenReturn(Flux.just(expectedNotification));

        // Then
        StepVerifier.create(handler.handle(request))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getReceiverDiscordId()).isEqualTo(expectedNotification.getReceiverDiscordId());
                    assertThat(result.getSenderDiscordId()).isEqualTo(expectedNotification.getSenderDiscordId());
                    assertThat(result.getType()).isEqualTo(expectedNotification.getType().name());
                    assertThat(result.getMessage()).isEqualTo(expectedNotification.getMessage());
                    assertThat(result.isGlobal()).isEqualTo(expectedNotification.isGlobal());
                    assertThat(result.isInteractable()).isEqualTo(expectedNotification.isInteractable());
                    assertThat(result.getMetadata()).isEqualTo(expectedNotification.getMetadata());
                })
                .verifyComplete();
    }
}
