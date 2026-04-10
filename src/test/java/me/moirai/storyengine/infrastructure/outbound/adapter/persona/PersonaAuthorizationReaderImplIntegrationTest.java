package me.moirai.storyengine.infrastructure.outbound.adapter.persona;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import me.moirai.storyengine.AbstractIntegrationTest;
import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.port.outbound.persona.PersonaAuthorizationReader;

public class PersonaAuthorizationReaderImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PersonaAuthorizationReader reader;

    @BeforeEach
    public void before() {
        clearDatabase();
    }

    @Test
    public void getAuthorizationData_whenPersonaNotFound_thenReturnEmpty() {

        // Given
        var publicId = UUID.randomUUID();

        // When
        var result = reader.getAuthorizationData(publicId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void getAuthorizationData_whenPersonaFound_thenReturnVisibilityAndEmptyPermissions() {

        // Given
        var persona = insert(PersonaFixture.publicPersona().build(), Persona.class);

        // When
        var result = reader.getAuthorizationData(persona.getPublicId());

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.get().ownerId()).isNull();
        assertThat(result.get().writers()).isEmpty();
        assertThat(result.get().readers()).isEmpty();
        assertThat(result.get().visibility()).isEqualTo(Visibility.PUBLIC);
    }

    @Test
    public void getAuthorizationData_whenPersonaHasWriter_thenReturnWriterPublicId() {

        // Given
        var user = insert(UserFixture.player().build(), User.class);
        var persona = PersonaFixture.publicPersona()
                .permissions(new Permission(user.getId(), PermissionLevel.WRITE))
                .build();

        insert(persona, Persona.class);

        // When
        var result = reader.getAuthorizationData(persona.getPublicId());

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.get().writers()).containsExactly(user.getPublicId());
    }
}
