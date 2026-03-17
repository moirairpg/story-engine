package me.moirai.storyengine.core.application.usecase.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.world.GetWorldById;
import me.moirai.storyengine.core.port.inbound.world.WorldDetails;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@ExtendWith(MockitoExtension.class)
public class GetWorldByIdHandlerTest {

    @Mock
    private WorldRepository repository;

    @InjectMocks
    private GetWorldByIdHandler handler;

    @Test
    public void errorWhenQueryIsNull() {

        // Given
        GetWorldById query = null;

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(query));
    }

    @Test
    public void getWorldById() {

        // Given
        String requesterId = "84REAC";
        World world = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        ReflectionTestUtils.setField(world, "id", WorldFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(world, "publicId", WorldFixture.PUBLIC_ID);

        GetWorldById query = new GetWorldById(WorldFixture.PUBLIC_ID, requesterId);

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(world));

        // When
        WorldDetails result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(WorldFixture.PUBLIC_ID);
    }

    @Test
    public void updateWorld_whenIdIsNull_thenExceptionIsThrown() {

        // Given
        UUID id = null;
        String requesterId = "84REAC";
        GetWorldById command = new GetWorldById(id, requesterId);

        // Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void updateWorld_whenWorldNotFound_thenExceptionIsThrown() {

        // Given
        String requesterId = "84REAC";
        GetWorldById command = new GetWorldById(WorldFixture.PUBLIC_ID, requesterId);

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // Then
        assertThatExceptionOfType(AssetNotFoundException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void getWorldById_whenAccessDenied_thenThrowException() {

        // Given
        String requesterId = "RQSTRID";
        World world = WorldFixture.privateWorld()
                .build();

        GetWorldById query = new GetWorldById(WorldFixture.PUBLIC_ID, requesterId);

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(world));

        // When
        assertThrows(AssetAccessDeniedException.class, () -> handler.handle(query));
    }
}
