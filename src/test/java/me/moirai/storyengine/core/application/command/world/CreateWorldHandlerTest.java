package me.moirai.storyengine.core.application.command.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.CreateWorldFixture;
import me.moirai.storyengine.core.port.inbound.world.CreateWorld;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;

@ExtendWith(MockitoExtension.class)
public class CreateWorldHandlerTest {

    @Mock
    private WorldRepository repository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoragePort storagePort;

    @InjectMocks
    private CreateWorldHandler handler;

    @Test
    public void errorWhenCommandIsNull() {

        // Given
        CreateWorld command = null;

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void createWorld() {

        // Given
        var world = WorldFixture.privateWorld().build();
        ReflectionTestUtils.setField(world, "id", WorldFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(world, "publicId", WorldFixture.PUBLIC_ID);
        var command = CreateWorldFixture.createPrivateWorld();

        when(repository.save(any(World.class))).thenReturn(world);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(UserFixture.playerWithId()));

        // When
        var result = handler.handle(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(WorldFixture.PUBLIC_ID);
    }
}
