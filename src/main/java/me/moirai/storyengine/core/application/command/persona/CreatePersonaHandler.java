package me.moirai.storyengine.core.application.command.persona;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.port.inbound.persona.CreatePersona;
import me.moirai.storyengine.core.port.inbound.persona.PersonaDetails;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;

@CommandHandler
public class CreatePersonaHandler extends AbstractCommandHandler<CreatePersona, PersonaDetails> {

    private final PersonaRepository repository;

    public CreatePersonaHandler(PersonaRepository repository) {
        this.repository = repository;
    }

    @Override
    public PersonaDetails execute(CreatePersona command) {

        var persona = repository.save(Persona.builder()
                .name(command.name())
                .personality(command.personality())
                .visibility(command.visibility())
                .build());

        emptyIfNull(command.usersAllowedToRead()).forEach(id -> persona.grant(new Permission(id, PermissionLevel.READ)));
        emptyIfNull(command.usersAllowedToWrite()).forEach(id -> persona.grant(new Permission(id, PermissionLevel.WRITE)));

        var savedPersona = repository.save(persona);
        return mapResult(savedPersona);
    }

    private PersonaDetails mapResult(Persona persona) {

        return new PersonaDetails(
                persona.getPublicId(),
                persona.getName(),
                persona.getPersonality(),
                persona.getVisibility(),
                persona.getPermissions(),
                persona.getCreationDate(),
                persona.getLastUpdateDate());
    }
}
