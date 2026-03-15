package me.moirai.storyengine.core.application.usecase.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import me.moirai.storyengine.core.application.usecase.adventure.request.CreateAdventureFixture;
import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventure;
import org.springframework.test.util.ReflectionTestUtils;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@ExtendWith(MockitoExtension.class)
public class CreateAdventureHandlerTest {

    @Mock
    private WorldRepository worldRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private AdventureRepository repository;

    @InjectMocks
    private CreateAdventureHandler handler;

    @Test
    public void createAdventure_whenWorldNotFound_thenThrowException() {

        // Given
        CreateAdventure command = CreateAdventureFixture.sample().build();

        when(worldRepository.findByPublicId(anyString())).thenReturn(Optional.empty());

        // Then
        assertThrows(AssetNotFoundException.class, () -> handler.handle(command));
    }

    @Test
    public void createAdventure_whenNoWorldPermission_thenThrowException() {

        // Given
        String userId = "someUserId";
        CreateAdventure command = CreateAdventureFixture.sample()
                .requesterId(userId)
                .build();

        World world = WorldFixture.privateWorld().build();

        when(worldRepository.findByPublicId(anyString())).thenReturn(Optional.of(world));

        // Then
        assertThrows(AssetAccessDeniedException.class, () -> handler.handle(command));
    }

    @Test
    public void createAdventure_whenPersonaNotFound_thenThrowException() {

        // Given
        String userId = "someUserId";
        CreateAdventure command = CreateAdventureFixture.sample()
                .requesterId(userId)
                .build();

        World world = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(userId)
                        .build())
                .build();

        when(worldRepository.findByPublicId(anyString())).thenReturn(Optional.of(world));
        when(personaRepository.findByPublicId(anyString())).thenReturn(Optional.empty());

        // Then
        assertThrows(AssetNotFoundException.class, () -> handler.handle(command));
    }

    @Test
    public void createAdventure_whenNoPersonaPermission_thenThrowException() {

        // Given
        String userId = "someUserId";
        CreateAdventure command = CreateAdventureFixture.sample()
                .requesterId(userId)
                .build();

        World world = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(userId)
                        .build())
                .build();

        when(worldRepository.findByPublicId(anyString())).thenReturn(Optional.of(world));
        when(personaRepository.findByPublicId(anyString())).thenReturn(Optional.of(PersonaFixture.privatePersona().build()));

        // Then
        assertThrows(AssetAccessDeniedException.class, () -> handler.handle(command));
    }

    @Test
    public void createAdventure_whenValidDate_thenAdventureIsCreated() {

        // Given
        CreateAdventure command = CreateAdventureFixture.sample().build();
        Adventure adventure = AdventureFixture.privateMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(command.getRequesterDiscordId())
                        .build())
                .build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        World world = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(command.getRequesterDiscordId())
                        .build())
                .build();
        ReflectionTestUtils.setField(world, "id", WorldFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(world, "publicId", WorldFixture.PUBLIC_ID);

        when(worldRepository.findByPublicId(anyString())).thenReturn(Optional.of(world));
        when(personaRepository.findByPublicId(anyString())).thenReturn(Optional.of(
                PersonaFixture.privatePersona()
                        .permissions(PermissionsFixture.samplePermissions()
                                .ownerId(command.getRequesterDiscordId())
                                .build())
                        .build()));
        when(repository.save(any(Adventure.class))).thenReturn(adventure);

        // When
        AdventureDetails result = handler.handle(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(AdventureFixture.PUBLIC_ID);
    }
}
