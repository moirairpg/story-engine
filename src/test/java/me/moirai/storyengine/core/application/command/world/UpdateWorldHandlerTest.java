package me.moirai.storyengine.core.application.command.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.world.UpdateWorld;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;

@ExtendWith(MockitoExtension.class)
public class UpdateWorldHandlerTest {

    @Mock
    private WorldRepository repository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoragePort storagePort;

    @InjectMocks
    private UpdateWorldHandler handler;

    @Test
    public void updateWorld_whenFieldsAreProvided_thenUpdateWorld() {

        // given
        var id = WorldFixture.PUBLIC_ID;
        var newName = "NEW NAME";
        var command = new UpdateWorld(
                id,
                "MoirAI",
                "This is an RPG world",
                "As you enter the city, people around you start looking at you.",
                null,
                null,
                Visibility.PUBLIC,
                Set.of(),
                List.of(),
                List.of(),
                List.of());

        var expectedUpdatedWorld = WorldFixture.privateWorld().build();
        var unchangedWorld = WorldFixture.privateWorld().name(newName).build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedWorld));
        when(repository.save(any(World.class))).thenReturn(expectedUpdatedWorld);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.lastUpdateDate()).isEqualTo(expectedUpdatedWorld.getLastUpdateDate());
    }

    @Test
    public void updateWorld_whenValidData_thenWorldIsUpdated() {

        // given
        var id = WorldFixture.PUBLIC_ID;
        var command = new UpdateWorld(
                id,
                "MoirAI",
                "This is an RPG world",
                "As you enter the city, people around you start looking at you.",
                null,
                null,
                Visibility.PUBLIC,
                Set.of(),
                List.of(),
                List.of(),
                List.of());

        var unchangedWorld = WorldFixture.privateWorld().build();
        var expectedUpdatedWorld = WorldFixture.privateWorld()
                .name("MoirAI")
                .description("This is an RPG world")
                .adventureStart("As you enter the city, people around you start looking at you.")
                .visibility(Visibility.PUBLIC)
                .build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedWorld));
        when(repository.save(any(World.class))).thenReturn(expectedUpdatedWorld);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    public void updateWorld_whenEmptyUpdateFields_thenWorldIsNotChanged() {

        // given
        var id = WorldFixture.PUBLIC_ID;
        var command = new UpdateWorld(
                id,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(),
                List.of(),
                List.of());

        var unchangedWorld = WorldFixture.privateWorld().build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedWorld));
        when(repository.save(any(World.class))).thenReturn(unchangedWorld);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    public void updateWorld_whenPublicToBeMadePrivate_thenWorldIsMadePrivate() {

        // given
        var id = WorldFixture.PUBLIC_ID;
        var command = new UpdateWorld(
                id,
                null,
                null,
                null,
                null,
                null,
                Visibility.PRIVATE,
                null,
                List.of(),
                List.of(),
                List.of());

        var unchangedWorld = WorldFixture.publicWorld().build();
        var expectedWorld = WorldFixture.privateWorld().build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedWorld));
        when(repository.save(any(World.class))).thenReturn(expectedWorld);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    public void updateWorld_whenInvalidVisibility_thenNothingIsChanged() {

        // given
        var id = WorldFixture.PUBLIC_ID;
        var command = new UpdateWorld(
                id,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(),
                List.of(),
                List.of());

        var unchangedWorld = WorldFixture.privateWorld().build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedWorld));
        when(repository.save(any(World.class))).thenReturn(unchangedWorld);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    public void updateWorld_whenIdIsNull_thenExceptionIsThrown() {

        // given
        var command = new UpdateWorld(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(),
                List.of(),
                List.of());

        // then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void updateWorld_whenWorldNotFound_thenExceptionIsThrown() {

        // given
        var command = new UpdateWorld(
                WorldFixture.PUBLIC_ID,
                "SomeNewName",
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(),
                List.of(),
                List.of());

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // then
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void updateWorld_whenLorebookEntriesToAdd_thenEntriesAreAdded() {

        // given
        var command = new UpdateWorld(
                WorldFixture.PUBLIC_ID,
                "MoirAI",
                "This is an RPG world",
                "As you enter the city, people around you start looking at you.",
                null,
                null,
                Visibility.PUBLIC,
                Set.of(),
                List.of(new UpdateWorld.LorebookEntryToAdd("Hero", "The main character")),
                List.of(),
                List.of());

        var world = WorldFixture.privateWorld().build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(world));
        when(repository.save(any(World.class))).thenReturn(world);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
        verify(repository).save(any(World.class));
    }

    @Test
    public void updateWorld_whenLorebookEntriesToUpdate_thenEntriesAreUpdated() {

        // given
        var world = WorldFixture.privateWorld().build();
        var addedEntry = world.addLorebookEntry("Old Name", "Old Description");
        var entryId = UUID.randomUUID();
        ReflectionTestUtils.setField(addedEntry, "publicId", entryId);

        var command = new UpdateWorld(
                WorldFixture.PUBLIC_ID,
                "MoirAI",
                "This is an RPG world",
                "As you enter the city, people around you start looking at you.",
                null,
                null,
                Visibility.PUBLIC,
                Set.of(),
                List.of(),
                List.of(new UpdateWorld.LorebookEntryToUpdate(entryId, "New Name", "New Description")),
                List.of());

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(world));
        when(repository.save(any(World.class))).thenReturn(world);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
        verify(repository).save(any(World.class));
    }

    @Test
    public void updateWorld_whenLorebookEntriesToDelete_thenEntriesAreRemoved() {

        // given
        var world = WorldFixture.privateWorld().build();
        var addedEntry = world.addLorebookEntry("Entry", "Description");
        var entryId = UUID.randomUUID();
        ReflectionTestUtils.setField(addedEntry, "publicId", entryId);

        var command = new UpdateWorld(
                WorldFixture.PUBLIC_ID,
                "MoirAI",
                "This is an RPG world",
                "As you enter the city, people around you start looking at you.",
                null,
                null,
                Visibility.PUBLIC,
                Set.of(),
                List.of(),
                List.of(),
                List.of(entryId));

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(world));
        when(repository.save(any(World.class))).thenReturn(world);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
        verify(repository).save(any(World.class));
    }
}
