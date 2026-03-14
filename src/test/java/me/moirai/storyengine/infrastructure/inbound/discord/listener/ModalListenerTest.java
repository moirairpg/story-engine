package me.moirai.storyengine.infrastructure.inbound.discord.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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
import me.moirai.storyengine.common.usecases.UseCaseRunner;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureAuthorsNoteByChannelId;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureBumpByChannelId;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureNudgeByChannelId;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureRememberByChannelId;
import me.moirai.storyengine.core.port.inbound.discord.contextmenu.EditMessage;
import me.moirai.storyengine.core.port.inbound.discord.slashcommands.SayCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

@SuppressWarnings("unchecked")
public class ModalListenerTest extends AbstractDiscordTest {

    @Mock
    private UseCaseRunner useCaseRunner;

    @Mock
    private DiscordListenerErrorHandler errorHandler;

    @Mock
    private DiscordListenerHelper discordListenerHelper;

    @InjectMocks
    private ModalListener listener;

    @Test
    public void whenModalCalled_whenAuthorIsBot_thenDoNotOpenModal() {

        // Given
        ModalInteractionEvent event = mock(ModalInteractionEvent.class);

        when(event.getChannel()).thenReturn(channelUnion);
        when(event.getMember()).thenReturn(member);
        when(member.getUser()).thenReturn(user);
        when(user.isBot()).thenReturn(true);
        when(event.getJDA()).thenReturn(jda);
        when(event.getGuild()).thenReturn(guild);

        // When
        listener.onModalInteraction(event);

        // Then
        verify(useCaseRunner, times(0)).run(any());
    }

    @Test
    public void whenModalCalled_whenInvalidModal_thenDoNotOpenModal() {

        // Given
        String modalName = "invalidModal";

        ModalInteractionEvent event = mock(ModalInteractionEvent.class);

        when(event.getModalId()).thenReturn(modalName);
        when(event.getChannel()).thenReturn(channelUnion);
        when(event.getMember()).thenReturn(member);
        when(user.isBot()).thenReturn(false);
        when(event.getJDA()).thenReturn(jda);
        when(event.getGuild()).thenReturn(guild);

        // When
        listener.onModalInteraction(event);

        // Then
        verify(useCaseRunner, times(0)).run(any());
    }

    @Test
    public void sayModal_whenCommandCalled_thenSendInputToChannel() {

        // Given
        String channelId = "CHID";
        String modalName = "sayAsBot";
        String messageContent = "Message that will be sent as the bot";

        InteractionHook interactionHook = mock(InteractionHook.class);
        ReplyCallbackAction sendMessageCallback = mock(ReplyCallbackAction.class);
        ModalInteractionEvent event = mock(ModalInteractionEvent.class);
        ModalMapping modalMapping = mock(ModalMapping.class);
        WebhookMessageEditAction<Message> editNotificationAction = mock(WebhookMessageEditAction.class);
        RestAction<Message> getMessageAction = mock(RestAction.class);
        RestAction<Void> deleteOriginalAction = mock(RestAction.class);

        ArgumentCaptor<SayCommand> useCaseCaptor = ArgumentCaptor.forClass(SayCommand.class);

        when(event.getModalId()).thenReturn(modalName);
        when(event.getChannel()).thenReturn(channelUnion);
        when(event.reply(anyString())).thenReturn(sendMessageCallback);
        when(sendMessageCallback.setEphemeral(anyBoolean())).thenReturn(sendMessageCallback);
        when(sendMessageCallback.complete()).thenReturn(interactionHook);
        when(event.getValue(anyString())).thenReturn(modalMapping);
        when(modalMapping.getAsString()).thenReturn(messageContent);
        when(interactionHook.deleteOriginal()).thenReturn(deleteOriginalAction);
        when(interactionHook.editOriginal(anyString())).thenReturn(editNotificationAction);
        when(editNotificationAction.onSuccess(any())).thenReturn(getMessageAction);
        when(editNotificationAction.complete()).thenReturn(message);
        when(event.getMember()).thenReturn(member);
        when(user.isBot()).thenReturn(false);
        when(event.getJDA()).thenReturn(jda);
        when(event.getGuild()).thenReturn(guild);

        // When
        listener.onModalInteraction(event);

        // Then
        verify(useCaseRunner, times(1)).run(useCaseCaptor.capture());

        SayCommand useCase = useCaseCaptor.getValue();

        assertThat(useCase.getMessageContent()).isEqualTo(messageContent);
        assertThat(useCase.getChannelId()).isEqualTo(channelId);
    }

    @Test
    public void editMessageModal_whenMessageToBeEditedAuthorIsBot_thenEditMessage() {

        // Given
        String messageContent = "This is a message";
        String modalName = "editMessage";

        InteractionHook interactionHook = mock(InteractionHook.class);
        ReplyCallbackAction sendMessageCallback = mock(ReplyCallbackAction.class);
        ModalInteractionEvent event = mock(ModalInteractionEvent.class);
        ModalMapping modalMapping = mock(ModalMapping.class);
        WebhookMessageEditAction<Message> editNotificationAction = mock(WebhookMessageEditAction.class);
        RestAction<Message> getMessageAction = mock(RestAction.class);
        RestAction<Void> deleteOriginalAction = mock(RestAction.class);

        when(event.getModalId()).thenReturn(modalName);
        when(event.getChannel()).thenReturn(channelUnion);
        when(event.getMember()).thenReturn(member);
        when(user.isBot()).thenReturn(false);
        when(event.getJDA()).thenReturn(jda);
        when(event.getGuild()).thenReturn(guild);

        when(event.reply(anyString())).thenReturn(sendMessageCallback);
        when(sendMessageCallback.setEphemeral(anyBoolean())).thenReturn(sendMessageCallback);
        when(sendMessageCallback.complete()).thenReturn(interactionHook);

        when(event.getValue(anyString())).thenReturn(modalMapping);
        when(modalMapping.getAsString()).thenReturn(messageContent).thenReturn(MESSAGE_ID);

        when(textChannel.retrieveMessageById(anyString())).thenReturn(getMessageAction);
        when(getMessageAction.complete()).thenReturn(message);

        when(interactionHook.editOriginal(anyString())).thenReturn(editNotificationAction);
        when(editNotificationAction.onSuccess(any())).thenReturn(getMessageAction);
        when(interactionHook.deleteOriginal()).thenReturn(deleteOriginalAction);
        when(editNotificationAction.complete()).thenReturn(message);

        ArgumentCaptor<EditMessage> useCaseCaptor = ArgumentCaptor.forClass(EditMessage.class);

        when(useCaseRunner.run(useCaseCaptor.capture())).thenReturn(mock(Void.class));

        // When
        listener.onModalInteraction(event);

        // Then
        verify(useCaseRunner, times(1)).run(any());

        EditMessage useCase = useCaseCaptor.getValue();
        assertThat(useCase).isNotNull();
        assertThat(useCase.getChannelId()).isEqualTo(CHANNEL_ID);
        assertThat(useCase.getMessageId()).isEqualTo(MESSAGE_ID);
        assertThat(useCase.getMessageContent()).isEqualTo(messageContent);
    }

    @Test
    public void editMessageModal_whenMessageAuthorIsNotBot_thenDoNothing() {

        // Given
        String botId = "BOTID";
        String modalName = "editMessage";
        String messageContent = "This is a message";
        String notificationContentExpected = "It's only possible to edit messages sent by " + NICKNAME;

        InteractionHook interactionHook = mock(InteractionHook.class);
        ReplyCallbackAction sendMessageCallback = mock(ReplyCallbackAction.class);
        ModalInteractionEvent event = mock(ModalInteractionEvent.class);
        ModalMapping modalMapping = mock(ModalMapping.class);
        RestAction<Message> getMessageAction = mock(RestAction.class);
        ArgumentCaptor<String> notificationCaptor = ArgumentCaptor.forClass(String.class);

        when(event.getModalId()).thenReturn(modalName);
        when(event.getChannel()).thenReturn(channelUnion);
        when(event.getMember()).thenReturn(member);
        when(user.isBot()).thenReturn(false);
        when(event.getJDA()).thenReturn(jda);
        when(event.getGuild()).thenReturn(guild);
        when(member.getId()).thenReturn(botId);

        when(event.getValue(anyString())).thenReturn(modalMapping);
        when(modalMapping.getAsString()).thenReturn(messageContent).thenReturn(MESSAGE_ID);

        when(textChannel.retrieveMessageById(anyString())).thenReturn(getMessageAction);
        when(getMessageAction.complete()).thenReturn(message);

        when(event.reply(anyString())).thenReturn(sendMessageCallback);
        when(sendMessageCallback.setEphemeral(anyBoolean())).thenReturn(sendMessageCallback);
        when(sendMessageCallback.complete()).thenReturn(interactionHook);

        when(discordListenerHelper.updateNotification(any(), notificationCaptor.capture())).thenReturn(message);

        // When
        listener.onModalInteraction(event);

        // Then
        String notificationContent = notificationCaptor.getValue();
        assertThat(notificationContent).isNotNull()
                .isNotEmpty()
                .isEqualTo(notificationContentExpected);
    }

    @Test
    public void editMessageModal_whenMessageAuthorIsNotBotAndBotNicknameIsNull_thenDoNothing() {

        // Given
        String botId = "BOTID";
        String modalName = "editMessage";
        String messageContent = "This is a message";
        String notificationContentExpected = "It's only possible to edit messages sent by " + USERNAME;

        InteractionHook interactionHook = mock(InteractionHook.class);
        ReplyCallbackAction sendMessageCallback = mock(ReplyCallbackAction.class);
        ModalInteractionEvent event = mock(ModalInteractionEvent.class);
        ModalMapping modalMapping = mock(ModalMapping.class);
        RestAction<Message> getMessageAction = mock(RestAction.class);

        ArgumentCaptor<String> notificationCaptor = ArgumentCaptor.forClass(String.class);

        when(event.getModalId()).thenReturn(modalName);
        when(event.getChannel()).thenReturn(channelUnion);
        when(event.getMember()).thenReturn(member);
        when(user.isBot()).thenReturn(false);
        when(event.getJDA()).thenReturn(jda);
        when(event.getGuild()).thenReturn(guild);
        when(member.getId()).thenReturn(botId);
        when(member.getNickname()).thenReturn(null);

        when(event.getValue(anyString())).thenReturn(modalMapping);
        when(modalMapping.getAsString()).thenReturn(messageContent).thenReturn(MESSAGE_ID);

        when(textChannel.retrieveMessageById(anyString())).thenReturn(getMessageAction);
        when(getMessageAction.complete()).thenReturn(message);

        when(event.reply(anyString())).thenReturn(sendMessageCallback);
        when(sendMessageCallback.setEphemeral(anyBoolean())).thenReturn(sendMessageCallback);
        when(sendMessageCallback.complete()).thenReturn(interactionHook);

        when(discordListenerHelper.updateNotification(any(), notificationCaptor.capture())).thenReturn(message);

        // When
        listener.onModalInteraction(event);

        // Then
        String notificationContent = notificationCaptor.getValue();
        assertThat(notificationContent).isNotNull()
                .isNotEmpty()
                .isEqualTo(notificationContentExpected);
    }

    @Test
    public void rememberModal_whenCreated_thenUpdateRemember() {

        // Given
        String modalName = "remember";
        String contentToUpdate = "This is the new content";

        InteractionHook interactionHook = mock(InteractionHook.class);
        ReplyCallbackAction sendMessageCallback = mock(ReplyCallbackAction.class);
        ModalInteractionEvent event = mock(ModalInteractionEvent.class);
        ModalMapping modalMapping = mock(ModalMapping.class);
        WebhookMessageEditAction<Message> editNotificationAction = mock(WebhookMessageEditAction.class);
        RestAction<Message> getMessageAction = mock(RestAction.class);
        RestAction<Void> deleteOriginalAction = mock(RestAction.class);

        when(event.getModalId()).thenReturn(modalName);
        when(event.getChannel()).thenReturn(channelUnion);
        when(event.getMember()).thenReturn(member);
        when(user.isBot()).thenReturn(false);
        when(event.getJDA()).thenReturn(jda);
        when(event.getGuild()).thenReturn(guild);

        when(event.getModalId()).thenReturn(modalName);
        when(event.getChannel()).thenReturn(channelUnion);
        when(event.reply(anyString())).thenReturn(sendMessageCallback);
        when(sendMessageCallback.setEphemeral(anyBoolean())).thenReturn(sendMessageCallback);
        when(sendMessageCallback.complete()).thenReturn(interactionHook);
        when(event.getValue(anyString())).thenReturn(modalMapping);
        when(modalMapping.getAsString()).thenReturn(contentToUpdate);
        when(interactionHook.deleteOriginal()).thenReturn(deleteOriginalAction);
        when(interactionHook.editOriginal(anyString())).thenReturn(editNotificationAction);
        when(editNotificationAction.onSuccess(any())).thenReturn(getMessageAction);
        when(editNotificationAction.complete()).thenReturn(message);
        when(event.getMember()).thenReturn(member);
        when(user.isBot()).thenReturn(false);
        when(event.getJDA()).thenReturn(jda);
        when(event.getGuild()).thenReturn(guild);

        ArgumentCaptor<UpdateAdventureRememberByChannelId> captor = ArgumentCaptor
                .forClass(UpdateAdventureRememberByChannelId.class);

        when(useCaseRunner.run(captor.capture())).thenReturn(null);

        // When
        listener.onModalInteraction(event);

        // Then
        UpdateAdventureRememberByChannelId request = captor.getValue();

        assertThat(request).isNotNull();
        assertThat(request.getRemember()).isEqualTo(contentToUpdate);
    }

    @Test
    public void nudgeModal_whenCreated_thenUpdateRemember() {

        // Given
        String modalName = "nudge";
        String contentToUpdate = "This is the new content";

        InteractionHook interactionHook = mock(InteractionHook.class);
        ReplyCallbackAction sendMessageCallback = mock(ReplyCallbackAction.class);
        ModalInteractionEvent event = mock(ModalInteractionEvent.class);
        ModalMapping modalMapping = mock(ModalMapping.class);
        WebhookMessageEditAction<Message> editNotificationAction = mock(WebhookMessageEditAction.class);
        RestAction<Message> getMessageAction = mock(RestAction.class);
        RestAction<Void> deleteOriginalAction = mock(RestAction.class);

        when(event.getModalId()).thenReturn(modalName);
        when(event.getChannel()).thenReturn(channelUnion);
        when(event.getMember()).thenReturn(member);
        when(user.isBot()).thenReturn(false);
        when(event.getJDA()).thenReturn(jda);
        when(event.getGuild()).thenReturn(guild);

        when(event.getModalId()).thenReturn(modalName);
        when(event.getChannel()).thenReturn(channelUnion);
        when(event.reply(anyString())).thenReturn(sendMessageCallback);
        when(sendMessageCallback.setEphemeral(anyBoolean())).thenReturn(sendMessageCallback);
        when(sendMessageCallback.complete()).thenReturn(interactionHook);
        when(event.getValue(anyString())).thenReturn(modalMapping);
        when(modalMapping.getAsString()).thenReturn(contentToUpdate);
        when(interactionHook.deleteOriginal()).thenReturn(deleteOriginalAction);
        when(interactionHook.editOriginal(anyString())).thenReturn(editNotificationAction);
        when(editNotificationAction.onSuccess(any())).thenReturn(getMessageAction);
        when(editNotificationAction.complete()).thenReturn(message);
        when(event.getMember()).thenReturn(member);
        when(user.isBot()).thenReturn(false);
        when(event.getJDA()).thenReturn(jda);
        when(event.getGuild()).thenReturn(guild);

        ArgumentCaptor<UpdateAdventureNudgeByChannelId> captor = ArgumentCaptor
                .forClass(UpdateAdventureNudgeByChannelId.class);

        when(useCaseRunner.run(captor.capture())).thenReturn(null);

        // When
        listener.onModalInteraction(event);

        // Then
        UpdateAdventureNudgeByChannelId request = captor.getValue();

        assertThat(request).isNotNull();
        assertThat(request.getNudge()).isEqualTo(contentToUpdate);
    }

    @Test
    public void authorsNoteModal_whenCreated_thenUpdateRemember() {

        // Given
        String modalName = "authorsNote";
        String contentToUpdate = "This is the new content";

        InteractionHook interactionHook = mock(InteractionHook.class);
        ReplyCallbackAction sendMessageCallback = mock(ReplyCallbackAction.class);
        ModalInteractionEvent event = mock(ModalInteractionEvent.class);
        ModalMapping modalMapping = mock(ModalMapping.class);
        WebhookMessageEditAction<Message> editNotificationAction = mock(WebhookMessageEditAction.class);
        RestAction<Message> getMessageAction = mock(RestAction.class);
        RestAction<Void> deleteOriginalAction = mock(RestAction.class);

        when(event.getModalId()).thenReturn(modalName);
        when(event.getChannel()).thenReturn(channelUnion);
        when(event.getMember()).thenReturn(member);
        when(user.isBot()).thenReturn(false);
        when(event.getJDA()).thenReturn(jda);
        when(event.getGuild()).thenReturn(guild);

        when(event.getModalId()).thenReturn(modalName);
        when(event.getChannel()).thenReturn(channelUnion);
        when(event.reply(anyString())).thenReturn(sendMessageCallback);
        when(sendMessageCallback.setEphemeral(anyBoolean())).thenReturn(sendMessageCallback);
        when(sendMessageCallback.complete()).thenReturn(interactionHook);
        when(event.getValue(anyString())).thenReturn(modalMapping);
        when(modalMapping.getAsString()).thenReturn(contentToUpdate);
        when(interactionHook.deleteOriginal()).thenReturn(deleteOriginalAction);
        when(interactionHook.editOriginal(anyString())).thenReturn(editNotificationAction);
        when(editNotificationAction.onSuccess(any())).thenReturn(getMessageAction);
        when(editNotificationAction.complete()).thenReturn(message);
        when(event.getMember()).thenReturn(member);
        when(user.isBot()).thenReturn(false);
        when(event.getJDA()).thenReturn(jda);
        when(event.getGuild()).thenReturn(guild);

        ArgumentCaptor<UpdateAdventureAuthorsNoteByChannelId> captor = ArgumentCaptor
                .forClass(UpdateAdventureAuthorsNoteByChannelId.class);

        when(useCaseRunner.run(captor.capture())).thenReturn(null);

        // When
        listener.onModalInteraction(event);

        // Then
        UpdateAdventureAuthorsNoteByChannelId request = captor.getValue();

        assertThat(request).isNotNull();
        assertThat(request.getAuthorsNote()).isEqualTo(contentToUpdate);
    }

    @Test
    public void bumpModal_whenCreated_thenUpdateRemember() {

        // Given
        String modalName = "bump";
        String contentToUpdate = "This is the new content";
        String bumpFrequency = "5";

        InteractionHook interactionHook = mock(InteractionHook.class);
        ReplyCallbackAction sendMessageCallback = mock(ReplyCallbackAction.class);
        ModalInteractionEvent event = mock(ModalInteractionEvent.class);
        ModalMapping modalMapping = mock(ModalMapping.class);
        WebhookMessageEditAction<Message> editNotificationAction = mock(WebhookMessageEditAction.class);
        RestAction<Message> getMessageAction = mock(RestAction.class);
        RestAction<Void> deleteOriginalAction = mock(RestAction.class);

        when(event.getModalId()).thenReturn(modalName);
        when(event.getChannel()).thenReturn(channelUnion);
        when(event.getMember()).thenReturn(member);
        when(user.isBot()).thenReturn(false);
        when(event.getJDA()).thenReturn(jda);
        when(event.getGuild()).thenReturn(guild);

        when(event.getModalId()).thenReturn(modalName);
        when(event.getChannel()).thenReturn(channelUnion);
        when(event.reply(anyString())).thenReturn(sendMessageCallback);
        when(sendMessageCallback.setEphemeral(anyBoolean())).thenReturn(sendMessageCallback);
        when(sendMessageCallback.complete()).thenReturn(interactionHook);
        when(event.getValue(anyString())).thenReturn(modalMapping);
        when(modalMapping.getAsString()).thenReturn(contentToUpdate).thenReturn(bumpFrequency);
        when(interactionHook.deleteOriginal()).thenReturn(deleteOriginalAction);
        when(interactionHook.editOriginal(anyString())).thenReturn(editNotificationAction);
        when(editNotificationAction.onSuccess(any())).thenReturn(getMessageAction);
        when(editNotificationAction.complete()).thenReturn(message);
        when(event.getMember()).thenReturn(member);
        when(user.isBot()).thenReturn(false);
        when(event.getJDA()).thenReturn(jda);
        when(event.getGuild()).thenReturn(guild);

        ArgumentCaptor<UpdateAdventureBumpByChannelId> captor = ArgumentCaptor
                .forClass(UpdateAdventureBumpByChannelId.class);

        when(useCaseRunner.run(captor.capture())).thenReturn(null);

        // When
        listener.onModalInteraction(event);

        // Then
        UpdateAdventureBumpByChannelId request = captor.getValue();

        assertThat(request).isNotNull();
        assertThat(request.getBump()).isEqualTo(contentToUpdate);
        assertThat(request.getBumpFrequency()).isEqualTo(Integer.valueOf(bumpFrequency));
    }
}
