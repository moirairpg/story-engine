package me.moirai.storyengine.infrastructure.outbound.adapter.adventure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import me.moirai.storyengine.AbstractIntegrationTest;
import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureAuthorizationReader;

public class AdventureAuthorizationReaderImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AdventureAuthorizationReader reader;

    @BeforeEach
    public void before() {
        clearDatabase();
    }

    @Test
    public void getAuthorizationData_whenAdventureNotFound_thenReturnEmpty() {

        // Given
        var publicId = UUID.randomUUID();

        // When
        var result = reader.getAuthorizationData(publicId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void getAuthorizationData_whenAdventureFound_thenReturnVisibilityAndEmptyPermissions() {

        // Given
        var persona = insert(PersonaFixture.publicPersona().build(), Persona.class);
        var world = insert(WorldFixture.publicWorld().build(), World.class);
        var adventure = AdventureFixture.publicSingleplayerAdventure()
                .worldId(world.getPublicId())
                .personaId(persona.getId())
                .build();

        insert(adventure, Adventure.class);

        // When
        var result = reader.getAuthorizationData(adventure.getPublicId());

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.get().ownerId()).isNull();
        assertThat(result.get().writers()).isEmpty();
        assertThat(result.get().readers()).isEmpty();
        assertThat(result.get().visibility()).isEqualTo(Visibility.PUBLIC);
    }

    @Test
    public void getAuthorizationData_whenAdventureHasWriter_thenReturnWriterPublicId() {

        // Given
        var user = insert(UserFixture.player().build(), User.class);
        var persona = insert(PersonaFixture.publicPersona().build(), Persona.class);
        var world = insert(WorldFixture.publicWorld().build(), World.class);
        var adventure = AdventureFixture.publicSingleplayerAdventure()
                .worldId(world.getPublicId())
                .personaId(persona.getId())
                .permissions(new Permission(user.getId(), PermissionLevel.WRITE))
                .build();

        insert(adventure, Adventure.class);

        // When
        var result = reader.getAuthorizationData(adventure.getPublicId());

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.get().writers()).containsExactly(user.getPublicId());
    }
}
