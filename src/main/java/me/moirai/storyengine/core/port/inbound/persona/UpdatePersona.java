package me.moirai.storyengine.core.port.inbound.persona;

import java.util.Set;
import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;
import me.moirai.storyengine.common.dto.PermissionDto;
import me.moirai.storyengine.common.enums.Visibility;

public record UpdatePersona(
        UUID personaId,
        String name,
        String personality,
        Visibility visibility,
        Set<PermissionDto> permissions)
        implements Command<PersonaDetails> {

    public UpdatePersona {
        permissions = permissions != null ? Set.copyOf(permissions) : Set.of();
    }
}
