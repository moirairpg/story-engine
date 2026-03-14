package me.moirai.storyengine.core.domain.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.domain.Permissions;
import me.moirai.storyengine.common.domain.Visibility;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.exception.ModerationException;
import me.moirai.storyengine.core.application.model.result.TextModerationResultFixture;
import me.moirai.storyengine.core.port.inbound.world.CreateWorld;
import me.moirai.storyengine.core.port.inbound.world.CreateWorldLorebookEntry;
import me.moirai.storyengine.core.application.usecase.world.request.CreateWorldLorebookEntryFixture;
import me.moirai.storyengine.core.port.inbound.world.DeleteWorldLorebookEntry;
import me.moirai.storyengine.core.port.inbound.world.GetWorldLorebookEntryById;
import me.moirai.storyengine.core.port.inbound.world.UpdateWorldLorebookEntry;
import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.port.TextModerationPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class WorldServiceImplTest {

    @Mock
    private TextModerationPort moderationPort;

    @Mock
    private WorldLorebookEntryRepository lorebookEntryRepository;

    @Mock
    private WorldRepository worldRepository;

    @InjectMocks
    private WorldServiceImpl service;

    @Test
    public void createWorld_whenValidData_thenWorldIsCreatedSuccessfully() {

        // Given
        String name = "Eldrida";
        String description = "Eldrida is a fantasy world";
        String adventureStart = "You have arrived at the world of Eldrida.";
        Permissions permissions = PermissionsFixture.samplePermissions().build();
        String visibility = "PRIVATE";

        World expectedWorld = WorldFixture.publicWorld()
                .name(name)
                .description(description)
                .adventureStart(adventureStart)
                .visibility(Visibility.fromString(visibility))
                .permissions(permissions)
                .build();

        CreateWorld command = CreateWorld.builder()
                .name(name)
                .adventureStart(adventureStart)
                .description(description)
                .visibility(visibility)
                .requesterId(permissions.getOwnerId())
                .usersAllowedToRead(permissions.getUsersAllowedToRead())
                .usersAllowedToWrite(permissions.getUsersAllowedToWrite())
                .lorebookEntries(Collections.singletonList(CreateWorldLorebookEntryFixture.sampleLorebookEntry().build()))
                .build();

        when(moderationPort.moderate(anyString()))
                .thenReturn(Mono.just(TextModerationResultFixture.withoutFlags().build()));

        when(worldRepository.save(any(World.class))).thenReturn(expectedWorld);

        // Then
        StepVerifier.create(service.createFrom(command))
                .assertNext(createdWorld -> {
                    assertThat(createdWorld).isNotNull().isEqualTo(expectedWorld);
                    assertThat(createdWorld.getName()).isEqualTo(expectedWorld.getName());
                    assertThat(createdWorld.getOwnerId()).isEqualTo(expectedWorld.getOwnerId());
                    assertThat(createdWorld.getUsersAllowedToWrite()).isEqualTo(expectedWorld.getUsersAllowedToWrite());
                    assertThat(createdWorld.getUsersAllowedToRead()).isEqualTo(expectedWorld.getUsersAllowedToRead());
                    assertThat(createdWorld.getDescription()).isEqualTo(expectedWorld.getDescription());
                    assertThat(createdWorld.getAdventureStart()).isEqualTo(expectedWorld.getAdventureStart());
                    assertThat(createdWorld.getVisibility()).isEqualTo(expectedWorld.getVisibility());
                })
                .verifyComplete();
    }

    @Test
    public void createWorld_whenLorebookIsNull_thenWorldIsCreatedSuccessfully() {

        // Given
        String name = "Eldrida";
        String description = "Eldrida is a fantasy world";
        String adventureStart = "You have arrived at the world of Eldrida.";
        Permissions permissions = PermissionsFixture.samplePermissions().build();
        String visibility = "PRIVATE";

        World expectedWorld = WorldFixture.publicWorld()
                .name(name)
                .description(description)
                .adventureStart(adventureStart)
                .visibility(Visibility.fromString(visibility))
                .permissions(permissions)
                .build();

        CreateWorld command = CreateWorld.builder()
                .name(name)
                .adventureStart(adventureStart)
                .description(description)
                .visibility(visibility)
                .requesterId(permissions.getOwnerId())
                .usersAllowedToRead(permissions.getUsersAllowedToRead())
                .usersAllowedToWrite(permissions.getUsersAllowedToWrite())
                .build();

        when(moderationPort.moderate(anyString()))
                .thenReturn(Mono.just(TextModerationResultFixture.withoutFlags().build()));

        when(worldRepository.save(any(World.class))).thenReturn(expectedWorld);

        // Then
        StepVerifier.create(service.createFrom(command))
                .assertNext(createdWorld -> {
                    assertThat(createdWorld).isNotNull().isEqualTo(expectedWorld);
                    assertThat(createdWorld.getName()).isEqualTo(expectedWorld.getName());
                    assertThat(createdWorld.getOwnerId()).isEqualTo(expectedWorld.getOwnerId());
                    assertThat(createdWorld.getUsersAllowedToWrite()).isEqualTo(expectedWorld.getUsersAllowedToWrite());
                    assertThat(createdWorld.getUsersAllowedToRead()).isEqualTo(expectedWorld.getUsersAllowedToRead());
                    assertThat(createdWorld.getDescription()).isEqualTo(expectedWorld.getDescription());
                    assertThat(createdWorld.getAdventureStart()).isEqualTo(expectedWorld.getAdventureStart());
                    assertThat(createdWorld.getVisibility()).isEqualTo(expectedWorld.getVisibility());
                })
                .verifyComplete();
    }

    @Test
    public void createLorebookEntry_whenValidData_thenEntryIsCreatedSuccessfully() {

        // Given
        String name = "Eldrida";
        String description = "Eldrida is a kingdom in an empire";
        String regex = "[Ee]ldrida";
        String worldId = "WRLDID";

        WorldLorebookEntry.Builder lorebookEntryBuilder = WorldLorebookEntryFixture.sampleLorebookEntry()
                .name(name)
                .description(description)
                .regex(regex);

        WorldLorebookEntry expectedLorebookEntry = lorebookEntryBuilder.build();

        CreateWorldLorebookEntry command = CreateWorldLorebookEntry.builder()
                .name(name)
                .description(description)
                .regex(regex)
                .worldId(worldId)
                .requesterId("586678721356875")
                .build();

        World world = WorldFixture.privateWorld().build();

        when(worldRepository.findById(anyString())).thenReturn(Optional.of(world));
        when(lorebookEntryRepository.save(any(WorldLorebookEntry.class))).thenReturn(expectedLorebookEntry);

        // When
        WorldLorebookEntry createdLorebookEntry = service.createLorebookEntry(command);

        // Then
        assertThat(createdLorebookEntry.getName()).isEqualTo(expectedLorebookEntry.getName());
        assertThat(createdLorebookEntry.getDescription()).isEqualTo(expectedLorebookEntry.getDescription());
        assertThat(createdLorebookEntry.getRegex()).isEqualTo(expectedLorebookEntry.getRegex());
    }

    @Test
    public void createLorebookEntry_whenNotEnoughPermissionOnWorld_thenThrowException() {

        // Given
        String name = "Eldrida";
        String description = "Eldrida is a kingdom in an empire";
        String regex = "[Ee]ldrida";
        String worldId = "WRLDID";

        CreateWorldLorebookEntry command = CreateWorldLorebookEntry.builder()
                .name(name)
                .description(description)
                .regex(regex)
                .worldId(worldId)
                .requesterId("INVLDUSR")
                .build();

        World world = WorldFixture.privateWorld().build();

        when(worldRepository.findById(anyString())).thenReturn(Optional.of(world));

        // Then
        assertThrows(AssetAccessDeniedException.class, () -> service.createLorebookEntry(command));
    }

    @Test
    public void updateLorebookEntry_whenValidData_thenEntryIsUpdatedSuccessfully() {

        // Given
        String name = "Eldrida";
        String description = "Eldrida is a kingdom in an empire";
        String regex = "[Ee]ldrida";
        String worldId = "WRLDID";

        WorldLorebookEntry.Builder lorebookEntryBuilder = WorldLorebookEntryFixture.sampleLorebookEntry()
                .name(name)
                .description(description)
                .regex(regex);

        WorldLorebookEntry expectedLorebookEntry = lorebookEntryBuilder.build();

        UpdateWorldLorebookEntry command = UpdateWorldLorebookEntry.builder()
                .id("ENTRID")
                .name(name)
                .description(description)
                .regex(regex)
                .worldId(worldId)
                .requesterId("586678721356875")
                .build();

        World world = WorldFixture.privateWorld().build();

        when(worldRepository.findById(anyString())).thenReturn(Optional.of(world));
        when(lorebookEntryRepository.findById(anyString())).thenReturn(Optional.of(expectedLorebookEntry));
        when(lorebookEntryRepository.save(any(WorldLorebookEntry.class))).thenReturn(expectedLorebookEntry);

        // When
        WorldLorebookEntry createdLorebookEntry = service.updateLorebookEntry(command);

        // Then
        assertThat(createdLorebookEntry.getName()).isEqualTo(expectedLorebookEntry.getName());
        assertThat(createdLorebookEntry.getDescription()).isEqualTo(expectedLorebookEntry.getDescription());
        assertThat(createdLorebookEntry.getRegex()).isEqualTo(expectedLorebookEntry.getRegex());
    }

    @Test
    public void updateLorebookEntry_whenEmptyUpdateFields_thenNothingIsChanged() {

        // Given
        String name = "Eldrida";
        String description = "Eldrida is a kingdom in an empire";
        String regex = "[Ee]ldrida";
        String worldId = "WRLDID";

        WorldLorebookEntry expectedLorebookEntry = WorldLorebookEntryFixture.sampleLorebookEntry()
                .name(name)
                .description(description)
                .regex(regex)
                .build();

        UpdateWorldLorebookEntry command = UpdateWorldLorebookEntry.builder()
                .id("ENTRID")
                .name(null)
                .description(null)
                .regex(null)
                .worldId(worldId)
                .requesterId("586678721356875")
                .build();

        World world = WorldFixture.privateWorld().build();

        when(worldRepository.findById(anyString())).thenReturn(Optional.of(world));
        when(lorebookEntryRepository.findById(anyString())).thenReturn(Optional.of(expectedLorebookEntry));
        when(lorebookEntryRepository.save(any(WorldLorebookEntry.class))).thenReturn(expectedLorebookEntry);

        // When
        WorldLorebookEntry createdLorebookEntry = service.updateLorebookEntry(command);

        // Then
        assertThat(createdLorebookEntry.getName()).isEqualTo(expectedLorebookEntry.getName());
        assertThat(createdLorebookEntry.getDescription()).isEqualTo(expectedLorebookEntry.getDescription());
        assertThat(createdLorebookEntry.getRegex()).isEqualTo(expectedLorebookEntry.getRegex());
    }

    @Test
    public void updateLorebookEntry_whenWorldNotFound_thenThrowException() {

        // Given
        String name = "Eldrida";
        String description = "Eldrida is a kingdom in an empire";
        String regex = "[Ee]ldrida";
        String worldId = "WRLDID";

        UpdateWorldLorebookEntry command = UpdateWorldLorebookEntry.builder()
                .id("ENTRID")
                .name(name)
                .description(description)
                .regex(regex)
                .worldId(worldId)
                .requesterId("586678721356875")
                .build();

        World world = WorldFixture.privateWorld().build();

        when(worldRepository.findById(anyString())).thenReturn(Optional.of(world));
        when(lorebookEntryRepository.findById(anyString())).thenReturn(Optional.empty());

        // Then
        assertThrows(AssetNotFoundException.class, () -> service.updateLorebookEntry(command));
    }

    @Test
    public void updateLorebookEntry_whenInvalidPermission_thenThrowException() {

        // Given
        String name = "Eldrida";
        String description = "Eldrida is a kingdom in an empire";
        String regex = "[Ee]ldrida";
        String worldId = "WRLDID";

        UpdateWorldLorebookEntry command = UpdateWorldLorebookEntry.builder()
                .id("ENTRID")
                .name(name)
                .description(description)
                .regex(regex)
                .worldId(worldId)
                .requesterId("INVLDUSR")
                .build();

        World world = WorldFixture.privateWorld().build();

        when(worldRepository.findById(anyString())).thenReturn(Optional.of(world));

        // Then
        assertThrows(AssetAccessDeniedException.class, () -> service.updateLorebookEntry(command));
    }

    @Test
    public void updateLorebookEntry_whenEntryNotFound_thenThrowException() {

        // Given
        String name = "Eldrida";
        String description = "Eldrida is a kingdom in an empire";
        String regex = "[Ee]ldrida";
        String worldId = "WRLDID";

        UpdateWorldLorebookEntry command = UpdateWorldLorebookEntry.builder()
                .id("ENTRID")
                .name(name)
                .description(description)
                .regex(regex)
                .worldId(worldId)
                .build();

        when(worldRepository.findById(anyString())).thenReturn(Optional.empty());

        // Then
        assertThrows(AssetNotFoundException.class, () -> service.updateLorebookEntry(command));
    }

    @Test
    public void createLorebookEntry_whenWorldNotExists_thenThrowException() {

        // Given
        String name = "Eldrida";
        String description = "Eldrida is a kingdom in an empire";
        String regex = "[Ee]ldrida";
        String worldId = "WRLDID";

        CreateWorldLorebookEntry command = CreateWorldLorebookEntry.builder()
                .name(name)
                .description(description)
                .regex(regex)
                .worldId(worldId)
                .build();

        when(worldRepository.findById(anyString())).thenReturn(Optional.empty());

        // Then
        assertThrows(AssetNotFoundException.class,
                () -> service.createLorebookEntry(command));
    }

    @Test
    public void findLorebookEntry_whenValidId_thenEntryIsRerturned() {

        // Given
        String requesterId = "RQSTRID";
        GetWorldLorebookEntryById query = GetWorldLorebookEntryById.builder()
                .requesterId(requesterId)
                .worldId("WRLDID")
                .entryId("ENTRID")
                .build();

        WorldLorebookEntry entry = WorldLorebookEntryFixture.sampleLorebookEntry().build();

        World world = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        when(worldRepository.findById(anyString())).thenReturn(Optional.of(world));
        when(lorebookEntryRepository.findById(anyString())).thenReturn(Optional.of(entry));

        // When
        WorldLorebookEntry retrievedEntry = service.findLorebookEntryById(query);

        // Then
        assertThat(retrievedEntry).isNotNull();
    }

    @Test
    public void findLorebookEntry_whenWorldNotFound_thenThrowException() {

        // Given
        String requesterId = "RQSTRID";
        GetWorldLorebookEntryById query = GetWorldLorebookEntryById.builder()
                .requesterId(requesterId)
                .worldId("WRLDID")
                .entryId("ENTRID")
                .build();

        when(worldRepository.findById(anyString())).thenReturn(Optional.empty());

        // Then
        assertThrows(AssetNotFoundException.class, () -> service.findLorebookEntryById(query));
    }

    @Test
    public void findLorebookEntry_whenInvalidPermissionOnWorld_thenThrowException() {

        // Given
        String requesterId = "RQSTRID";
        GetWorldLorebookEntryById query = GetWorldLorebookEntryById.builder()
                .requesterId(requesterId)
                .worldId("WRLDID")
                .entryId("ENTRID")
                .build();

        World world = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId("INVLDID")
                        .build())
                .build();

        when(worldRepository.findById(anyString())).thenReturn(Optional.of(world));

        // Then
        assertThrows(AssetAccessDeniedException.class, () -> service.findLorebookEntryById(query));
    }

    @Test
    public void findLorebookEntry_whenEntryNotFound_thenThrowException() {

        // Given
        String requesterId = "RQSTRID";
        GetWorldLorebookEntryById query = GetWorldLorebookEntryById.builder()
                .requesterId(requesterId)
                .worldId("WRLDID")
                .entryId("ENTRID")
                .build();

        World world = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        when(worldRepository.findById(anyString())).thenReturn(Optional.of(world));
        when(lorebookEntryRepository.findById(anyString())).thenReturn(Optional.empty());

        // Then
        assertThrows(AssetNotFoundException.class, () -> service.findLorebookEntryById(query));
    }

    @Test
    public void deleteLorebookEntry_whenProperPermissionAndId_thenEntryIsDeleted() {

        // Given
        String requesterId = "RQSTRID";
        DeleteWorldLorebookEntry query = DeleteWorldLorebookEntry.builder()
                .requesterId(requesterId)
                .worldId("WRLDID")
                .lorebookEntryId("ENTRID")
                .build();

        WorldLorebookEntry entry = WorldLorebookEntryFixture.sampleLorebookEntry().build();

        World world = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        when(worldRepository.findById(anyString())).thenReturn(Optional.of(world));
        when(lorebookEntryRepository.findById(anyString())).thenReturn(Optional.of(entry));

        // When
        service.deleteLorebookEntry(query);

        // Then
        verify(lorebookEntryRepository, times(1)).findById(anyString());
        verify(lorebookEntryRepository, times(1)).deleteById(anyString());
    }

    @Test
    public void deleteLorebookEntry_whenWorldNotExists_thenThrowException() {

        // Given
        String requesterId = "RQSTRID";
        DeleteWorldLorebookEntry query = DeleteWorldLorebookEntry.builder()
                .requesterId(requesterId)
                .worldId("WRLDID")
                .lorebookEntryId("ENTRID")
                .build();

        when(worldRepository.findById(anyString())).thenReturn(Optional.empty());

        // Then
        assertThrows(AssetNotFoundException.class, () -> service.deleteLorebookEntry(query));
    }

    @Test
    public void deleteLorebookEntry_whenInvalidPermissionOnWorld_thenThrowException() {

        // Given
        String requesterId = "RQSTRID";
        DeleteWorldLorebookEntry query = DeleteWorldLorebookEntry.builder()
                .requesterId(requesterId)
                .worldId("WRLDID")
                .lorebookEntryId("ENTRID")
                .build();

        World world = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId("INVLDID")
                        .build())
                .build();

        when(worldRepository.findById(anyString())).thenReturn(Optional.of(world));

        // Then
        assertThrows(AssetAccessDeniedException.class, () -> service.deleteLorebookEntry(query));
    }

    @Test
    public void deleteLorebookEntry_whenEntryNotFound_thenThrowException() {

        // Given
        String requesterId = "RQSTRID";
        DeleteWorldLorebookEntry query = DeleteWorldLorebookEntry.builder()
                .requesterId(requesterId)
                .worldId("WRLDID")
                .lorebookEntryId("ENTRID")
                .build();

        World world = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        when(worldRepository.findById(anyString())).thenReturn(Optional.of(world));
        when(lorebookEntryRepository.findById(anyString())).thenReturn(Optional.empty());

        // Then
        assertThrows(AssetNotFoundException.class, () -> service.deleteLorebookEntry(query));
    }

    @Test
    public void createWorld_whenContentIsFlagged_thenExceptionIsThrown() {

        // Given
        String name = "Eldrida";
        String description = "Eldrida is a fantasy world";
        String adventureStart = "You have arrived at the world of Eldrida.";
        Permissions permissions = PermissionsFixture.samplePermissions().build();
        String visibility = "PRIVATE";

        CreateWorld command = CreateWorld.builder()
                .name(name)
                .adventureStart(adventureStart)
                .description(description)
                .visibility(visibility)
                .requesterId(permissions.getOwnerId())
                .usersAllowedToRead(permissions.getUsersAllowedToRead())
                .usersAllowedToWrite(permissions.getUsersAllowedToWrite())
                .lorebookEntries(Collections.singletonList(CreateWorldLorebookEntryFixture.sampleLorebookEntry().build()))
                .build();

        when(moderationPort.moderate(anyString()))
                .thenReturn(Mono.just(TextModerationResultFixture.withFlags().build()));

        // Then
        StepVerifier.create(service.createFrom(command))
                .verifyError(ModerationException.class);
    }
}
