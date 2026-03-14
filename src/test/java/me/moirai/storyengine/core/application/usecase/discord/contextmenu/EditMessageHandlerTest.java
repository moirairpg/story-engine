package me.moirai.storyengine.core.application.usecase.discord.contextmenu;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import me.moirai.storyengine.AbstractDiscordTest;
import me.moirai.storyengine.core.application.port.DiscordChannelPort;
import me.moirai.storyengine.core.application.usecase.discord.DiscordMessageData;

public class EditMessageHandlerTest extends AbstractDiscordTest {

    @Mock
    private DiscordChannelPort discordChannelPort;

    @InjectMocks
    private EditMessageHandler handler;

    @Test
    public void editMessage_whenCalled_thenMessageIsEdited() {

        // Given
        String messageContent = "This is a message";

        EditMessage useCase = EditMessage.build(CHANNEL_ID, MESSAGE_ID, messageContent);

        ArgumentCaptor<String> messageContentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> channelIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageIdCaptor = ArgumentCaptor.forClass(String.class);

        when(discordChannelPort.editMessageById(channelIdCaptor.capture(), messageIdCaptor.capture(),
                messageContentCaptor.capture())).thenReturn(mock(DiscordMessageData.class));

        // When
        handler.execute(useCase);

        // Then
        verify(discordChannelPort, times(1))
                .editMessageById(anyString(), anyString(), anyString());

        String capturedMessageContent = messageContentCaptor.getValue();
        String capturedChannelId = channelIdCaptor.getValue();
        String capturedMessageId = messageIdCaptor.getValue();

        assertThat(capturedMessageContent).isNotNull().isNotEmpty().isEqualTo(messageContent);
        assertThat(capturedChannelId).isNotNull().isNotEmpty().isEqualTo(CHANNEL_ID);
        assertThat(capturedMessageId).isNotNull().isNotEmpty().isEqualTo(MESSAGE_ID);
    }
}
