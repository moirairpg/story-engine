package me.moirai.storyengine.core.application.command.persona;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.port.inbound.persona.PersonaDetails;
import me.moirai.storyengine.core.port.inbound.persona.UpdatePersona;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;

@CommandHandler
public class UpdatePersonaHandler extends AbstractCommandHandler<UpdatePersona, PersonaDetails> {

    private static final String PERSONA_NOT_FOUND = "Persona was not found";
    private static final String ID_CANNOT_BE_NULL_OR_EMPTY = "Persona ID cannot be null or empty";

    private final PersonaRepository repository;

    public UpdatePersonaHandler(PersonaRepository repository) {

        this.repository = repository;
    }

    @Override
    public void validate(UpdatePersona request) {

        if (request.personaId() == null) {
            throw new IllegalArgumentException(ID_CANNOT_BE_NULL_OR_EMPTY);
        }
    }

    @Override
    public PersonaDetails execute(UpdatePersona command) {

        var persona = repository.findByPublicId(command.personaId())
                .orElseThrow(() -> new AssetNotFoundException(PERSONA_NOT_FOUND));

        persona.updateName(command.name());
        persona.updatePersonality(command.personality());

        if (command.visibility() != null) {
            switch (command.visibility()) {
                case PUBLIC -> persona.makePublic();
                case PRIVATE -> persona.makePrivate();
                default -> persona.makePrivate();
            }
        }

        emptyIfNull(command.usersAllowedToReadToAdd()).forEach(id -> persona.grant(new Permission(id, PermissionLevel.READ)));
        emptyIfNull(command.usersAllowedToWriteToAdd()).forEach(id -> persona.grant(new Permission(id, PermissionLevel.WRITE)));
        emptyIfNull(command.usersAllowedToReadToRemove()).forEach(persona::revoke);
        emptyIfNull(command.usersAllowedToWriteToRemove()).forEach(persona::revoke);

        var updatedPersona = repository.save(persona);

        return mapResult(updatedPersona);
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
