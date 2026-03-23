package me.moirai.storyengine.core.application.usecase.discord.messagereceived;

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

import me.moirai.storyengine.core.application.usecase.discord.DiscordMessageDataFixture;
import me.moirai.storyengine.core.port.inbound.discord.messagereceived.ChatModeRequest;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.discord.DiscordChannelPort;
import me.moirai.storyengine.core.port.outbound.generation.StoryGenerationPort;
import me.moirai.storyengine.core.port.outbound.generation.StoryGenerationRequest;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;

@ExtendWith(MockitoExtension.class)
public class ChatModeHandlerTest {

    @Mock
    private AdventureRepository adventureRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private DiscordChannelPort discordChannelPort;

    @Mock
    private StoryGenerationPort storyGenerationPort;

    @InjectMocks
    private ChatModeHandler handler;

    @Test
    public void messageReceived_whenMessageIsReceived_thenGenerateOutput() {

        // Given
        var channelId = "CHID";

        var adventure = AdventureFixture.publicMultiplayerAdventure()
                .channelId(channelId)
                .build();

        var useCase = ChatModeRequest.builder()
                .authordDiscordId("John")
                .botUsername("TestBot")
                .isBotMentioned(false)
                .mentionedUsersIds(Collections.emptyList())
                .channelId(channelId)
                .guildId("GLDID")
                .messageId("MSGID")
                .build();

        var generationRequestCaptor = ArgumentCaptor.forClass(StoryGenerationRequest.class);

        when(adventureRepository.findByChannelId(anyString())).thenReturn(Optional.of(adventure));
        when(discordChannelPort.getLastMessageIn(anyString()))
                .thenReturn(Optional.of(DiscordMessageDataFixture.messageData()));
        when(discordChannelPort.retrieveEntireHistoryBefore(anyString(), anyString()))
                .thenReturn(DiscordMessageDataFixture.messageList(5));
        when(personaRepository.findById(anyLong())).thenReturn(Optional.of(PersonaFixture.publicPersonaWithId()));

        // When
        handler.execute(useCase);

        // Then
        verify(storyGenerationPort, times(1)).continueStory(generationRequestCaptor.capture());

        var generationRequest = generationRequestCaptor.getValue();
        assertThat(generationRequest).isNotNull();
        assertThat(generationRequest.botNickname()).isEqualTo(useCase.getBotNickname());
        assertThat(generationRequest.botUsername()).isEqualTo(useCase.getBotUsername());
        assertThat(generationRequest.channelId()).isEqualTo(useCase.getChannelId());
        assertThat(generationRequest.guildId()).isEqualTo(useCase.getGuildId());
        assertThat(generationRequest.personaId()).isEqualTo(PersonaFixture.PUBLIC_ID);
        assertThat(generationRequest.adventureId()).isEqualTo(adventure.getId());
    }
}
