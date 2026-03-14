package me.moirai.storyengine.infrastructure.inbound.discord.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.list;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.AbstractDiscordTest;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.exception.ModerationException;
import me.moirai.storyengine.core.application.port.DiscordChannelPort;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.DiscordEmbeddedMessageRequest;
import net.dv8tion.jda.api.interactions.InteractionHook;

@ExtendWith(MockitoExtension.class)
public class DiscordListenerErrorHandlerTest extends AbstractDiscordTest {

    @Mock
    private DiscordListenerHelper discordListenerHelper;

    @Mock
    private DiscordChannelPort discordChannelPort;

    @InjectMocks
    private DiscordListenerErrorHandler handler;

    @Test
    public void notificationError_whenNotificationReceivedWithError_andIsModerationError_thenUpdateNotification() {

        // Given
        ModerationException error = new ModerationException("Inappropriate content.", list("violence"));
        InteractionHook interactionHook = mock(InteractionHook.class);

        // When
        handler.handleError(interactionHook, error);

        // Then
        verify(discordListenerHelper, times(1)).updateNotification(any(), any());
    }

    @Test
    public void notificationError_whenNotificationReceivedWithError_andIsNotFoundError_thenUpdateNotification() {

        // Given
        AssetNotFoundException error = new AssetNotFoundException("Persona not found");
        InteractionHook interactionHook = mock(InteractionHook.class);

        // When
        handler.handleError(interactionHook, error);

        // Then
        verify(discordListenerHelper, times(1)).updateNotification(any(), any());
    }

    @Test
    public void notificationError_whenNotificationReceivedWithError_andIsUnknownError_thenUpdateNotification() {

        // Given
        IllegalStateException error = new IllegalStateException("Unknown error");
        InteractionHook interactionHook = mock(InteractionHook.class);

        // When
        handler.handleError(interactionHook, error);

        // Then
        verify(discordListenerHelper, times(1)).updateNotification(any(), any());
    }

    @Test
    public void eventError_whenNotificationReceivedWithError_andIsModerationError_thenUpdateNotification() {

        // Given
        String expectedMessage = "Message content was flagged by moderation. The following topics were blocked: violence";
        ModerationException error = new ModerationException("Inappropriate content.", list("violence"));

        ArgumentCaptor<DiscordEmbeddedMessageRequest> captor = ArgumentCaptor
                .forClass(DiscordEmbeddedMessageRequest.class);

        doNothing().when(discordChannelPort).sendTemporaryEmbeddedMessageTo(anyString(), captor.capture(), anyInt());

        // When
        handler.handleError(member, channelUnion, error);

        // Then
        DiscordEmbeddedMessageRequest capturedValue = captor.getValue();
        assertThat(capturedValue).isNotNull();
        assertThat(capturedValue.getMessageContent()).isEqualTo(expectedMessage);
    }

    @Test
    public void eventError_whenNotificationReceivedWithError_andIsNotFoundError_thenUpdateNotification() {

        // Given
        String expectedMessage = "Asset requested was not found";
        AssetNotFoundException error = new AssetNotFoundException(expectedMessage);

        ArgumentCaptor<DiscordEmbeddedMessageRequest> captor = ArgumentCaptor
                .forClass(DiscordEmbeddedMessageRequest.class);

        doNothing().when(discordChannelPort).sendTemporaryEmbeddedMessageTo(anyString(), captor.capture(), anyInt());

        // When
        handler.handleError(member, channelUnion, error);

        // Then
        DiscordEmbeddedMessageRequest capturedValue = captor.getValue();
        assertThat(capturedValue).isNotNull();
        assertThat(capturedValue.getMessageContent()).isEqualTo(expectedMessage);
    }

    @Test
    public void eventError_whenNotificationReceivedWithError_andIsUnknownError_thenUpdateNotification() {

        // Given
        String expectedMessage = "Something went wrong. Please try again.";
        IllegalStateException error = new IllegalStateException("Unknown error");

        ArgumentCaptor<DiscordEmbeddedMessageRequest> captor = ArgumentCaptor
                .forClass(DiscordEmbeddedMessageRequest.class);

        when(member.getNickname()).thenReturn(null);
        doNothing().when(discordChannelPort).sendTemporaryEmbeddedMessageTo(anyString(), captor.capture(), anyInt());

        // When
        handler.handleError(member, channelUnion, error);

        // Then
        DiscordEmbeddedMessageRequest capturedValue = captor.getValue();
        assertThat(capturedValue).isNotNull();
        assertThat(capturedValue.getMessageContent()).isEqualTo(expectedMessage);
    }
}
