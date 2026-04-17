package me.moirai.storyengine.core.application.command.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
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
import me.moirai.storyengine.core.port.inbound.world.UpdateWorldLorebookEntry;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@ExtendWith(MockitoExtension.class)
public class UpdateWorldLorebookEntryHandlerTest {

    @Mock
    private WorldRepository repository;

    @InjectMocks
    private UpdateWorldLorebookEntryHandler handler;

    @Test
    public void updateWorld() {

        // given
        var command = new UpdateWorldLorebookEntry(
                WorldLorebookEntryFixture.PUBLIC_ID,
                WorldFixture.PUBLIC_ID,
                "MoirAI",
                "This is an RPG world");

        var expectedUpdatedEntry = WorldLorebookEntryFixture.sampleLorebookEntry().build();

        var baseWorld = WorldFixture.publicWorld().build();

        var world = spy(baseWorld);
        doReturn(expectedUpdatedEntry).when(world)
                .updateLorebookEntry(any(UUID.class), anyString(), anyString());

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(world));
        when(repository.save(any())).thenReturn(world);

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.lastUpdateDate()).isEqualTo(expectedUpdatedEntry.getLastUpdateDate());
    }
}
