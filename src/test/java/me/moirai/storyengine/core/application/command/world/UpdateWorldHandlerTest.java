package me.moirai.storyengine.core.application.command.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.exception.ModerationException;
import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.world.UpdateWorld;
import me.moirai.storyengine.core.port.outbound.generation.TextModerationPort;
import me.moirai.storyengine.core.port.outbound.generation.TextModerationResultFixture;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class UpdateWorldHandlerTest {

    @Mock
    private WorldRepository repository;

    @Mock
    private TextModerationPort moderationPort;

    @InjectMocks
    private UpdateWorldHandler handler;

    @Test
    public void updateWorld_whenFieldsAreProvided_thenUpdateWorld() {

        // Given
        UUID id = WorldFixture.PUBLIC_ID;
        String requesterId = "RQSTRID";
        String newName = "NEW NAME";
        UpdateWorld command = new UpdateWorld(
                id,
                "MoirAI",
                "This is an RPG world",
                "As you enter the city, people around you start looking at you.",
                Visibility.PUBLIC,
                requesterId,
                null,
                null,
                null,
                null);

        World expectedUpdatedWorld = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        World unchangedWorld = WorldFixture.privateWorld()
                .name(newName)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedWorld));
        when(repository.save(any(World.class))).thenReturn(expectedUpdatedWorld);
        when(moderationPort.moderate(anyString()))
                .thenReturn(Mono.just(TextModerationResultFixture.withoutFlags().build()));

        // Then
        StepVerifier.create(handler.handle(command))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.lastUpdateDate()).isEqualTo(expectedUpdatedWorld.getLastUpdateDate());
                })
                .verifyComplete();
    }

    @Test
    public void updateWorld_whenValidData_thenWorldIsUpdated() {

        // Given
        UUID id = WorldFixture.PUBLIC_ID;
        String requesterId = "RQSTRID";
        UpdateWorld command = new UpdateWorld(
                id,
                "MoirAI",
                "This is an RPG world",
                "As you enter the city, people around you start looking at you.",
                Visibility.PUBLIC,
                requesterId,
                null,
                null,
                null,
                null);

        World unchangedWorld = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        World expectedUpdatedWorld = WorldFixture.privateWorld()
                .name("MoirAI")
                .description("This is an RPG world")
                .adventureStart("As you enter the city, people around you start looking at you.")
                .visibility(Visibility.PUBLIC)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        when(moderationPort.moderate(anyString()))
                .thenReturn(Mono.just(TextModerationResultFixture.withoutFlags().build()));

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedWorld));
        when(repository.save(any(World.class))).thenReturn(expectedUpdatedWorld);
        when(moderationPort.moderate(anyString()))
                .thenReturn(Mono.just(TextModerationResultFixture.withoutFlags().build()));

        // Then
        StepVerifier.create(handler.handle(command))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                }).verifyComplete();
    }

    @Test
    public void updateWorld_whenEmptyUpdateFields_thenWorldIsNotChanged() {

        // Given
        UUID id = WorldFixture.PUBLIC_ID;
        String requesterId = "RQSTRID";
        UpdateWorld command = new UpdateWorld(
                id,
                null,
                null,
                null,
                null,
                requesterId,
                null,
                null,
                null,
                null);

        World unchangedWorld = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedWorld));
        when(repository.save(any(World.class))).thenReturn(unchangedWorld);

        // Then
        StepVerifier.create(handler.handle(command))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                }).verifyComplete();
    }

    @Test
    public void updateWorld_whenPublicToBeMadePrivate_thenWorldIsMadePrivate() {

        // Given
        UUID id = WorldFixture.PUBLIC_ID;
        String requesterId = "RQSTRID";
        UpdateWorld command = new UpdateWorld(
                id,
                null,
                null,
                null,
                Visibility.PRIVATE,
                requesterId,
                null,
                null,
                null,
                null);

        World unchangedWorld = WorldFixture.publicWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        World expectedWorld = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedWorld));
        when(repository.save(any(World.class))).thenReturn(expectedWorld);

        // Then
        StepVerifier.create(handler.handle(command))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                }).verifyComplete();
    }

    @Test
    public void updateWorld_whenInvalidVisibility_thenNothingIsChanged() {

        // Given
        UUID id = WorldFixture.PUBLIC_ID;
        String requesterId = "RQSTRID";
        UpdateWorld command = new UpdateWorld(
                id,
                null,
                null,
                null,
                null,
                requesterId,
                null,
                null,
                null,
                null);

        World unchangedWorld = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedWorld));
        when(repository.save(any(World.class))).thenReturn(unchangedWorld);

        // Then
        StepVerifier.create(handler.handle(command))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                }).verifyComplete();
    }

    @Test
    public void updateWorld_whenAccessDenied_thenExceptionIsThrown() {

        // Given
        UUID id = WorldFixture.PUBLIC_ID;
        String requesterId = "RQSTRID";

        UpdateWorld command = new UpdateWorld(
                id,
                "MoirAI",
                "This is an RPG world",
                "As you enter the city, people around you start looking at you.",
                Visibility.PUBLIC,
                requesterId,
                null,
                null,
                null,
                null);

        World unchangedWorld = WorldFixture.privateWorld()
                .name("NEW NAME")
                .build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedWorld));
        when(moderationPort.moderate(anyString()))
                .thenReturn(Mono.just(TextModerationResultFixture.withoutFlags().build()));

        // Then
        StepVerifier.create(handler.handle(command))
                .verifyError(me.moirai.storyengine.common.exception.AssetAccessDeniedException.class);
    }

    @Test
    public void updateWorld_whenIdIsNull_thenExceptionIsThrown() {

        // Given
        UpdateWorld command = new UpdateWorld(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        // Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void updateWorld_whenContentIsFlagged_thenExceptionIsThrown() {

        // Given
        UpdateWorld command = new UpdateWorld(
                WorldFixture.PUBLIC_ID,
                "MoirAI",
                "This is an RPG world",
                "As you enter the city, people around you start looking at you.",
                Visibility.PUBLIC,
                null,
                null,
                null,
                null,
                null);

        when(moderationPort.moderate(anyString()))
                .thenReturn(Mono.just(TextModerationResultFixture.withFlags().build()));

        // Then
        StepVerifier.create(handler.handle(command))
                .verifyError(ModerationException.class);
    }

    @Test
    public void updateWorld_whenWorldNotFound_thenExceptionIsThrown() {

        // Given
        UpdateWorld command = new UpdateWorld(
                WorldFixture.PUBLIC_ID,
                "SomeNewName",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());
        when(moderationPort.moderate(anyString()))
                .thenReturn(Mono.just(TextModerationResultFixture.withoutFlags().build()));

        // Then
        StepVerifier.create(handler.handle(command))
                .verifyError(AssetNotFoundException.class);
    }
}
