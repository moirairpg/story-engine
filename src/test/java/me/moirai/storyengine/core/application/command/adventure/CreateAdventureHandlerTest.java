package me.moirai.storyengine.core.application.command.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

import me.moirai.storyengine.common.dto.PermissionDto;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.port.inbound.CreateAdventureFixture;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureLorebookEntryDetails;
import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventure;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.generation.EmbeddingPort;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;
import me.moirai.storyengine.core.port.outbound.vectorsearch.LorebookVectorSearchPort;

@ExtendWith(MockitoExtension.class)
public class CreateAdventureHandlerTest {

    private static final Long AUTHENTICATED_USER_ID = 99999L;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private AdventureRepository repository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmbeddingPort embeddingPort;

    @Mock
    private LorebookVectorSearchPort vectorSearchPort;

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
    public void shouldThrowExceptionWhenPersonaNotFound() {

        // given
        var command = CreateAdventureFixture.sample();
        when(personaRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // then
        assertThrows(NotFoundException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldCreateAdventureWhenWorldIdIsNull() {

        // given
        var sample = CreateAdventureFixture.sample();
        var command = new CreateAdventure(
                sample.name(),
                sample.description(),
                null,
                sample.personaId(),
                sample.visibility(),
                sample.moderation(),
                sample.isMultiplayer(),
                sample.adventureStart(),
                Set.of(),
                Set.of(),
                sample.modelConfiguration(),
                sample.contextAttributes());

        var adventure = AdventureFixture.privateMultiplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var persona = PersonaFixture.privatePersona().build();
        ReflectionTestUtils.setField(persona, "id", PersonaFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(persona, "publicId", PersonaFixture.PUBLIC_ID);

        when(personaRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(persona));
        when(repository.save(any(Adventure.class))).thenReturn(adventure);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.worldId()).isNull();
    }

    @Test
    public void shouldCreateAdventureWhenValidData() {

        // given
        var command = CreateAdventureFixture.sample();
        var adventure = AdventureFixture.privateMultiplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var persona = PersonaFixture.privatePersona().build();
        ReflectionTestUtils.setField(persona, "id", PersonaFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(persona, "publicId", PersonaFixture.PUBLIC_ID);

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
                sample.adventureStart(),
                Set.of(),
                Set.of(permissionDto),
                sample.modelConfiguration(),
                sample.contextAttributes());

        var adventure = AdventureFixture.privateMultiplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var persona = PersonaFixture.privatePersona().build();
        ReflectionTestUtils.setField(persona, "id", PersonaFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(persona, "publicId", PersonaFixture.PUBLIC_ID);

        var user = UserFixture.playerWithId();

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
    public void shouldUpsertVectorsForLorebookEntriesFromCommand() {

        // given
        var sample = CreateAdventureFixture.sample();
        var lorebookEntry1 = new AdventureLorebookEntryDetails(null, null, "Mana Shards", "Crystalized ancient magic", null, false, null, null);
        var lorebookEntry2 = new AdventureLorebookEntryDetails(null, null, "The Silence", "A void that devours magic", null, false, null, null);
        var command = new CreateAdventure(
                sample.name(),
                sample.description(),
                sample.worldId(),
                sample.personaId(),
                sample.visibility(),
                sample.moderation(),
                sample.isMultiplayer(),
                sample.adventureStart(),
                Set.of(lorebookEntry1, lorebookEntry2),
                Set.of(),
                sample.modelConfiguration(),
                sample.contextAttributes());

        var adventure = AdventureFixture.privateMultiplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var persona = PersonaFixture.privatePersona().build();
        ReflectionTestUtils.setField(persona, "id", PersonaFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(persona, "publicId", PersonaFixture.PUBLIC_ID);

        when(personaRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(persona));
        when(repository.save(any(Adventure.class))).thenReturn(adventure);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(UserFixture.playerWithId()));
        when(embeddingPort.embed(anyString())).thenReturn(new float[]{0.1f, 0.2f});

        // when
        handler.handle(command);

        // then
        verify(embeddingPort, times(2)).embed(anyString());
        verify(vectorSearchPort, times(2)).upsert(any(UUID.class), any(), any(float[].class));
    }

    @Test
    public void shouldNotUpsertVectorsWhenLorebookEntriesIsEmpty() {

        // given
        var command = CreateAdventureFixture.sample();

        var adventure = AdventureFixture.privateMultiplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var persona = PersonaFixture.privatePersona().build();
        ReflectionTestUtils.setField(persona, "id", PersonaFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(persona, "publicId", PersonaFixture.PUBLIC_ID);

        when(personaRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(persona));
        when(repository.save(any(Adventure.class))).thenReturn(adventure);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        handler.handle(command);

        // then
        verify(embeddingPort, times(0)).embed(anyString());
        verify(vectorSearchPort, times(0)).upsert(any(UUID.class), any(), any(float[].class));
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
                sample.adventureStart(),
                Set.of(),
                Set.of(permissionDto),
                sample.modelConfiguration(),
                sample.contextAttributes());

        var persona = PersonaFixture.privatePersona().build();

        when(personaRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(persona));
        when(userRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // then
        assertThrows(NotFoundException.class, () -> handler.handle(command));
    }
}
