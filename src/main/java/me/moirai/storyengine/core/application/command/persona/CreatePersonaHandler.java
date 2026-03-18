package me.moirai.storyengine.core.application.command.persona;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.domain.Permissions;
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

        var permissions = Permissions.builder()
                .ownerId(command.requesterId())
                .usersAllowedToRead(command.usersAllowedToRead())
                .usersAllowedToWrite(command.usersAllowedToWrite())
                .build();

        var persona = repository.save(Persona.builder()
                .name(command.name())
                .personality(command.personality())
                .visibility(command.visibility())
                .permissions(permissions)
                .build());

        return mapResult(persona);
    }

    private PersonaDetails mapResult(Persona persona) {

        return new PersonaDetails(
                persona.getPublicId(),
                persona.getName(),
                persona.getPersonality(),
                persona.getVisibility(),
                persona.getOwnerId(),
                persona.getUsersAllowedToRead(),
                persona.getUsersAllowedToWrite(),
                persona.getCreationDate(),
                persona.getLastUpdateDate());
    }
}
