package me.moirai.storyengine.core.domain.persona;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.common.domain.Permissions;
import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.core.domain.PermissionsFixture;

public class PersonaFixture {

    private static final String OWNER_DISCORD_ID = "586678721356875";
    public static final UUID PUBLIC_ID = UUID.fromString("857345aa-0000-0000-0000-000000000000");
    public static final Long NUMERIC_ID = 1L;

    public static Persona.Builder publicPersona() {

        var permissions = PermissionsFixture.samplePermissions().build();
        return Persona.builder()
                .name("MoirAI")
                .personality("I am a Discord chatbot")
                .visibility(Visibility.PUBLIC)
                .creatorId(OWNER_DISCORD_ID)
                .creationDate(OffsetDateTime.now())
                .lastUpdateDate(OffsetDateTime.now())
                .permissions(permissions);
    }

    public static Persona.Builder privatePersona() {

        Permissions permissions = PermissionsFixture.samplePermissions().build();
        return Persona.builder()
                .name("MoirAI")
                .personality("I am a Discord chatbot")
                .visibility(Visibility.PRIVATE)
                .creatorId(OWNER_DISCORD_ID)
                .creationDate(OffsetDateTime.now())
                .lastUpdateDate(OffsetDateTime.now())
                .permissions(permissions);
    }

    public static Persona publicPersonaWithId() {

        Persona persona = publicPersona().build();
        ReflectionTestUtils.setField(persona, "id", NUMERIC_ID);
        ReflectionTestUtils.setField(persona, "publicId", PUBLIC_ID);
        return persona;
    }

    public static Persona privatePersonaWithId() {

        Persona persona = privatePersona().build();
        ReflectionTestUtils.setField(persona, "id", NUMERIC_ID);
        ReflectionTestUtils.setField(persona, "publicId", PUBLIC_ID);
        return persona;
    }
}
