package me.moirai.storyengine.core.domain.persona;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;

public class PersonaTest {

    @Test
    public void updateVisibility_whenPrivate_thenMakePublic() {

        // given
        var persona = PersonaFixture.privatePersona().build();

        // when
        persona.makePublic();

        // then
        assertThat(persona.isPublic()).isTrue();
    }

    @Test
    public void updateVisibility_whenPublic_thenMakePrivate() {

        // given
        var persona = PersonaFixture.publicPersona().build();

        // when
        persona.makePrivate();

        // then
        assertThat(persona.isPublic()).isFalse();
    }

    @Test
    public void updatePersona_whenNewNameProvided_thenUpdatePersona() {

        // given
        var name = "New Name";
        var persona = PersonaFixture.publicPersona().build();

        // when
        persona.updateName(name);

        // then
        assertThat(persona.getName()).isEqualTo(name);
    }

    @Test
    public void updatePersona_whenNewPersonality_thenUpdatePersona() {

        // given
        var personality = "New Personality";
        var persona = PersonaFixture.publicPersona().build();

        // when
        persona.updatePersonality(personality);

        // then
        assertThat(persona.getPersonality()).isEqualTo(personality);
    }

    @Test
    public void createPersona_whenNullName_thenThrowException() {

        // given
        var personaBuilder = PersonaFixture.publicPersona().name(null);

        // then
        assertThrows(BusinessRuleViolationException.class, personaBuilder::build);
    }

    @Test
    public void createPersona_whenEmptyName_thenThrowException() {

        // given
        var personaBuilder = PersonaFixture.publicPersona().name(EMPTY);

        // then
        assertThrows(BusinessRuleViolationException.class, personaBuilder::build);
    }

    @Test
    public void createPersona_whenNullPersonality_thenThrowException() {

        // given
        var personaBuilder = PersonaFixture.publicPersona().personality(null);

        // then
        assertThrows(BusinessRuleViolationException.class, personaBuilder::build);
    }

    @Test
    public void createPersona_whenEmptyPersonality_thenThrowException() {

        // given
        var personaBuilder = PersonaFixture.publicPersona().personality(EMPTY);

        // then
        assertThrows(BusinessRuleViolationException.class, personaBuilder::build);
    }

    @Test
    public void createPersona_whenEmptyVisibility_thenThrowException() {

        // given
        var personaBuilder = PersonaFixture.publicPersona().visibility(null);

        // then
        assertThrows(BusinessRuleViolationException.class, personaBuilder::build);
    }

    @Test
    public void grantWritePermission_thenUserCanWriteAndRead() {

        // given
        var userId = 1234567890L;
        var persona = PersonaFixture.publicPersona().build();
        persona.permissions().add(new Permission(9999L, PermissionLevel.OWNER));

        // when
        persona.grant(new Permission(userId, PermissionLevel.WRITE));

        // then
        assertThat(persona.canWrite(userId)).isTrue();
        assertThat(persona.canRead(userId)).isTrue();
    }

    @Test
    public void grantReadPermission_thenUserCanOnlyRead() {

        // given
        var userId = 1234567890L;
        var persona = PersonaFixture.publicPersona().build();
        persona.permissions().add(new Permission(9999L, PermissionLevel.OWNER));

        // when
        persona.grant(new Permission(userId, PermissionLevel.READ));

        // then
        assertThat(persona.canWrite(userId)).isFalse();
        assertThat(persona.canRead(userId)).isTrue();
    }

    @Test
    public void revokeReadPermission_thenUserCannotRead() {

        // given
        var userId = 1234567890L;
        var persona = PersonaFixture.privatePersona().build();
        persona.permissions().add(new Permission(9999L, PermissionLevel.OWNER));
        persona.grant(new Permission(userId, PermissionLevel.READ));

        // when
        persona.revoke(userId);

        // then
        assertThat(persona.canRead(userId)).isFalse();
        assertThat(persona.canWrite(userId)).isFalse();
    }

    @Test
    public void revokeWritePermission_thenUserCannotWrite() {

        // given
        var userId = 1234567890L;
        var persona = PersonaFixture.privatePersona().build();
        persona.permissions().add(new Permission(9999L, PermissionLevel.OWNER));
        persona.grant(new Permission(userId, PermissionLevel.WRITE));

        // when
        persona.revoke(userId);

        // then
        assertThat(persona.canWrite(userId)).isFalse();
        assertThat(persona.canRead(userId)).isFalse();
    }
}
