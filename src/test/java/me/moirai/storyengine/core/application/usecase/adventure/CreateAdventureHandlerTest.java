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
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventure;
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

        when(worldRepository.findById(anyString())).thenReturn(Optional.empty());

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

        when(worldRepository.findById(anyString())).thenReturn(Optional.of(world));

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

        when(worldRepository.findById(anyString())).thenReturn(Optional.of(world));
        when(personaRepository.findById(anyString())).thenReturn(Optional.empty());

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

        Persona persona = PersonaFixture.privatePersona().build();

        when(worldRepository.findById(anyString())).thenReturn(Optional.of(world));
        when(personaRepository.findById(anyString())).thenReturn(Optional.of(persona));

        // Then
        assertThrows(AssetAccessDeniedException.class, () -> handler.handle(command));
    }

    @Test
    public void createAdventure_whenValidDate_thenAdventureIsCreated() {

        // Given
        String id = "HAUDHUAHD";
        CreateAdventure command = CreateAdventureFixture.sample().build();
        Adventure adventure = AdventureFixture.privateMultiplayerAdventure()
                .id(id)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(command.getRequesterDiscordId())
                        .build())
                .build();

        World world = WorldFixture.privateWorld()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(command.getRequesterDiscordId())
                        .build())
                .build();

        Persona persona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(command.getRequesterDiscordId())
                        .build())
                .build();

        when(worldRepository.findById(anyString())).thenReturn(Optional.of(world));
        when(personaRepository.findById(anyString())).thenReturn(Optional.of(persona));
        when(repository.save(any(Adventure.class))).thenReturn(adventure);

        // When
        AdventureDetails result = handler.handle(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
    }
}
