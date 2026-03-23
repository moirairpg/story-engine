package me.moirai.storyengine.core.application.usecase.discord.slashcommands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import me.moirai.storyengine.AbstractDiscordTest;
import me.moirai.storyengine.core.application.usecase.discord.DiscordUserDetailsFixture;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.port.inbound.discord.slashcommands.RetryCommand;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.discord.DiscordChannelPort;
import me.moirai.storyengine.core.port.outbound.discord.DiscordMessageData;
import me.moirai.storyengine.core.port.outbound.generation.StoryGenerationPort;
import me.moirai.storyengine.core.port.outbound.generation.StoryGenerationRequest;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;

public class RetryGenerationHandlerTest extends AbstractDiscordTest {

    @Mock
    private DiscordChannelPort discordChannelPort;

    @Mock
    private AdventureRepository adventureRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private StoryGenerationPort storyGenerationPort;

    @InjectMocks
    private RetryCommandHandler handler;

    @Test
    public void retryCommand_whenLastMessageElegible_thenShouldDeleteItAndRegenerateOutput() {

        // Given
        var botId = "BOTID";
        var channelId = "CHID";

        var adventure = AdventureFixture.publicMultiplayerAdventure()
                .channelId(channelId)
                .build();

        var useCase = RetryCommand.builder()
                .botId(botId)
                .botNickname("nickname")
                .botUsername("user.name")
                .channelId(channelId)
                .guildId("GDID")
                .build();

        var chatMessageData = new DiscordMessageData(null, null, "Some message",
                DiscordUserDetailsFixture.create()
                        .id(botId)
                        .build(),
                List.of());

        var generationRequestCaptor = ArgumentCaptor.forClass(StoryGenerationRequest.class);

        when(adventureRepository.findByChannelId(anyString())).thenReturn(Optional.of(adventure));
        when(discordChannelPort.getLastMessageIn(anyString())).thenReturn(Optional.of(chatMessageData));
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
    public void retryCommand_whenLastMessageAuthorIsNotBot_thenThrowException() {

        // Given
        var botId = "BOTID";
        var channelId = "CHID";
        var expectedErrorMessage = "This command can only be used if the last message in channel was sent by the bot.";

        var useCase = RetryCommand.builder()
                .botId(botId)
                .botNickname("nickname")
                .botUsername("user.name")
                .channelId(channelId)
                .guildId("GDID")
                .build();

        var chatMessageData = new DiscordMessageData(null, null, "Some message",
                DiscordUserDetailsFixture.create()
                        .id("SMID")
                        .build(),
                List.of());

        when(discordChannelPort.getLastMessageIn(anyString())).thenReturn(Optional.of(chatMessageData));

        // Then
        assertThatThrownBy(() -> handler.execute(useCase))
                .hasMessage(expectedErrorMessage);
    }

    @Test
    public void retryCommand_whenNoMessagesInChannel_thenThrowException() {

        // Given
        var botId = "BOTID";
        var channelId = "CHID";
        var expectedErrorMessage = "Channel has no messages";

        var useCase = RetryCommand.builder()
                .botId(botId)
                .botNickname("nickname")
                .botUsername("user.name")
                .channelId(channelId)
                .guildId("GDID")
                .build();

        when(discordChannelPort.getLastMessageIn(anyString())).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> handler.execute(useCase))
                .hasMessage(expectedErrorMessage);
    }

    @Test
    public void retryCommand_whenUnknownError_thenThrowException() {

        // Given
        var botId = "BOTID";
        var channelId = "CHID";

        var useCase = RetryCommand.builder()
                .botId(botId)
                .botNickname("nickname")
                .botUsername("user.name")
                .channelId(channelId)
                .guildId("GDID")
                .build();

        when(discordChannelPort.getLastMessageIn(anyString())).thenThrow(RuntimeException.class);

        // Then
        assertThatThrownBy(() -> handler.execute(useCase))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    public void retryCommand_whenRetrieveUserMessageAndChannelIsEmpty_thenThrowException() {

        // Given
        var botId = "BOTID";
        var channelId = "CHID";
        var expectedErrorMessage = "Channel has no messages";

        var adventure = AdventureFixture.publicMultiplayerAdventure()
                .channelId(channelId)
                .build();

        var useCase = RetryCommand.builder()
                .botId(botId)
                .botNickname("nickname")
                .botUsername("user.name")
                .channelId(channelId)
                .guildId("GDID")
                .build();

        var chatMessageData = new DiscordMessageData(null, null, "Some message",
                DiscordUserDetailsFixture.create()
                        .id(botId)
                        .build(),
                List.of());

        when(adventureRepository.findByChannelId(anyString())).thenReturn(Optional.of(adventure));
        when(discordChannelPort.getLastMessageIn(anyString()))
                .thenReturn(Optional.of(chatMessageData))
                .thenReturn(Optional.empty());
        when(personaRepository.findById(anyLong())).thenReturn(Optional.of(PersonaFixture.publicPersonaWithId()));

        // Then
        assertThatThrownBy(() -> handler.execute(useCase))
                .hasMessage(expectedErrorMessage);
    }

    @Test
    public void retryCommand_whenRetrieveLastMessageAndChannelIsEmpty_thenThrowException() {

        // Given
        var botId = "BOTID";
        var channelId = "CHID";
        var expectedErrorMessage = "Channel has no messages";

        var useCase = RetryCommand.builder()
                .botId(botId)
                .botNickname("nickname")
                .botUsername("user.name")
                .channelId(channelId)
                .guildId("GDID")
                .build();

        when(discordChannelPort.getLastMessageIn(anyString())).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> handler.execute(useCase))
                .hasMessage(expectedErrorMessage);
    }
}
