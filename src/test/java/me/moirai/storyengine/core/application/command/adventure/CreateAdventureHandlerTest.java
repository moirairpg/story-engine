package me.moirai.storyengine.core.application.command.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.dto.PermissionDto;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.CreateAdventureFixture;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventure;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@ExtendWith(MockitoExtension.class)
public class CreateAdventureHandlerTest {

    private static final Long AUTHENTICATED_USER_ID = 99999L;

    @Mock
    private WorldRepository worldRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private AdventureRepository repository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CreateAdventureHandler handler;

    @BeforeEach
    void setupSecurityContext() {

        var principal = new MoiraiPrincipal(UUID.randomUUID(), AUTHENTICATED_USER_ID, "discordId",
                "user", "user@test.com", "token", "refresh", null, null);
        var authentication = new UsernamePasswordAuthenticationToken(principal, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void createAdventure_whenWorldNotFound_thenThrowException() {

        // given
        var command = CreateAdventureFixture.sample();
        when(worldRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // then
        assertThrows(AssetNotFoundException.class, () -> handler.handle(command));
    }

    @Test
    public void createAdventure_whenPersonaNotFound_thenThrowException() {

        // given
        var command = CreateAdventureFixture.sample();
        var world = WorldFixture.privateWorld().build();
        world.grant(new Permission(AUTHENTICATED_USER_ID, PermissionLevel.READ));

        when(worldRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(world));
        when(personaRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // then
        assertThrows(AssetNotFoundException.class, () -> handler.handle(command));
    }

    @Test
    public void createAdventure_whenValidDate_thenAdventureIsCreated() {

        // given
        var command = CreateAdventureFixture.sample();
        var adventure = AdventureFixture.privateMultiplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var world = WorldFixture.privateWorld().build();
        ReflectionTestUtils.setField(world, "id", WorldFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(world, "publicId", WorldFixture.PUBLIC_ID);

        var persona = PersonaFixture.privatePersona().build();
        ReflectionTestUtils.setField(persona, "id", PersonaFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(persona, "publicId", PersonaFixture.PUBLIC_ID);

        when(worldRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(world));
        when(personaRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(persona));
        when(repository.save(any(Adventure.class))).thenReturn(adventure);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        AdventureDetails result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(AdventureFixture.PUBLIC_ID);
    }

    @Test
    public void shouldSetPermissionsFromCommandWhenCreateAdventure() {

        // given
        var sample = CreateAdventureFixture.sample();
        var permissionDto = new PermissionDto(UserFixture.PUBLIC_ID, PermissionLevel.READ);
        var command = new CreateAdventure(
                sample.name(),
                sample.description(),
                sample.worldId(),
                sample.personaId(),
                sample.visibility(),
                sample.moderation(),
                sample.isMultiplayer(),
                Set.of(permissionDto),
                sample.modelConfiguration(),
                sample.contextAttributes());

        var adventure = AdventureFixture.privateMultiplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var world = WorldFixture.privateWorld().build();
        ReflectionTestUtils.setField(world, "id", WorldFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(world, "publicId", WorldFixture.PUBLIC_ID);

        var persona = PersonaFixture.privatePersona().build();
        ReflectionTestUtils.setField(persona, "id", PersonaFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(persona, "publicId", PersonaFixture.PUBLIC_ID);

        var user = UserFixture.playerWithId();

        when(worldRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(world));
        when(personaRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(persona));
        when(userRepository.findByPublicId(UserFixture.PUBLIC_ID)).thenReturn(Optional.of(user));
        when(repository.save(any(Adventure.class))).thenReturn(adventure);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));

        // when
        AdventureDetails result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    public void shouldThrowWhenUserNotFoundDuringPermissionResolution() {

        // given
        var sample = CreateAdventureFixture.sample();
        var permissionDto = new PermissionDto(UserFixture.PUBLIC_ID, PermissionLevel.READ);
        var command = new CreateAdventure(
                sample.name(),
                sample.description(),
                sample.worldId(),
                sample.personaId(),
                sample.visibility(),
                sample.moderation(),
                sample.isMultiplayer(),
                Set.of(permissionDto),
                sample.modelConfiguration(),
                sample.contextAttributes());

        var world = WorldFixture.privateWorld().build();
        var persona = PersonaFixture.privatePersona().build();

        when(worldRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(world));
        when(personaRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(persona));
        when(userRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // then
        assertThrows(AssetNotFoundException.class, () -> handler.handle(command));
    }
}
