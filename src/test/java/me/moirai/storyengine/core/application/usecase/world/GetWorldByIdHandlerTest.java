package me.moirai.storyengine.core.application.usecase.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.port.inbound.world.GetWorldById;
import me.moirai.storyengine.core.port.inbound.world.GetWorldResult;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;
import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;

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
        String id = "HAUDHUAHD";
        String requesterId = "84REAC";
        World world = WorldFixture.privateWorld()
                .id(id)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        GetWorldById query = GetWorldById.build(id, requesterId);

        when(repository.findById(anyString())).thenReturn(Optional.of(world));

        // When
        GetWorldResult result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    public void updateWorld_whenIdIsNull_thenExceptionIsThrown() {

        // Given
        String id = null;
        String requesterId = "84REAC";
        GetWorldById command = GetWorldById.build(id, requesterId);

        // Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void updateWorld_whenWorldNotFound_thenExceptionIsThrown() {

        // Given
        String id = "WRLDID";
        String requesterId = "84REAC";
        GetWorldById command = GetWorldById.build(id, requesterId);

        when(repository.findById(anyString())).thenReturn(Optional.empty());

        // Then
        assertThatExceptionOfType(AssetNotFoundException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void getWorldById_whenAccessDenied_thenThrowException() {

        // Given
        String id = "HAUDHUAHD";
        String requesterId = "RQSTRID";
        World world = WorldFixture.privateWorld()
                .id(id)
                .build();

        GetWorldById query = GetWorldById.build(id, requesterId);

        when(repository.findById(anyString())).thenReturn(Optional.of(world));

        // When
        assertThrows(AssetAccessDeniedException.class, () -> handler.handle(query));
    }
}
