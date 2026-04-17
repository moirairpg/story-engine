package me.moirai.storyengine.core.application.command.world;

import static org.assertj.core.api.Assertions.assertThat;
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

import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.world.CreateWorldLorebookEntry;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@ExtendWith(MockitoExtension.class)
public class CreateWorldLorebookEntryHandlerTest {

    @Mock
    private WorldRepository repository;

    @InjectMocks
    private CreateWorldLorebookEntryHandler handler;

    @Test
    public void errorWhenCommandIsNull() {

        // given
        CreateWorldLorebookEntry command = null;

        // then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void errorWhenWorldIdIsNull() {

        // given
        var command = new CreateWorldLorebookEntry(
                null,
                "Volin Habar",
                "Volin Habar is a warrior that fights with a sword.");

        // then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void errorWhenNameIsNull() {

        // given
        var command = new CreateWorldLorebookEntry(
                WorldFixture.PUBLIC_ID,
                null,
                "Volin Habar is a warrior that fights with a sword.");

        // then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void errorWhenDescriptionIsNull() {

        // given
        var command = new CreateWorldLorebookEntry(
                WorldFixture.PUBLIC_ID,
                "Volin Habar",
                null);

        // then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void createWorldLorebookEntry() {

        // given
        var command = new CreateWorldLorebookEntry(
                WorldFixture.PUBLIC_ID,
                "Volin Habar",
                "Volin Habar is a warrior that fights with a sword.");

        var world = WorldFixture.privateWorld().build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(world));
        when(repository.save(any())).thenReturn(world);

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(command.name());
    }
}
