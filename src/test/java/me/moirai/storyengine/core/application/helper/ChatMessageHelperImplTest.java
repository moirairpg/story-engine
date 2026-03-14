package me.moirai.storyengine.core.application.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.port.inbound.discord.DiscordMessageData;
import me.moirai.storyengine.core.application.usecase.discord.DiscordMessageDataFixture;
import me.moirai.storyengine.core.port.outbound.TokenizerPort;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
public class ChatMessageHelperImplTest {

    @Mock
    private TokenizerPort tokenizerPort;

    @InjectMocks
    private ChatMessageAdapter service;

    @Test
    public void addMessagesToContext_whenEnoughTokensAndAssetDefined_thenMessagesAreAdded() {

        // Given
        Map<String, Object> context = createContextWithMessageNumber(10);
        int reservedTokens = 250;
        String assetManipulated = "lorebook";

        context.put(assetManipulated, assetManipulated);

        when(tokenizerPort.getTokenCountFrom(anyString())).thenReturn(20);

        // When
        Map<String, Object> result = service.addMessagesToContext(context, reservedTokens, assetManipulated);

        // Then
        assertThat(result).isNotNull().isNotEmpty();
        assertThat((List<DiscordMessageData>) result.get("retrievedMessages"))
                .isNotNull()
                .isEmpty();

        assertThat((List<DiscordMessageData>) result.get("messageHistory")).isNotNull()
                .isNotEmpty()
                .hasSize(10);
    }

    @Test
    public void addMessagesToContext_whenNotEnoughTokensAndAssetDefined_thenLimitNumberOfMessagesAreAdded() {

        // Given
        Map<String, Object> context = createContextWithMessageNumber(10);
        int expectedNumberOfMessagesAdded = 5;
        int reservedTokens = 250;
        String assetManipulated = "lorebook";

        context.put(assetManipulated, assetManipulated);

        when(tokenizerPort.getTokenCountFrom(anyString()))
                .thenReturn(20) // tokens in asset

                .thenReturn(20)
                .thenReturn(0) // first iteration

                .thenReturn(20)
                .thenReturn(20) // second iteration

                .thenReturn(20)
                .thenReturn(40) // third iteration

                .thenReturn(20)
                .thenReturn(60) // fourth iteration

                .thenReturn(20)
                .thenReturn(80) // fifth iteration

                .thenReturn(200)
                .thenReturn(100); // sixth iteration, not enough tokens

        // When
        Map<String, Object> result = service.addMessagesToContext(context, reservedTokens, assetManipulated);

        // Then
        assertThat(result).isNotNull().isNotEmpty();
        assertThat((List<DiscordMessageData>) result.get("retrievedMessages"))
                .isNotNull()
                .hasSize(5);

        assertThat((List<DiscordMessageData>) result.get("messageHistory")).isNotNull()
                .isNotEmpty()
                .hasSize(expectedNumberOfMessagesAdded);
    }

    @Test
    public void addMessagesToContext_whenDefinedAssetUsesAllTokens_thenNoMessagesAreAdded() {

        // Given
        Map<String, Object> context = createContextWithMessageNumber(10);
        int expectedNumberOfMessagesNotAdded = 10;
        int reservedTokens = 250;
        String assetManipulated = "lorebook";

        context.put(assetManipulated, assetManipulated);

        when(tokenizerPort.getTokenCountFrom(anyString())).thenReturn(300);

        // When
        Map<String, Object> result = service.addMessagesToContext(context, reservedTokens, assetManipulated);

        // Then
        assertThat(result).isNotNull().isNotEmpty();
        assertThat((List<DiscordMessageData>) result.get("retrievedMessages"))
                .isNotNull()
                .hasSize(expectedNumberOfMessagesNotAdded);

        assertThat((List<DiscordMessageData>) result.get("messageHistory"))
                .isNotNull()
                .isEmpty();
    }

    @Test
    public void addMessagesToContext_whenEnoughTokens_thenMessagesAreAdded() {

        // Given
        Map<String, Object> context = createContextWithMessageNumber(10);
        int reservedTokens = 250;

        when(tokenizerPort.getTokenCountFrom(anyString())).thenReturn(20);

        // When
        Map<String, Object> result = service.addMessagesToContext(context, reservedTokens);

        // Then
        assertThat(result).isNotNull().isNotEmpty();
        assertThat((List<DiscordMessageData>) result.get("retrievedMessages"))
                .isNotNull()
                .isEmpty();

        assertThat((List<DiscordMessageData>) result.get("messageHistory")).isNotNull()
                .isNotEmpty()
                .hasSize(10);
    }

    @Test
    public void addMessagesToContext_whenNotEnoughTokens_thenLimitNumberOfMessagesAreAdded() {

        // Given
        Map<String, Object> context = createContextWithMessageNumber(10);
        int expectedNumberOfMessagesAdded = 5;
        int reservedTokens = 250;

        when(tokenizerPort.getTokenCountFrom(anyString()))
                .thenReturn(20)
                .thenReturn(0) // first iteration

                .thenReturn(20)
                .thenReturn(20) // second iteration

                .thenReturn(20)
                .thenReturn(40) // third iteration

                .thenReturn(20)
                .thenReturn(60) // fourth iteration

                .thenReturn(20)
                .thenReturn(80) // fifth iteration

                .thenReturn(200)
                .thenReturn(100); // sixth iteration, not enough tokens

        // When
        Map<String, Object> result = service.addMessagesToContext(context, reservedTokens);

        // Then
        assertThat(result).isNotNull().isNotEmpty();
        assertThat((List<DiscordMessageData>) result.get("retrievedMessages"))
                .isNotNull()
                .hasSize(5);

        assertThat((List<DiscordMessageData>) result.get("messageHistory")).isNotNull()
                .isNotEmpty()
                .hasSize(expectedNumberOfMessagesAdded);
    }

    @Test
    public void addMessagesToContext_whenSpecificAmountOfMessagesProvided_thenAddOnlyThoseMessages() {

        // Given
        Map<String, Object> context = createContextWithMessageNumber(10);
        int messagesToAdd = 7;
        int expectedNumberOfMessagesAdded = 7;
        int expectedNumberOfMessagesNotAdded = 3;
        int reservedTokens = 250;

        when(tokenizerPort.getTokenCountFrom(anyString())).thenReturn(20);

        // When
        Map<String, Object> result = service.addMessagesToContext(context, reservedTokens, messagesToAdd);

        // Then
        assertThat(result).isNotNull().isNotEmpty();
        assertThat((List<DiscordMessageData>) result.get("retrievedMessages"))
                .isNotNull()
                .hasSize(expectedNumberOfMessagesNotAdded);

        assertThat((List<DiscordMessageData>) result.get("messageHistory")).isNotNull()
                .isNotEmpty()
                .hasSize(expectedNumberOfMessagesAdded);
    }

    @Test
    public void addMessagesToContext_whenSpecificNumberOfMessagesButNotEnoughTokens_thenLimitNumberOfMessagesAreAdded() {

        // Given
        Map<String, Object> context = createContextWithMessageNumber(10);
        int messagesToAdd = 7;
        int expectedNumberOfMessagesAdded = 5;
        int expectedNumberOfMessagesNotAdded = 5;
        int reservedTokens = 250;

        when(tokenizerPort.getTokenCountFrom(anyString()))
                .thenReturn(20)
                .thenReturn(0) // first iteration

                .thenReturn(20)
                .thenReturn(20) // second iteration

                .thenReturn(20)
                .thenReturn(40) // third iteration

                .thenReturn(20)
                .thenReturn(60) // fourth iteration

                .thenReturn(20)
                .thenReturn(80) // fifth iteration

                .thenReturn(200)
                .thenReturn(100); // sixth iteration, not enough tokens

        // When
        Map<String, Object> result = service.addMessagesToContext(context, reservedTokens, messagesToAdd);

        // Then
        assertThat(result).isNotNull().isNotEmpty();
        assertThat((List<DiscordMessageData>) result.get("retrievedMessages"))
                .isNotNull()
                .hasSize(expectedNumberOfMessagesNotAdded);

        assertThat((List<DiscordMessageData>) result.get("messageHistory")).isNotNull()
                .isNotEmpty()
                .hasSize(expectedNumberOfMessagesAdded);
    }

    private Map<String, Object> createContextWithMessageNumber(int items) {

        List<DiscordMessageData> messageDataList = new ArrayList<>();
        for (int i = 0; i < items; i++) {
            int messageNumber = i + 1;
            messageDataList.add(DiscordMessageDataFixture.messageData()
                    .id(String.valueOf(messageNumber))
                    .content(String.format("Message %s", messageNumber))
                    .build());
        }

        Map<String, Object> context = new HashMap<>();
        context.put("retrievedMessages", messageDataList);

        return context;
    }
}
