package me.moirai.storyengine.core.port.inbound;

import java.util.UUID;
import java.util.stream.Collectors;

import me.moirai.storyengine.common.dto.PermissionDto;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.port.inbound.persona.PersonaDetails;

public class GetPersonaResultFixture {

    public static PersonaDetails privatePersona() {

        var persona = PersonaFixture.privatePersonaWithIdAndPermissions();

        var permissions = persona.getPermissions().stream()
                .map(permission -> new PermissionDto(
                        UUID.fromString("d6622c6c-85bb-41ba-aa53-93fa68681f85"),
                        PermissionLevel.OWNER))
                .collect(Collectors.toSet());

        return new PersonaDetails(
                persona.getPublicId(),
                persona.getName(),
                persona.getPersonality(),
                persona.getVisibility(),
                permissions,
                persona.getCreationDate(),
                persona.getLastUpdateDate());
    }

    public static PersonaDetails publicPersona() {

        var persona = PersonaFixture.publicPersonaWithIdAndPermissions();

        var permissions = persona.getPermissions().stream()
                .map(permission -> new PermissionDto(
                        UUID.fromString("d6622c6c-85bb-41ba-aa53-93fa68681f85"),
                        PermissionLevel.OWNER))
                .collect(Collectors.toSet());

        return new PersonaDetails(
                persona.getPublicId(),
                persona.getName(),
                persona.getPersonality(),
                persona.getVisibility(),
                permissions,
                persona.getCreationDate(),
                persona.getLastUpdateDate());
    }
}
