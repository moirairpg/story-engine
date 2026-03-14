package me.moirai.storyengine.infrastructure.inbound.discord.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.AbstractDiscordTest;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

@SuppressWarnings("all")
@ExtendWith(MockitoExtension.class)
public class DiscordListenerHelperTest extends AbstractDiscordTest {

    @InjectMocks
    private DiscordListenerHelper helper;

    @Test
    public void sendNotification_whenEventIsUpdated_thenSendNotification() {

        // Given
        String message = "Some message";
        ModalInteractionEvent event = mock(ModalInteractionEvent.class);
        ReplyCallbackAction replyCallbackAction = mock(ReplyCallbackAction.class);
        InteractionHook interactionHook = mock(InteractionHook.class);

        when(event.reply(anyString())).thenReturn(replyCallbackAction);
        when(replyCallbackAction.setEphemeral(anyBoolean())).thenReturn(replyCallbackAction);
        when(replyCallbackAction.complete()).thenReturn(interactionHook);

        // When
        InteractionHook result = helper.sendNotification(event, message);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    public void updateNotification_whenEventIsUpdated_thenUpdateNotification() {

        // Given
        String messageContent = "Some message";
        InteractionHook interactionHook = mock(InteractionHook.class);
        WebhookMessageEditAction webhookMessageEditAction = mock(WebhookMessageEditAction.class);
        RestAction restAction = mock(RestAction.class);

        when(interactionHook.editOriginal(anyString())).thenReturn(webhookMessageEditAction);
        when(webhookMessageEditAction.onSuccess(any())).thenReturn(restAction);
        when(restAction.complete()).thenReturn(message);

        // When
        Message result = helper.updateNotification(interactionHook, messageContent);

        // Then
        assertThat(message).isNotNull();
    }
}
