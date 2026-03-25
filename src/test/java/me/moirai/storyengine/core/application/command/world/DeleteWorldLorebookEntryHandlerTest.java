package me.moirai.storyengine.core.application.command.world;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.domain.world.WorldLorebookEntryFixture;
import me.moirai.storyengine.core.port.inbound.world.DeleteWorldLorebookEntry;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@ExtendWith(MockitoExtension.class)
public class DeleteWorldLorebookEntryHandlerTest {

    @Mock
    private WorldRepository repository;

    @InjectMocks
    private DeleteWorldLorebookEntryHandler handler;

    @Test
    public void errorWhenEntryIdIsNull() {

        // given
        var command = new DeleteWorldLorebookEntry(
                null,
                WorldFixture.PUBLIC_ID);

        // then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void errorWhenWorldIdIsNull() {

        // given
        var command = new DeleteWorldLorebookEntry(
                WorldLorebookEntryFixture.PUBLIC_ID,
                null);

        // then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void deleteWorld() {

        // given
        var command = new DeleteWorldLorebookEntry(
                WorldLorebookEntryFixture.PUBLIC_ID,
                WorldFixture.PUBLIC_ID);

        var baseWorld = WorldFixture.publicWorld().build();

        var world = spy(baseWorld);
        doNothing().when(world).removeLorebookEntry(any(UUID.class));

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(world));
        when(repository.save(any())).thenReturn(world);

        // when
        handler.handle(command);

        // then
        verify(repository, times(1)).save(any());
    }
}
