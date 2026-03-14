package me.moirai.storyengine.core.application.usecase.discord.slashcommands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import me.moirai.storyengine.AbstractDiscordTest;
import me.moirai.storyengine.core.application.port.DiscordChannelPort;

public class SayCommandHandlerTest extends AbstractDiscordTest {

    @Mock
    private DiscordChannelPort discordChannelPort;

    @InjectMocks
    private SayCommandHandler handler;

    @Test
    public void sayCommand_whenCalled_shouldSendMessageAsBot() {

        // Given
        String channelId = "CHID";
        String messageContent = "This is a message.";

        ArgumentCaptor<String> channelIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageContentCaptor = ArgumentCaptor.forClass(String.class);

        SayCommand useCase = SayCommand.build(channelId, messageContent);

        // When
        handler.execute(useCase);

        // Then
        verify(discordChannelPort, times(1))
                .sendTextMessageTo(channelIdCaptor.capture(), messageContentCaptor.capture());

        String channelIdCaptured = channelIdCaptor.getValue();
        String messageContentCaptured = messageContentCaptor.getValue();

        assertThat(channelIdCaptured).isEqualTo(channelId);
        assertThat(messageContentCaptured).isEqualTo(messageContent);
    }
}
