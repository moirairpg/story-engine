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
import me.moirai.storyengine.core.port.inbound.discord.slashcommands.StartCommand;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.discord.DiscordChannelPort;
import me.moirai.storyengine.core.port.outbound.generation.StoryGenerationPort;
import me.moirai.storyengine.core.port.outbound.generation.StoryGenerationRequest;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

public class StartCommandHandlerTest extends AbstractDiscordTest {

    @Mock
    private AdventureRepository adventureRepository;

    @Mock
    private WorldRepository worldRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private StoryGenerationPort storyGenerationPort;

    @Mock
    private DiscordChannelPort discordChannelPort;

    @InjectMocks
    private StartCommandHandler handler;

    @Test
    public void startCommand_whenIssued_thenSendAdventureStartAndCallGeneration() {

        // Given
        var channelId = "CHID";

        var adventure = AdventureFixture.publicMultiplayerAdventure()
                .channelId(channelId)
                .build();

        var useCase = StartCommand.builder()
                .botId("BOTID")
                .botNickname("nickname")
                .botUsername("user.name")
                .channelId(channelId)
                .guildId("GDID")
                .build();

        var world = WorldFixture.privateWorld().build();

        var generationRequestCaptor = ArgumentCaptor.forClass(StoryGenerationRequest.class);

        when(adventureRepository.findByChannelId(anyString())).thenReturn(Optional.of(adventure));
        when(discordChannelPort.retrieveEntireHistoryFrom(anyString()))
                .thenReturn(DiscordMessageDataFixture.messageList(5));
        when(worldRepository.findById(anyLong())).thenReturn(Optional.of(world));
        when(personaRepository.findById(anyLong())).thenReturn(Optional.of(PersonaFixture.publicPersonaWithId()));

        // When
        handler.execute(useCase);

        // Then
        verify(storyGenerationPort, times(1)).continueStory(generationRequestCaptor.capture());

        var generationRequest = generationRequestCaptor.getValue();
        assertThat(generationRequest).isNotNull();
        assertThat(generationRequest.getBotId()).isEqualTo(useCase.getBotId());
        assertThat(generationRequest.getBotNickname()).isEqualTo(useCase.getBotNickname());
        assertThat(generationRequest.getBotUsername()).isEqualTo(useCase.getBotUsername());
        assertThat(generationRequest.getChannelId()).isEqualTo(useCase.getChannelId());
        assertThat(generationRequest.getGuildId()).isEqualTo(useCase.getGuildId());
        assertThat(generationRequest.getPersonaId()).isEqualTo(PersonaFixture.PUBLIC_ID);
        assertThat(generationRequest.getAdventureId()).isEqualTo(adventure.getId());
    }

    @Test
    public void startCommand_whenUnknownError_thenThrowException() {

        // Given
        var channelId = "CHID";

        var useCase = StartCommand.builder()
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

    @Test
    public void startCommand_whenAdventureHasNoWorld_thenThrowException() {

        // Given
        var channelId = "CHID";

        var useCase = StartCommand.builder()
                .botId("BOTID")
                .botNickname("nickname")
                .botUsername("user.name")
                .channelId(channelId)
                .guildId("GDID")
                .build();

        var adventure = AdventureFixture.publicMultiplayerAdventure()
                .channelId(channelId)
                .build();

        when(adventureRepository.findByChannelId(anyString())).thenReturn(Optional.of(adventure));
        when(discordChannelPort.retrieveEntireHistoryFrom(anyString()))
                .thenReturn(DiscordMessageDataFixture.messageList(5));
        when(worldRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> handler.execute(useCase))
                .hasMessage("Adventure has no world linked to it");
    }
}
