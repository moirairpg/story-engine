package me.moirai.storyengine.core.application.usecase.discord.slashcommands;

import static org.assertj.core.api.Assertions.assertThat;
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
import me.moirai.storyengine.core.application.port.StoryGenerationPort;
import me.moirai.storyengine.core.application.usecase.discord.DiscordMessageDataFixture;
import me.moirai.storyengine.core.port.inbound.discord.slashcommands.StartCommand;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.adventure.AdventureRepository;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldRepository;
import me.moirai.storyengine.core.port.DiscordChannelPort;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.outbound.StoryGenerationRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class StartCommandHandlerTest extends AbstractDiscordTest {

    @Mock
    private AdventureRepository adventureRepository;

    @Mock
    private WorldRepository worldRepository;

    @Mock
    private StoryGenerationPort storyGenerationPort;

    @Mock
    private DiscordChannelPort discordChannelPort;

    @InjectMocks
    private StartCommandHandler handler;

    @Test
    public void startCommand_whenIssued_thenSendAdventureStartAndCallGeneration() {

        // Given
        String channelId = "CHID";

        Adventure adventure = AdventureFixture.publicMultiplayerAdventure()
                .channelId(channelId)
                .build();

        StartCommand useCase = StartCommand.builder()
                .botId("BOTID")
                .botNickname("nickname")
                .botUsername("user.name")
                .channelId(channelId)
                .guildId("GDID")
                .build();

        World world = WorldFixture.privateWorld().build();

        ArgumentCaptor<StoryGenerationRequest> generationRequestCaptor = ArgumentCaptor
                .forClass(StoryGenerationRequest.class);

        when(adventureRepository.findByChannelId(anyString())).thenReturn(Optional.of(adventure));

        when(discordChannelPort.retrieveEntireHistoryFrom(anyString()))
                .thenReturn(DiscordMessageDataFixture.messageList(5));

        when(worldRepository.findById(anyString())).thenReturn(Optional.of(world));

        // When
        Mono<Void> result = handler.execute(useCase);

        // Then
        StepVerifier.create(result).verifyComplete();

        verify(storyGenerationPort, times(1)).continueStory(generationRequestCaptor.capture());

        StoryGenerationRequest generationRequest = generationRequestCaptor.getValue();
        assertThat(generationRequest).isNotNull();
        assertThat(generationRequest.getBotId()).isEqualTo(useCase.getBotId());
        assertThat(generationRequest.getBotNickname()).isEqualTo(useCase.getBotNickname());
        assertThat(generationRequest.getBotUsername()).isEqualTo(useCase.getBotUsername());
        assertThat(generationRequest.getChannelId()).isEqualTo(useCase.getChannelId());
        assertThat(generationRequest.getGuildId()).isEqualTo(useCase.getGuildId());
        assertThat(generationRequest.getPersonaId()).isEqualTo(adventure.getPersonaId());
        assertThat(generationRequest.getAdventureId()).isEqualTo(adventure.getId());
    }

    @Test
    public void startCommand_whenUnknownError_thenThrowException() {

        // Given
        String channelId = "CHID";

        StartCommand useCase = StartCommand.builder()
                .botId("BOTID")
                .botNickname("nickname")
                .botUsername("user.name")
                .channelId(channelId)
                .guildId("GDID")
                .build();

        when(adventureRepository.findByChannelId(anyString())).thenThrow(RuntimeException.class);

        when(discordChannelPort.retrieveEntireHistoryFrom(anyString()))
                .thenReturn(DiscordMessageDataFixture.messageList(5));

        // When
        Mono<Void> result = handler.execute(useCase);

        // Then
        StepVerifier.create(result).verifyErrorMessage("An error occurred while generating output");
    }

    @Test
    public void startCommand_whenAdventureHasNoWorld_thenThrowException() {

        // Given
        String channelId = "CHID";
        String expectedErrorMessage = "Adventure has no world linked to it";

        StartCommand useCase = StartCommand.builder()
                .botId("BOTID")
                .botNickname("nickname")
                .botUsername("user.name")
                .channelId(channelId)
                .guildId("GDID")
                .build();

        Adventure adventure = AdventureFixture.publicMultiplayerAdventure()
                .channelId(channelId)
                .build();

        when(adventureRepository.findByChannelId(anyString())).thenReturn(Optional.of(adventure));

        when(discordChannelPort.retrieveEntireHistoryFrom(anyString()))
                .thenReturn(DiscordMessageDataFixture.messageList(5));

        when(worldRepository.findById(anyString())).thenReturn(Optional.empty());

        // When
        Mono<Void> result = handler.execute(useCase);

        // Then
        StepVerifier.create(result).verifyErrorMessage(expectedErrorMessage);
    }
}
