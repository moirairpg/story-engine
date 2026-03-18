package me.moirai.storyengine.core.application.usecase.discord.messagereceived;

import static me.moirai.storyengine.core.application.usecase.discord.DiscordMessageDataFixture.messageList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
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

import me.moirai.storyengine.core.port.inbound.discord.DiscordMessageData;
import me.moirai.storyengine.core.application.usecase.discord.DiscordMessageDataFixture;
import me.moirai.storyengine.core.application.usecase.discord.DiscordUserDetailsFixture;
import me.moirai.storyengine.core.port.inbound.discord.messagereceived.RpgModeRequest;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.discord.DiscordChannelPort;
import me.moirai.storyengine.core.port.outbound.generation.StoryGenerationPort;
import me.moirai.storyengine.core.port.outbound.generation.StoryGenerationRequest;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;

@ExtendWith(MockitoExtension.class)
public class RpgModeHandlerTest {

    @Mock
    private StoryGenerationPort storyGenerationPort;

    @Mock
    private AdventureRepository adventureRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private DiscordChannelPort discordChannelPort;

    @InjectMocks
    private RpgModeHandler handler;

    @Test
    public void messageReceived_whenMessageIsReceived_thenGenerateOutput() {

        // Given
        var channelId = "CHID";

        var adventure = AdventureFixture.publicSingleplayerAdventure()
                .channelId(channelId)
                .build();

        var useCase = RpgModeRequest.builder()
                .authordDiscordId("John")
                .botUsername("TestBot")
                .isBotMentioned(false)
                .mentionedUsersIds(Collections.emptyList())
                .channelId(channelId)
                .guildId("GLDID")
                .messageId("MSGID")
                .build();

        var generationRequestCaptor = ArgumentCaptor.forClass(StoryGenerationRequest.class);

        var messageHistory = messageList(5);
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
        when(personaRepository.findById(anyLong())).thenReturn(Optional.of(PersonaFixture.publicPersonaWithId()));

        // When
        handler.execute(useCase);

        // Then
        verify(storyGenerationPort, times(1)).continueStory(generationRequestCaptor.capture());

        var generationRequest = generationRequestCaptor.getValue();
        assertThat(generationRequest).isNotNull();
        assertThat(generationRequest.getBotNickname()).isEqualTo(useCase.getBotNickname());
        assertThat(generationRequest.getBotUsername()).isEqualTo(useCase.getBotUsername());
        assertThat(generationRequest.getChannelId()).isEqualTo(useCase.getChannelId());
        assertThat(generationRequest.getGuildId()).isEqualTo(useCase.getGuildId());
        assertThat(generationRequest.getPersonaId()).isEqualTo(PersonaFixture.PUBLIC_ID);
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
