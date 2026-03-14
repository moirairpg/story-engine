package me.moirai.storyengine.core.application.usecase.discord.messagereceived;

import static me.moirai.storyengine.core.application.usecase.discord.DiscordMessageDataFixture.messageList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.application.helper.StoryGenerationHelper;
import me.moirai.storyengine.core.port.inbound.discord.DiscordMessageData;
import me.moirai.storyengine.core.application.usecase.discord.DiscordMessageDataFixture;
import me.moirai.storyengine.core.application.usecase.discord.DiscordUserDetailsFixture;
import me.moirai.storyengine.core.port.inbound.discord.messagereceived.AuthorModeRequest;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.DiscordChannelPort;
import me.moirai.storyengine.core.port.outbound.StoryGenerationRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class AuthorModeHandlerTest {

    @Mock
    private StoryGenerationHelper storyGenerationPort;

    @Mock
    private AdventureRepository adventureRepository;

    @Mock
    private DiscordChannelPort discordChannelPort;

    @InjectMocks
    private AuthorModeHandler handler;

    @Test
    public void messageReceived_whenMessageIsReceived_thenGenerateOutput() {

        // Given
        String channelId = "CHID";

        Adventure adventure = AdventureFixture.publicMultiplayerAdventure()
                .channelId(channelId)
                .build();

        AuthorModeRequest useCase = AuthorModeRequest.builder()
                .authordDiscordId("John")
                .botUsername("TestBot")
                .isBotMentioned(false)
                .mentionedUsersIds(Collections.emptyList())
                .channelId(channelId)
                .guildId("GLDID")
                .messageId("MSGID")
                .build();

        ArgumentCaptor<StoryGenerationRequest> generationRequestCaptor = ArgumentCaptor
                .forClass(StoryGenerationRequest.class);

        List<DiscordMessageData> messageHistory = messageList(5);
        messageHistory.add(DiscordMessageData.builder()
                .content("TestBot said: Bot message 1")
                .author(DiscordUserDetailsFixture.create()
                        .nickname("TestBot")
                        .username("TestBot")
                        .build())
                .build());

        messageHistory.add(DiscordMessageData.builder()
                .content("TestBot said: Bot message 2")
                .author(DiscordUserDetailsFixture.create()
                        .nickname("TestBot")
                        .username("TestBot")
                        .build())
                .build());

        when(adventureRepository.findByChannelId(anyString())).thenReturn(Optional.of(adventure));

        when(discordChannelPort.getLastMessageIn(anyString()))
                .thenReturn(Optional.of(DiscordMessageDataFixture.messageData().build()));

        when(discordChannelPort.retrieveEntireHistoryBefore(anyString(), anyString()))
                .thenReturn(messageHistory);

        // When
        Mono<Void> result = handler.execute(useCase);

        // Then
        StepVerifier.create(result).verifyComplete();

        verify(storyGenerationPort, times(1)).continueStory(generationRequestCaptor.capture());

        StoryGenerationRequest generationRequest = generationRequestCaptor.getValue();
        assertThat(generationRequest).isNotNull();
        assertThat(generationRequest.getBotNickname()).isEqualTo(useCase.getBotNickname());
        assertThat(generationRequest.getBotUsername()).isEqualTo(useCase.getBotUsername());
        assertThat(generationRequest.getChannelId()).isEqualTo(useCase.getChannelId());
        assertThat(generationRequest.getGuildId()).isEqualTo(useCase.getGuildId());
        assertThat(generationRequest.getPersonaId()).isEqualTo(adventure.getPersonaId());
        assertThat(generationRequest.getAdventureId()).isEqualTo(adventure.getId());
        assertThat(generationRequest.getMessageHistory())
                .isNotNull()
                .isNotEmpty()
                .hasSize(8)
                .extracting(DiscordMessageData::getContent)
                .containsAnyOf("TestBot said: Bot message 1",
                        "natalis said: [ Message 1 ]");
    }
}
