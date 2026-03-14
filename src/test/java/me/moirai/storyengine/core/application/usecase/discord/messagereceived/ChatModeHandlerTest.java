package me.moirai.storyengine.core.application.usecase.discord.messagereceived;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.application.helper.StoryGenerationHelper;
import me.moirai.storyengine.core.application.port.DiscordChannelPort;
import me.moirai.storyengine.core.application.usecase.discord.DiscordMessageDataFixture;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.adventure.AdventureRepository;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.StoryGenerationRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class ChatModeHandlerTest {

    @Mock
    private AdventureRepository adventureRepository;

    @Mock
    private DiscordChannelPort discordChannelPort;

    @Mock
    private StoryGenerationHelper storyGenerationPort;

    @InjectMocks
    private ChatModeHandler handler;

    @Test
    public void messageReceived_whenMessageIsReceived_thenGenerateOutput() {

        // Given
        String channelId = "CHID";

        Adventure adventure = AdventureFixture.publicMultiplayerAdventure()
                .channelId(channelId)
                .build();

        ChatModeRequest useCase = ChatModeRequest.builder()
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

        when(adventureRepository.findByChannelId(anyString())).thenReturn(Optional.of(adventure));

        when(discordChannelPort.getLastMessageIn(anyString()))
                .thenReturn(Optional.of(DiscordMessageDataFixture.messageData().build()));

        when(discordChannelPort.retrieveEntireHistoryBefore(anyString(), anyString()))
                .thenReturn(DiscordMessageDataFixture.messageList(5));

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
    }
}
