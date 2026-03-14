package me.moirai.storyengine.core.domain.persona;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
import me.moirai.storyengine.core.domain.Permissions;
import me.moirai.storyengine.core.domain.PermissionsFixture;

public class PersonaTest {

    @Test
    public void updateVisibility_whenPrivate_thenMakePublic() {

        // Given
        Persona persona = PersonaFixture.privatePersona().build();

        // When
        persona.makePublic();

        // Then
        assertThat(persona.isPublic()).isTrue();
    }

    @Test
    public void updateVisibility_whenPublic_thenMakePrivate() {

        // Given
        Persona persona = PersonaFixture.publicPersona().build();

        // When
        persona.makePrivate();

        // Then
        assertThat(persona.isPublic()).isFalse();
    }

    @Test
    public void updatePersona_whenNewNameProvided_thenUpdatePersona() {

        // Given
        String name = "New Name";
        Persona persona = PersonaFixture.publicPersona().build();

        // When
        persona.updateName(name);

        // Then
        assertThat(persona.getName()).isEqualTo(name);
    }

    @Test
    public void updatePersona_whenNewPersonality_thenUpdatePersona() {

        // Given
        String personality = "New Personality";
        Persona persona = PersonaFixture.publicPersona().build();

        // When
        persona.updatePersonality(personality);

        // Then
        assertThat(persona.getPersonality()).isEqualTo(personality);
    }

    @Test
    public void createPersona_whenNullName_thenThrowException() {

        // Given
        Persona.Builder personaBuilder = PersonaFixture.publicPersona().name(null);

        // Then
        assertThrows(BusinessRuleViolationException.class, personaBuilder::build);
    }

    @Test
    public void createPersona_whenEmptyName_thenThrowException() {

        // Given
        Persona.Builder personaBuilder = PersonaFixture.publicPersona().name(EMPTY);

        // Then
        assertThrows(BusinessRuleViolationException.class, personaBuilder::build);
    }

    @Test
    public void createPersona_whenNullPersonality_thenThrowException() {

        // Given
        Persona.Builder personaBuilder = PersonaFixture.publicPersona().personality(null);

        // Then
        assertThrows(BusinessRuleViolationException.class, personaBuilder::build);
    }

    @Test
    public void createPersona_whenEmptyPersonality_thenThrowException() {

        // Given
        Persona.Builder personaBuilder = PersonaFixture.publicPersona().personality(EMPTY);

        // Then
        assertThrows(BusinessRuleViolationException.class, personaBuilder::build);
    }

    @Test
    public void createPersona_whenNullPermissions_thenThrowException() {

        // Given
        Persona.Builder personaBuilder = PersonaFixture.publicPersona().permissions(null);

        // Then
        assertThrows(BusinessRuleViolationException.class, personaBuilder::build);
    }

    @Test
    public void createPersona_whenEmptyVisibility_thenThrowException() {

        // Given
        Persona.Builder personaBuilder = PersonaFixture.publicPersona().visibility(null);

        // Then
        assertThrows(BusinessRuleViolationException.class, personaBuilder::build);
    }

    @Test
    public void updatePersona_whenNewWriterUserAdded_thenTheyShouldHaveReadAndWritePermission() {

        // Given
        String userId = "1234567890";
        Persona.Builder personaBuilder = PersonaFixture.publicPersona();
        Permissions permissions = PermissionsFixture.samplePermissions()
                .usersAllowedToWrite(new HashSet<>()).build();

        personaBuilder.permissions(permissions);

        Persona persona = personaBuilder.build();

        // When
        persona.addWriterUser(userId);

        // Then
        assertThat(persona.getUsersAllowedToWrite()).contains(userId);
        assertThat(persona.canUserWrite(userId)).isTrue();
        assertThat(persona.canUserRead(userId)).isTrue();
    }

    @Test
    public void updatePersona_whenNewReaderUserAdded_thenTheyShouldHaveOnlyReadPermission() {

        // Given
        String userId = "1234567890";
        Persona.Builder personaBuilder = PersonaFixture.publicPersona();
        Permissions permissions = PermissionsFixture.samplePermissions()
                .usersAllowedToRead(new HashSet<>()).build();

        personaBuilder.permissions(permissions);

        Persona persona = personaBuilder.build();

        // When
        persona.addReaderUser(userId);

        // Then
        assertThat(persona.getUsersAllowedToRead()).contains(userId);
        assertThat(persona.canUserWrite(userId)).isFalse();
        assertThat(persona.canUserRead(userId)).isTrue();
    }

    @Test
    public void updatePersona_whenReaderUserRemoved_thenReadPermissionShouldBeRevoked() {

        // Given
        String userId = "1234567890";
        Persona.Builder personaBuilder = PersonaFixture.privatePersona();

        Set<String> usersAllowedToRead = new HashSet<>();
        usersAllowedToRead.add(userId);

        Permissions permissions = PermissionsFixture.samplePermissions()
                .usersAllowedToRead(usersAllowedToRead).build();

        personaBuilder.permissions(permissions);

        Persona persona = personaBuilder.build();

        // When
        persona.removeReaderUser(userId);

        // Then
        assertThat(persona.getUsersAllowedToRead()).doesNotContain(userId);
        assertThat(persona.canUserWrite(userId)).isFalse();
        assertThat(persona.canUserRead(userId)).isFalse();
    }

    @Test
    public void updatePersona_whenWriterUserRemoved_thenReadAndWritePermissionShouldBeRevoked() {

        // Given
        String userId = "1234567890";
        Persona.Builder personaBuilder = PersonaFixture.privatePersona();

        Set<String> usersAllowedToWrite = new HashSet<>();
        usersAllowedToWrite.add(userId);

        Permissions permissions = PermissionsFixture.samplePermissions()
                .usersAllowedToWrite(usersAllowedToWrite).build();

        personaBuilder.permissions(permissions);

        Persona persona = personaBuilder.build();

        // When
        persona.removeWriterUser(userId);

        // Then
        assertThat(persona.getUsersAllowedToWrite()).doesNotContain(userId);
        assertThat(persona.canUserWrite(userId)).isFalse();
        assertThat(persona.canUserRead(userId)).isFalse();
    }
}
