package me.moirai.storyengine.core.application.usecase.completion;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.util.Lists.list;
import static org.assertj.core.util.Maps.newHashMap;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.exception.AIModelNotSupportedException;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.exception.ModerationException;
import me.moirai.storyengine.core.application.helper.LorebookEnrichmentHelper;
import me.moirai.storyengine.core.application.model.result.TextGenerationResult;
import me.moirai.storyengine.core.application.model.result.TextGenerationResultFixture;
import me.moirai.storyengine.core.application.model.result.TextModerationResult;
import me.moirai.storyengine.core.application.model.result.TextModerationResultFixture;
import me.moirai.storyengine.core.application.usecase.completion.request.CompleteText;
import me.moirai.storyengine.core.application.usecase.completion.request.CompleteTextFixture;
import me.moirai.storyengine.core.application.usecase.completion.result.CompleteTextResult;
import me.moirai.storyengine.core.application.usecase.discord.DiscordUserDetails;
import me.moirai.storyengine.core.application.usecase.discord.DiscordUserDetailsFixture;
import me.moirai.storyengine.core.application.usecase.discord.slashcommands.TokenizeResult;
import me.moirai.storyengine.core.application.usecase.discord.slashcommands.TokenizeResultFixture;
import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.persona.PersonaRepository;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.domain.port.TokenizerPort;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldRepository;
import me.moirai.storyengine.core.port.DiscordUserDetailsPort;
import me.moirai.storyengine.core.port.TextCompletionPort;
import me.moirai.storyengine.core.port.TextModerationPort;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class CompleteTextHandlerTest {

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private WorldRepository worldRepository;

    @Mock
    private LorebookEnrichmentHelper lorebookEnrichmentHelper;

    @Mock
    private TextModerationPort textModerationPort;

    @Mock
    private TextCompletionPort textCompletionPort;

    @Mock
    private DiscordUserDetailsPort discordUserDetailsPort;

    @Mock
    private TokenizerPort tokenizerPort;

    @InjectMocks
    private CompleteTextHandler handler;

    @Test
    public void whenNoFlaggedContent_andModerationIsDisabled_thenResultIsReturned() {

        // Given
        CompleteText command = CompleteTextFixture.withModerationDisabled().build();
        Persona persona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(command.getRequesterDiscordId())
                        .build())
                .build();

        World world = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(command.getRequesterDiscordId())
                        .build())
                .build();

        DiscordUserDetails userDetails = DiscordUserDetailsFixture.create().build();
        TextGenerationResult textGenerationResult = TextGenerationResultFixture.create().build();
        TokenizeResult tokenizeResult = TokenizeResultFixture.create().build();
        TextModerationResult textModerationResult = TextModerationResultFixture.withoutFlags().build();

        when(personaRepository.findById(anyString())).thenReturn(Optional.of(persona));
        when(worldRepository.findById(anyString())).thenReturn(Optional.of(world));
        when(discordUserDetailsPort.getUserById(anyString())).thenReturn(Optional.of(userDetails));
        when(textCompletionPort.generateTextFrom(any())).thenReturn(Mono.just(textGenerationResult));
        when(tokenizerPort.tokenize(anyString())).thenReturn(tokenizeResult);
        when(textModerationPort.moderate(anyString())).thenReturn(Mono.just(textModerationResult));
        when(lorebookEnrichmentHelper.enrichContextWithLorebook(anyList(), anyString(), any()))
                .thenReturn(newHashMap("lorebook", "Some lorebook definition"));

        // When
        Mono<CompleteTextResult> result = handler.handle(command);

        // Then
        StepVerifier.create(result)
                .expectNextCount(1)
                .expectComplete()
                .verify();
    }

    @Test
    public void whenFlaggedContent_andModerationIsDisabled_thenResultIsReturned() {

        // Given
        CompleteText command = CompleteTextFixture.withModerationDisabled()
                .messages(list(CompleteTextFixture.botMessage().build()))
                .build();

        Persona persona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(command.getRequesterDiscordId())
                        .build())
                .build();

        World world = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(command.getRequesterDiscordId())
                        .build())
                .build();

        DiscordUserDetails userDetails = DiscordUserDetailsFixture.create().build();
        TextGenerationResult textGenerationResult = TextGenerationResultFixture.create().build();
        TokenizeResult tokenizeResult = TokenizeResultFixture.create().build();
        TextModerationResult textModerationResult = TextModerationResultFixture.withFlags().build();

        when(personaRepository.findById(anyString())).thenReturn(Optional.of(persona));
        when(worldRepository.findById(anyString())).thenReturn(Optional.of(world));
        when(discordUserDetailsPort.getUserById(anyString())).thenReturn(Optional.of(userDetails));
        when(textCompletionPort.generateTextFrom(any())).thenReturn(Mono.just(textGenerationResult));
        when(tokenizerPort.tokenize(anyString())).thenReturn(tokenizeResult);
        when(textModerationPort.moderate(anyString())).thenReturn(Mono.just(textModerationResult));

        // When
        Mono<CompleteTextResult> result = handler.handle(command);

        // Then
        StepVerifier.create(result)
                .expectNextCount(1)
                .expectComplete()
                .verify();
    }

    @Test
    public void whenNoFlaggedContent_andModerationIsStrict_thenResultIsReturned() {

        // Given
        CompleteText command = CompleteTextFixture.withStrictModeration().build();
        Persona persona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(command.getRequesterDiscordId())
                        .build())
                .build();

        World world = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(command.getRequesterDiscordId())
                        .build())
                .build();

        DiscordUserDetails userDetails = DiscordUserDetailsFixture.create().build();
        TextGenerationResult textGenerationResult = TextGenerationResultFixture.create().build();
        TokenizeResult tokenizeResult = TokenizeResultFixture.create().build();
        TextModerationResult textModerationResult = TextModerationResultFixture.withoutFlags().build();

        when(personaRepository.findById(anyString())).thenReturn(Optional.of(persona));
        when(worldRepository.findById(anyString())).thenReturn(Optional.of(world));
        when(discordUserDetailsPort.getUserById(anyString())).thenReturn(Optional.of(userDetails));
        when(textCompletionPort.generateTextFrom(any())).thenReturn(Mono.just(textGenerationResult));
        when(tokenizerPort.tokenize(anyString())).thenReturn(tokenizeResult);
        when(textModerationPort.moderate(anyString())).thenReturn(Mono.just(textModerationResult));

        // When
        Mono<CompleteTextResult> result = handler.handle(command);

        // Then
        StepVerifier.create(result)
                .expectNextCount(1)
                .expectComplete()
                .verify();
    }

    @Test
    public void whenNoFlaggedContent_andModerationIsPermissive_thenResultIsReturned() {

        // Given
        CompleteText command = CompleteTextFixture.withPermissiveModeration().build();
        Persona persona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(command.getRequesterDiscordId())
                        .build())
                .build();

        World world = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(command.getRequesterDiscordId())
                        .build())
                .build();

        DiscordUserDetails userDetails = DiscordUserDetailsFixture.create().build();
        TextGenerationResult textGenerationResult = TextGenerationResultFixture.create().build();
        TokenizeResult tokenizeResult = TokenizeResultFixture.create().build();
        TextModerationResult textModerationResult = TextModerationResultFixture.withoutFlags().build();

        when(personaRepository.findById(anyString())).thenReturn(Optional.of(persona));
        when(worldRepository.findById(anyString())).thenReturn(Optional.of(world));
        when(discordUserDetailsPort.getUserById(anyString())).thenReturn(Optional.of(userDetails));
        when(textCompletionPort.generateTextFrom(any())).thenReturn(Mono.just(textGenerationResult));
        when(tokenizerPort.tokenize(anyString())).thenReturn(tokenizeResult);
        when(textModerationPort.moderate(anyString())).thenReturn(Mono.just(textModerationResult));

        // When
        Mono<CompleteTextResult> result = handler.handle(command);

        // Then
        StepVerifier.create(result)
                .expectNextCount(1)
                .expectComplete()
                .verify();
    }

    @Test
    public void whenFlaggedContent_andModerationIsStrict_thenExceptionIsThrown() {

        // Given
        CompleteText command = CompleteTextFixture.withStrictModeration().build();
        Persona persona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(command.getRequesterDiscordId())
                        .build())
                .build();

        World world = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(command.getRequesterDiscordId())
                        .build())
                .build();

        DiscordUserDetails userDetails = DiscordUserDetailsFixture.create().build();
        TextModerationResult textModerationResult = TextModerationResultFixture.withFlags().build();

        when(personaRepository.findById(anyString())).thenReturn(Optional.of(persona));
        when(worldRepository.findById(anyString())).thenReturn(Optional.of(world));
        when(discordUserDetailsPort.getUserById(anyString())).thenReturn(Optional.of(userDetails));
        when(textModerationPort.moderate(anyString())).thenReturn(Mono.just(textModerationResult));

        // When
        Mono<CompleteTextResult> result = handler.handle(command);

        // Then
        StepVerifier.create(result)
                .verifyError(ModerationException.class);
    }

    @Test
    public void whenFlaggedContentInInput_andModerationIsPermissive_thenExceptionIsThrown() {

        // Given
        CompleteText command = CompleteTextFixture.withPermissiveModeration().build();
        Persona persona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(command.getRequesterDiscordId())
                        .build())
                .build();

        World world = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(command.getRequesterDiscordId())
                        .build())
                .build();

        DiscordUserDetails userDetails = DiscordUserDetailsFixture.create().build();
        TextModerationResult textModerationResult = TextModerationResultFixture.withFlags().build();

        when(personaRepository.findById(anyString())).thenReturn(Optional.of(persona));
        when(worldRepository.findById(anyString())).thenReturn(Optional.of(world));
        when(discordUserDetailsPort.getUserById(anyString())).thenReturn(Optional.of(userDetails));
        when(textModerationPort.moderate(anyString())).thenReturn(Mono.just(textModerationResult));

        // When
        Mono<CompleteTextResult> result = handler.handle(command);

        // Then
        StepVerifier.create(result)
                .verifyError(ModerationException.class);
    }

    @Test
    public void whenFlaggedContentInOutput_andModerationIsPermissive_thenExceptionIsThrown() {

        // Given
        CompleteText command = CompleteTextFixture.withPermissiveModeration().build();
        Persona persona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(command.getRequesterDiscordId())
                        .build())
                .build();

        World world = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(command.getRequesterDiscordId())
                        .build())
                .build();

        DiscordUserDetails userDetails = DiscordUserDetailsFixture.create().build();
        TextGenerationResult textGenerationResult = TextGenerationResultFixture.create().build();
        TextModerationResult badModerationResult = TextModerationResultFixture.withFlags().build();
        TextModerationResult goodModerationResult = TextModerationResultFixture.withoutFlags().build();

        when(personaRepository.findById(anyString())).thenReturn(Optional.of(persona));
        when(worldRepository.findById(anyString())).thenReturn(Optional.of(world));
        when(discordUserDetailsPort.getUserById(anyString())).thenReturn(Optional.of(userDetails));
        when(textCompletionPort.generateTextFrom(any())).thenReturn(Mono.just(textGenerationResult));
        when(textModerationPort.moderate(anyString()))
                .thenReturn(Mono.just(goodModerationResult))
                .thenReturn(Mono.just(badModerationResult));

        // When
        Mono<CompleteTextResult> result = handler.handle(command);

        // Then
        StepVerifier.create(result)
                .verifyError(ModerationException.class);
    }

    @Test
    public void whenInvalidModel_thenErrorIsThrown() {

        // Given
        CompleteText command = CompleteTextFixture.withPermissiveModeration()
                .aiModel("invalidModel")
                .build();

        // Then
        assertThrows(AIModelNotSupportedException.class,
                () -> handler.handle(command));
    }

    @Test
    public void whenPersonaNotFound_thenThrowException() {

        // Given
        CompleteText command = CompleteTextFixture.withModerationDisabled().build();

        when(personaRepository.findById(anyString())).thenReturn(Optional.empty());

        // Then
        assertThatExceptionOfType(AssetNotFoundException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void whenWorldNotFound_thenThrowException() {

        // Given
        CompleteText command = CompleteTextFixture.withModerationDisabled().build();

        when(personaRepository.findById(anyString())).thenReturn(Optional.of(mock(Persona.class)));
        when(worldRepository.findById(anyString())).thenReturn(Optional.empty());

        // Then
        assertThatExceptionOfType(AssetNotFoundException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void whenDiscordUserNotFound_thenThrowException() {

        // Given
        CompleteText command = CompleteTextFixture.withModerationDisabled().build();
        Persona persona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(command.getRequesterDiscordId())
                        .build())
                .build();

        World world = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(command.getRequesterDiscordId())
                        .build())
                .build();

        when(personaRepository.findById(anyString())).thenReturn(Optional.of(persona));
        when(worldRepository.findById(anyString())).thenReturn(Optional.of(world));
        when(discordUserDetailsPort.getUserById(anyString())).thenReturn(Optional.empty());

        // Then
        assertThatExceptionOfType(AssetNotFoundException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void whenPersonaAccessDenied_thenThrowException() {

        // Given
        CompleteText command = CompleteTextFixture.withModerationDisabled().build();
        Persona persona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId("someOtherUser")
                        .usersAllowedToRead(null)
                        .usersAllowedToWrite(null)
                        .build())
                .build();

        World world = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId("someOtherUser")
                        .usersAllowedToRead(null)
                        .usersAllowedToWrite(null)
                        .build())
                .build();

        when(personaRepository.findById(anyString())).thenReturn(Optional.of(persona));
        when(worldRepository.findById(anyString())).thenReturn(Optional.of(world));

        // When

        // Then
        assertThatExceptionOfType(AssetAccessDeniedException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void whenWorldAccessDenied_thenThrowException() {

        // Given
        CompleteText command = CompleteTextFixture.withModerationDisabled().build();
        Persona persona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(command.getRequesterDiscordId())
                        .build())
                .build();

        World world = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId("someOtherUser")
                        .usersAllowedToRead(null)
                        .usersAllowedToWrite(null)
                        .build())
                .build();

        when(personaRepository.findById(anyString())).thenReturn(Optional.of(persona));
        when(worldRepository.findById(anyString())).thenReturn(Optional.of(world));

        // When

        // Then
        assertThatExceptionOfType(AssetAccessDeniedException.class)
                .isThrownBy(() -> handler.handle(command));
    }
}
