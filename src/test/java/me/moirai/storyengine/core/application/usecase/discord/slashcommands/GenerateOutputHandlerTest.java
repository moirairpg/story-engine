package me.moirai.storyengine.core.application.usecase.discord.slashcommands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import me.moirai.storyengine.AbstractDiscordTest;
import me.moirai.storyengine.core.application.usecase.discord.DiscordMessageDataFixture;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.port.inbound.discord.slashcommands.GoCommand;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.discord.DiscordChannelPort;
import me.moirai.storyengine.core.port.outbound.generation.StoryGenerationPort;
import me.moirai.storyengine.core.port.outbound.generation.StoryGenerationRequest;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;

public class GenerateOutputHandlerTest extends AbstractDiscordTest {

    @Mock
    private AdventureRepository adventureRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private DiscordChannelPort discordChannelPort;

    @Mock
    private StoryGenerationPort storyGenerationPort;

    @InjectMocks
    private GoCommandHandler handler;

    @Test
    public void goCommand_whenIssued_thenGenerateOutput() {

        // Given
        var channelId = "CHID";

        var adventure = AdventureFixture.publicMultiplayerAdventure()
                .channelId(channelId)
                .build();

        var useCase = GoCommand.builder()
                .botId("BOTID")
                .botNickname("nickname")
                .botUsername("user.name")
                .channelId(channelId)
                .guildId("GDID")
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

    @Test
    public void goCommand_whenUnknownError_thenThrowException() {

        // Given
        var channelId = "CHID";

        var useCase = GoCommand.builder()
                .botId("BOTID")
                .botNickname("nickname")
                .botUsername("user.name")
                .channelId(channelId)
                .guildId("GDID")
                .build();

        when(adventureRepository.findByChannelId(anyString())).thenThrow(RuntimeException.class);
        when(discordChannelPort.retrieveEntireHistoryFrom(anyString()))
                .thenReturn(DiscordMessageDataFixture.messageList(5));

        // Then
        assertThatThrownBy(() -> handler.execute(useCase))
                .isInstanceOf(RuntimeException.class);
    }
}
