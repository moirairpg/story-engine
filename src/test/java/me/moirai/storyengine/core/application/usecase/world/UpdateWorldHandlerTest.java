package me.moirai.storyengine.core.application.usecase.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.domain.Visibility;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.exception.ModerationException;
import me.moirai.storyengine.core.application.model.result.TextModerationResultFixture;
import me.moirai.storyengine.core.port.inbound.world.UpdateWorld;
import me.moirai.storyengine.core.port.outbound.generation.TextModerationPort;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;
import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;
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
        String id = "WRDID";
        String requesterId = "RQSTRID";
        String newName = "NEW NAME";
        UpdateWorld command = UpdateWorld.builder()
                .id(id)
                .name("MoirAI")
                .description("This is an RPG world")
                .adventureStart("As you enter the city, people around you start looking at you.")
                .visibility("PUBLIC")
                .requesterId(requesterId)
                .build();

        World expectedUpdatedWorld = WorldFixture.privateWorld()
                .id(id)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        World unchangedWorld = WorldFixture.privateWorld()
                .id(id)
                .name(newName)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        when(repository.findById(anyString())).thenReturn(Optional.of(unchangedWorld));
        when(repository.save(any(World.class))).thenReturn(expectedUpdatedWorld);
        when(moderationPort.moderate(anyString()))
                .thenReturn(Mono.just(TextModerationResultFixture.withoutFlags().build()));

        // Then
        StepVerifier.create(handler.handle(command))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getLastUpdateDate()).isEqualTo(expectedUpdatedWorld.getLastUpdateDate());
                })
                .verifyComplete();
    }

    @Test
    public void updateWorld_whenValidData_thenWorldIsUpdated() {

        // Given
        String id = "WRLDID";
        String requesterId = "RQSTRID";
        UpdateWorld command = UpdateWorld.builder()
                .id(id)
                .name("MoirAI")
                .description("This is an RPG world")
                .adventureStart("As you enter the city, people around you start looking at you.")
                .visibility("PUBLIC")
                .requesterId(requesterId)
                .build();

        World unchangedWorld = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        World expectedUpdatedWorld = WorldFixture.privateWorld()
                .id(id)
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

        when(repository.findById(anyString())).thenReturn(Optional.of(unchangedWorld));
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
        String id = "WRLDID";
        String requesterId = "RQSTRID";
        UpdateWorld command = UpdateWorld.builder()
                .id(id)
                .name(null)
                .description(null)
                .adventureStart(null)
                .visibility(null)
                .requesterId(requesterId)
                .build();

        World unchangedWorld = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        when(repository.findById(anyString())).thenReturn(Optional.of(unchangedWorld));
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
        String id = "WRLDID";
        String requesterId = "RQSTRID";
        UpdateWorld command = UpdateWorld.builder()
                .id(id)
                .visibility("private")
                .requesterId(requesterId)
                .build();

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

        when(repository.findById(anyString())).thenReturn(Optional.of(unchangedWorld));
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
        String id = "WRLDID";
        String requesterId = "RQSTRID";
        UpdateWorld command = UpdateWorld.builder()
                .id(id)
                .visibility("invalid")
                .requesterId(requesterId)
                .build();

        World unchangedWorld = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        when(repository.findById(anyString())).thenReturn(Optional.of(unchangedWorld));
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
        String id = "WRDID";
        String requesterId = "RQSTRID";
        String newName = "NEW NAME";

        UpdateWorld command = UpdateWorld.builder()
                .id(id)
                .name("MoirAI")
                .description("This is an RPG world")
                .adventureStart("As you enter the city, people around you start looking at you.")
                .visibility("PUBLIC")
                .requesterId(requesterId)
                .build();

        World unchangedWorld = WorldFixture.privateWorld()
                .id(id)
                .name(newName)
                .build();

        when(repository.findById(anyString())).thenReturn(Optional.of(unchangedWorld));
        when(moderationPort.moderate(anyString()))
                .thenReturn(Mono.just(TextModerationResultFixture.withoutFlags().build()));

        // Then
        StepVerifier.create(handler.handle(command))
                .verifyError(me.moirai.storyengine.common.exception.AssetAccessDeniedException.class);
    }

    @Test
    public void updateWorld_whenIdIsNull_thenExceptionIsThrown() {

        // Given
        String id = null;
        UpdateWorld command = UpdateWorld.builder()
                .id(id)
                .build();

        // Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void updateWorld_whenContentIsFlagged_thenExceptionIsThrown() {

        // Given
        String id = "WRLDID";
        UpdateWorld command = UpdateWorld.builder()
                .id(id)
                .name("MoirAI")
                .description("This is an RPG world")
                .adventureStart("As you enter the city, people around you start looking at you.")
                .visibility("PUBLIC")
                .build();

        when(moderationPort.moderate(anyString()))
                .thenReturn(Mono.just(TextModerationResultFixture.withFlags().build()));

        // Then
        StepVerifier.create(handler.handle(command))
                .verifyError(ModerationException.class);
    }

    @Test
    public void updateWorld_whenWorldNotFound_thenExceptionIsThrown() {

        // Given
        String id = "WRLDID";
        UpdateWorld command = UpdateWorld.builder()
                .id(id)
                .name("SomeNewName")
                .build();

        when(repository.findById(anyString())).thenReturn(Optional.empty());
        when(moderationPort.moderate(anyString()))
                .thenReturn(Mono.just(TextModerationResultFixture.withoutFlags().build()));

        // Then
        StepVerifier.create(handler.handle(command))
                .verifyError(AssetNotFoundException.class);
    }
}
