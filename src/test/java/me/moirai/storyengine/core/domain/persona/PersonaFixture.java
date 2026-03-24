package me.moirai.storyengine.core.domain.persona;

import java.util.UUID;

import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.core.domain.PermissionFixture;

public class PersonaFixture {

    public static final Long OWNER_ID = 1111L;
    public static final UUID PUBLIC_ID = UUID.fromString("857345aa-0000-0000-0000-000000000000");
    public static final Long NUMERIC_ID = 1L;

    public static Persona.Builder publicPersona() {

        return Persona.builder()
                .name("MoirAI")
                .personality("I am a Discord chatbot")
                .visibility(Visibility.PUBLIC)
                .permissions(new Permission(OWNER_ID, PermissionLevel.OWNER));
    }

    public static Persona.Builder privatePersona() {

        return Persona.builder()
                .name("MoirAI")
                .personality("I am a Discord chatbot")
                .visibility(Visibility.PRIVATE)
                .permissions(new Permission(OWNER_ID, PermissionLevel.OWNER));
    }

    public static Persona publicPersonaWithId() {

        var persona = publicPersona().build();
        ReflectionTestUtils.setField(persona, "id", NUMERIC_ID);
        ReflectionTestUtils.setField(persona, "publicId", PUBLIC_ID);
        return persona;
    }

    public static Persona privatePersonaWithId() {

        var persona = privatePersona().build();
        ReflectionTestUtils.setField(persona, "id", NUMERIC_ID);
        ReflectionTestUtils.setField(persona, "publicId", PUBLIC_ID);
        return persona;
    }

    public static Persona publicPersonaWithIdAndPermissions() {

        var persona = publicPersonaWithId();
        persona.permissions.addAll(PermissionFixture.samplePermissions());
        return persona;
    }

    public static Persona privatePersonaWithIdAndPermissions() {

        var persona = privatePersonaWithId();
        persona.permissions.addAll(PermissionFixture.samplePermissions());
        return persona;
    }
}
