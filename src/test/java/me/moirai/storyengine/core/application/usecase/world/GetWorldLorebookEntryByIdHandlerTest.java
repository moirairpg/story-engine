package me.moirai.storyengine.core.application.usecase.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.domain.world.WorldLorebookEntry;
import me.moirai.storyengine.core.domain.world.WorldLorebookEntryFixture;
import me.moirai.storyengine.core.port.inbound.world.GetWorldLorebookEntryById;
import me.moirai.storyengine.core.port.inbound.world.WorldLorebookEntryDetails;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@ExtendWith(MockitoExtension.class)
public class GetWorldLorebookEntryByIdHandlerTest {

    @Mock
    private WorldRepository repository;

    @InjectMocks
    private GetWorldLorebookEntryByIdHandler handler;

    @Test
    public void errorWhenQueryIsNull() {

        // Given
        GetWorldLorebookEntryById query = null;

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(query));
    }

    @Test
    public void errorWhenEntryIdIsNull() {

        // Given
        GetWorldLorebookEntryById query = new GetWorldLorebookEntryById(
                null,
                WorldFixture.PUBLIC_ID,
                null);

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(query));
    }

    @Test
    public void errorWhenWorldIdIsNull() {

        // Given
        GetWorldLorebookEntryById query = new GetWorldLorebookEntryById(
                WorldLorebookEntryFixture.PUBLIC_ID,
                null,
                null);

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(query));
    }

    @Test
    public void getWorldLorebookEntryById() {

        // Given
        UUID publicId = WorldLorebookEntryFixture.PUBLIC_ID;
        UUID worldId = WorldFixture.PUBLIC_ID;
        String requesterId = "4314324";

        WorldLorebookEntry entry = WorldLorebookEntryFixture.sampleLorebookEntry().build();
        ReflectionTestUtils.setField(entry, "id", WorldLorebookEntryFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(entry, "publicId", publicId);

        World baseWorld = WorldFixture.publicWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        World world = spy(baseWorld);
        doReturn(entry).when(world).getLorebookEntryById(any(UUID.class));

        GetWorldLorebookEntryById query = new GetWorldLorebookEntryById(
                publicId,
                worldId,
                requesterId);

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(world));

        // When
        WorldLorebookEntryDetails result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(entry.getPublicId());
    }
}
