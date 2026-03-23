package me.moirai.storyengine.infrastructure.outbound.adapter.discord;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import me.moirai.storyengine.core.port.outbound.discord.DiscordEmbeddedMessageRequest;
import me.moirai.storyengine.core.port.outbound.discord.DiscordMessageData;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.DiscordEmbeddedMessageRequestFixture;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Mentions;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.MessageHistory.MessageRetrieveAction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DiscordChannelAdapterTest {

    @Mock
    private User user;

    @Mock
    private Member member;

    @Mock
    private Guild guild;

    @Mock
    private Message message;

    @Mock
    private TextChannel textChannel;

    @Mock
    private JDA jda;

    @InjectMocks
    private DiscordChannelAdapter adapter;

    @Test
    void sendMessage_whenCalled_thenMessageShouldBeSent() {

        // Given
        String channelId = "123";
        String authorId = "123";
        String messageContent = "Hello, World!";

        MessageCreateAction messageCreateActionMock = mock(MessageCreateAction.class);
        CacheRestAction<Member> cacheRestActionMemberMock = mock(CacheRestAction.class);

        when(jda.getTextChannelById(channelId)).thenReturn(textChannel);
        when(textChannel.sendMessage(anyString())).thenReturn(messageCreateActionMock);
        when(messageCreateActionMock.complete()).thenReturn(message);
        when(message.getGuild()).thenReturn(guild);
        when(message.getAuthor()).thenReturn(user);
        when(message.getContentRaw()).thenReturn(messageContent);
        when(member.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(authorId);
        when(member.getId()).thenReturn(authorId);
        when(guild.retrieveMemberById(anyString())).thenReturn(cacheRestActionMemberMock);
        when(cacheRestActionMemberMock.complete()).thenReturn(member);

        // When
        DiscordMessageData result = adapter.sendTextMessageTo(channelId, messageContent);

        // Then
        verify(textChannel, times(1)).sendMessage(anyString());

        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo(messageContent);
        assertThat(result.author().getId()).isEqualTo(authorId);
    }

    @Test
    void sendEmbeddedMessage_whenCalled_thenMessageShouldBeSent() {

        // Given
        String channelId = "123";
        String authorId = "123";
        String messageContent = "Some text";

        DiscordEmbeddedMessageRequest embedRequest = DiscordEmbeddedMessageRequestFixture.create().build();
        MessageCreateAction messageCreateActionMock = mock(MessageCreateAction.class);
        CacheRestAction<Member> cacheRestActionMemberMock = mock(CacheRestAction.class);
        MessageEmbed messageEmbed = mock(MessageEmbed.class);

        when(jda.getTextChannelById(channelId)).thenReturn(textChannel);
        when(textChannel.sendMessageEmbeds(any(MessageEmbed.class))).thenReturn(messageCreateActionMock);
        when(messageCreateActionMock.complete()).thenReturn(message);
        when(message.getGuild()).thenReturn(guild);
        when(message.getAuthor()).thenReturn(user);
        when(message.getContentRaw()).thenReturn(messageContent);
        when(member.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(authorId);
        when(member.getId()).thenReturn(authorId);
        when(guild.retrieveMemberById(anyString())).thenReturn(cacheRestActionMemberMock);
        when(cacheRestActionMemberMock.complete()).thenReturn(member);

        when(message.getEmbeds()).thenReturn(singletonList(messageEmbed));
        when(messageEmbed.getDescription()).thenReturn(embedRequest.getMessageContent());

        // When
        DiscordMessageData result = adapter.sendEmbeddedMessageTo(channelId, embedRequest);

        // Then
        verify(textChannel, times(1)).sendMessageEmbeds(any(MessageEmbed.class));

        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo(messageContent);
        assertThat(result.author().getId()).isEqualTo(authorId);
    }

    @Test
    void sendTemporaryMessage_whenCalled_thenMessageShouldBeSent() {

        // Given
        int deleteMessageAfterSeconds = 5;
        String channelId = "123";
        String messageId = "123";
        String messageContent = "Hello, World!";

        MessageCreateAction messageCreateActionMock = mock(MessageCreateAction.class);
        AuditableRestAction<Void> auditableRestAction = mock(AuditableRestAction.class);

        when(jda.getTextChannelById(channelId)).thenReturn(textChannel);
        when(textChannel.sendMessage(anyString())).thenReturn(messageCreateActionMock);
        when(messageCreateActionMock.complete()).thenReturn(message);
        when(message.getId()).thenReturn(messageId);
        when(textChannel.deleteMessageById(anyString())).thenReturn(auditableRestAction);
        when(auditableRestAction.completeAfter(anyLong(), any(TimeUnit.class))).thenReturn(null);
        when(message.delete()).thenReturn(auditableRestAction);

        // When
        adapter.sendTemporaryTextMessageTo(channelId, messageContent, deleteMessageAfterSeconds);

        // Then
        verify(textChannel, times(1)).sendMessage(anyString());
    }

    @Test
    void sendTemporaryEmbeddedMessage_whenCalled_thenMessageShouldBeSent() {

        // Given
        int deleteMessageAfterSeconds = 5;
        String channelId = "123";
        String messageId = "123";

        DiscordEmbeddedMessageRequest embedRequest = DiscordEmbeddedMessageRequestFixture.create().build();
        MessageCreateAction messageCreateActionMock = mock(MessageCreateAction.class);
        AuditableRestAction<Void> auditableRestAction = mock(AuditableRestAction.class);
        MessageEmbed messageEmbed = mock(MessageEmbed.class);

        when(jda.getTextChannelById(channelId)).thenReturn(textChannel);
        when(textChannel.sendMessageEmbeds(any(MessageEmbed.class))).thenReturn(messageCreateActionMock);
        when(messageCreateActionMock.complete()).thenReturn(message);
        when(message.getId()).thenReturn(messageId);
        when(textChannel.deleteMessageById(anyString())).thenReturn(auditableRestAction);
        when(auditableRestAction.completeAfter(anyLong(), any(TimeUnit.class))).thenReturn(null);
        when(message.delete()).thenReturn(auditableRestAction);

        when(message.getEmbeds()).thenReturn(singletonList(messageEmbed));
        when(messageEmbed.getDescription()).thenReturn(embedRequest.getMessageContent());

        // When
        adapter.sendTemporaryEmbeddedMessageTo(channelId, embedRequest, deleteMessageAfterSeconds);

        // Then
        verify(textChannel, times(1)).sendMessageEmbeds(any(MessageEmbed.class));
    }

    @Test
    void getMessageById_whenCalled_thenMessageShouldBeRetrieved() {

        // Given
        String authorId = "123";
        String mention = "<@123>";
        String nickname = "FireDragon";
        String username = "john.doe";
        String channelId = "123";
        String messageId = "123";
        String messageContent = "Hello, World!";
        String expectedMessageContent = "@john.doe (known as FireDragon) said: Hello, World!";

        Mentions mentions = mock(Mentions.class);
        CacheRestAction<Member> cacheRestActionMemberMock = mock(CacheRestAction.class);
        RestAction<Message> restActionMessage = mock(RestAction.class);

        when(jda.getTextChannelById(channelId)).thenReturn(textChannel);
        when(textChannel.retrieveMessageById(anyString())).thenReturn(restActionMessage);
        when(restActionMessage.complete()).thenReturn(message);
        when(message.getGuild()).thenReturn(guild);
        when(guild.retrieveMemberById(anyString())).thenReturn(cacheRestActionMemberMock);
        when(cacheRestActionMemberMock.complete()).thenReturn(member);
        when(message.getAuthor()).thenReturn(user);
        when(user.getId()).thenReturn(authorId);
        when(user.getName()).thenReturn(username);
        when(member.getUser()).thenReturn(user);
        when(member.getId()).thenReturn(authorId);
        when(member.getNickname()).thenReturn(nickname);
        when(member.getAsMention()).thenReturn(mention);
        when(message.getContentRaw()).thenReturn(messageContent);
        when(message.getMentions()).thenReturn(mentions);
        when(mentions.getMembers()).thenReturn(singletonList(member));

        // When
        Optional<DiscordMessageData> result = adapter.getMessageById(channelId, messageId);

        // Then
        verify(textChannel, times(1)).retrieveMessageById(anyString());

        assertThat(result).isNotNull().isNotEmpty();
        assertThat(result.get().content()).isEqualTo(expectedMessageContent);
        assertThat(result.get().author().getId()).isEqualTo(authorId);
        assertThat(result.get().channelId()).isEqualTo(channelId);
    }

    @Test
    void getMessageById_whenErrorIsThrown_thenEmptyResultIsReturned() {

        // Given
        String channelId = "123";
        String messageId = "123";

        when(jda.getTextChannelById(channelId)).thenThrow(RuntimeException.class);

        // When
        Optional<DiscordMessageData> result = adapter.getMessageById(channelId, messageId);

        // Then
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    void deleteMessageById_whenCalled_thenMessageShouldBeDeleted() {

        // Given
        String channelId = "123";
        String messageId = "456";

        AuditableRestAction<Void> auditableRestAction = mock(AuditableRestAction.class);

        when(jda.getTextChannelById(channelId)).thenReturn(textChannel);
        when(textChannel.deleteMessageById(anyString())).thenReturn(auditableRestAction);

        // When
        adapter.deleteMessageById(channelId, messageId);

        // Then
        verify(auditableRestAction, times(1)).complete();
    }

    @Test
    void editMessageById_whenCalled_thenMessageShouldBeEdited() {

        // Given
        String authorId = "123";
        String mention = "<@123>";
        String nickname = "FireDragon";
        String username = "john.doe";
        String channelId = "123";
        String messageId = "456";
        String newMessageContent = "Edited message";

        Mentions mentions = mock(Mentions.class);
        CacheRestAction<Member> cacheRestActionMemberMock = mock(CacheRestAction.class);
        RestAction<Message> restActionMessage = mock(RestAction.class);
        MessageEditAction messageEditAction = mock(MessageEditAction.class);

        when(jda.getTextChannelById(channelId)).thenReturn(textChannel);
        when(textChannel.retrieveMessageById(anyString())).thenReturn(restActionMessage);
        when(restActionMessage.complete()).thenReturn(message);
        when(message.editMessage(anyString())).thenReturn(messageEditAction);
        when(messageEditAction.complete()).thenReturn(message);
        when(message.getGuild()).thenReturn(guild);
        when(guild.retrieveMemberById(anyString())).thenReturn(cacheRestActionMemberMock);
        when(cacheRestActionMemberMock.complete()).thenReturn(member);
        when(message.getAuthor()).thenReturn(user);
        when(member.getUser()).thenReturn(user);
        when(member.getId()).thenReturn(authorId);
        when(member.getNickname()).thenReturn(nickname);
        when(member.getAsMention()).thenReturn(mention);
        when(user.getId()).thenReturn(authorId);
        when(user.getName()).thenReturn(username);
        when(member.getId()).thenReturn(authorId);
        when(message.getMentions()).thenReturn(mentions);
        when(mentions.getMembers()).thenReturn(singletonList(member));

        // When
        DiscordMessageData result = adapter.editMessageById(channelId, messageId, newMessageContent);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.author().getId()).isEqualTo(authorId);
        assertThat(result.content()).isEqualTo(newMessageContent);
    }

    @Test
    void messageHistory_whenEntireHistoryWanted_thenHistoryShouldReturnAllMessages() {

        // Given
        String authorId = "123";
        String mention = "<@123>";
        String nickname = "NCKNM";
        String username = "john.doe";
        String channelId = "123";
        int expectedMessagesInEnd = 5;
        List<Message> messageList = buildMessageList(5);

        MessageRetrieveAction messageRetrieveActionMock = mock(MessageRetrieveAction.class);
        MessageHistory messageHistoryMock = mock(MessageHistory.class);
        MockedStatic<MessageHistory> messageHistoryStaticMock = mockStatic(MessageHistory.class);

        when(jda.getTextChannelById(channelId)).thenReturn(textChannel);
        when(messageRetrieveActionMock.limit(anyInt())).thenReturn(messageRetrieveActionMock);
        when(messageRetrieveActionMock.complete()).thenReturn(messageHistoryMock);
        when(messageHistoryMock.getRetrievedHistory()).thenReturn(messageList);
        when(user.getName()).thenReturn(username);
        when(member.getUser()).thenReturn(user);
        when(member.getId()).thenReturn(authorId);
        when(member.getNickname()).thenReturn(nickname);
        when(member.getAsMention()).thenReturn(mention);

        messageHistoryStaticMock.when(() -> MessageHistory.getHistoryFromBeginning(any()))
                .thenReturn(messageRetrieveActionMock);

        // When
        List<DiscordMessageData> result = adapter.retrieveEntireHistoryFrom(channelId);

        // Then
        assertThat(result).isNotNull().hasSize(expectedMessagesInEnd);
        assertThat(result.get(0).content()).isEqualTo("@john.doe (known as NCKNM) said: Message 1");
        assertThat(result.get(1).content()).isEqualTo("@john.doe (known as NCKNM) said: Message 2");
        assertThat(result.get(2).content()).isEqualTo("@john.doe (known as NCKNM) said: Message 3");
        assertThat(result.get(3).content()).isEqualTo("@john.doe (known as NCKNM) said: Message 4");
        assertThat(result.get(4).content()).isEqualTo("@john.doe (known as NCKNM) said: Message 5");

        messageHistoryStaticMock.close();
    }

    @Test
    void messageHistory_whenMessagesBeforeOneSuppliedWanted_thenHistoryShouldReturnAllMessages() {

        // Given
        String authorId = "123";
        String mention = "<@123>";
        String nickname = "NCKNM";
        String username = "john.doe";
        String messageId = "123";
        String channelId = "123";
        int expectedMessagesInEnd = 5;
        List<Message> messageList = buildMessageList(5);

        Mentions mentions = mock(Mentions.class);
        MessageRetrieveAction messageRetrieveActionMock = mock(MessageRetrieveAction.class);
        MessageHistory messageHistoryMock = mock(MessageHistory.class);
        MockedStatic<MessageHistory> messageHistoryStaticMock = mockStatic(MessageHistory.class);

        when(jda.getTextChannelById(channelId)).thenReturn(textChannel);
        when(messageRetrieveActionMock.limit(anyInt())).thenReturn(messageRetrieveActionMock);
        when(messageRetrieveActionMock.complete()).thenReturn(messageHistoryMock);
        when(messageHistoryMock.getRetrievedHistory()).thenReturn(messageList);
        when(message.getMentions()).thenReturn(mentions);
        when(mentions.getMembers()).thenReturn(singletonList(member));
        when(user.getName()).thenReturn(username);
        when(member.getUser()).thenReturn(user);
        when(member.getId()).thenReturn(authorId);
        when(member.getNickname()).thenReturn(nickname);
        when(member.getAsMention()).thenReturn(mention);

        messageHistoryStaticMock.when(() -> MessageHistory.getHistoryBefore(any(), anyString()))
                .thenReturn(messageRetrieveActionMock);

        // When
        List<DiscordMessageData> result = adapter.retrieveEntireHistoryBefore(messageId, channelId);

        // Then
        assertThat(result).isNotNull().hasSize(expectedMessagesInEnd);
        assertThat(result.get(0).content()).isEqualTo("@john.doe (known as NCKNM) said: Message 1");
        assertThat(result.get(1).content()).isEqualTo("@john.doe (known as NCKNM) said: Message 2");
        assertThat(result.get(2).content()).isEqualTo("@john.doe (known as NCKNM) said: Message 3");
        assertThat(result.get(3).content()).isEqualTo("@john.doe (known as NCKNM) said: Message 4");
        assertThat(result.get(4).content()).isEqualTo("@john.doe (known as NCKNM) said: Message 5");

        messageHistoryStaticMock.close();
    }

    @Test
    void messageHistory_whenMessagesBeforeOneSuppliedWantedAndNoUsersAreMentioned_thenHistoryShouldReturnAllMessages() {

        // Given
        String authorId = "123";
        String mention = "<@123>";
        String nickname = "NCKNM";
        String username = "john.doe";
        String messageId = "123";
        String channelId = "123";
        int expectedMessagesInEnd = 5;
        List<Message> messageList = buildMessageList(5);

        Mentions mentions = mock(Mentions.class);
        MessageRetrieveAction messageRetrieveActionMock = mock(MessageRetrieveAction.class);
        MessageHistory messageHistoryMock = mock(MessageHistory.class);
        MockedStatic<MessageHistory> messageHistoryStaticMock = mockStatic(MessageHistory.class);

        when(jda.getTextChannelById(channelId)).thenReturn(textChannel);
        when(messageRetrieveActionMock.limit(anyInt())).thenReturn(messageRetrieveActionMock);
        when(messageRetrieveActionMock.complete()).thenReturn(messageHistoryMock);
        when(messageHistoryMock.getRetrievedHistory()).thenReturn(messageList);
        when(message.getMentions()).thenReturn(mentions);
        when(mentions.getMembers()).thenReturn(Collections.emptyList());
        when(user.getName()).thenReturn(username);
        when(member.getUser()).thenReturn(user);
        when(member.getId()).thenReturn(authorId);
        when(member.getNickname()).thenReturn(nickname);
        when(member.getAsMention()).thenReturn(mention);

        messageHistoryStaticMock.when(() -> MessageHistory.getHistoryBefore(any(), anyString()))
                .thenReturn(messageRetrieveActionMock);

        // When
        List<DiscordMessageData> result = adapter.retrieveEntireHistoryBefore(messageId, channelId);

        // Then
        assertThat(result).isNotNull().hasSize(expectedMessagesInEnd);
        assertThat(result.get(0).content()).isEqualTo("@john.doe (known as NCKNM) said: Message 1");
        assertThat(result.get(1).content()).isEqualTo("@john.doe (known as NCKNM) said: Message 2");
        assertThat(result.get(2).content()).isEqualTo("@john.doe (known as NCKNM) said: Message 3");
        assertThat(result.get(3).content()).isEqualTo("@john.doe (known as NCKNM) said: Message 4");
        assertThat(result.get(4).content()).isEqualTo("@john.doe (known as NCKNM) said: Message 5");

        messageHistoryStaticMock.close();
    }

    private List<Message> buildMessageList(int amountOfMessages) {

        List<Message> messageList = new ArrayList<>();

        for (int i = 0; i < amountOfMessages; i++) {
            int index = i + 1;
            String content = String.format("Message %s", index);
            Message message = mock(Message.class);
            Mentions mentions = mock(Mentions.class);

            CacheRestAction<Member> cacheRestActionMember = mock(CacheRestAction.class);

            when(message.getGuild()).thenReturn(guild);
            when(message.getAuthor()).thenReturn(user);
            when(user.getId()).thenReturn("USRID");
            when(message.getContentRaw()).thenReturn(content);
            when(guild.retrieveMemberById(anyString())).thenReturn(cacheRestActionMember);
            when(cacheRestActionMember.complete()).thenReturn(member);
            when(member.getNickname()).thenReturn("NCKNM");
            when(member.getUser()).thenReturn(user);
            when(user.getName()).thenReturn("NM");
            when(message.getMentions()).thenReturn(mentions);
            when(mentions.getUsers()).thenReturn(Collections.emptyList());

            messageList.add(message);
        }

        return messageList;
    }
}
