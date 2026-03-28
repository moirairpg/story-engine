package me.moirai.storyengine.core.application.command.persona;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import java.util.stream.Collectors;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.dto.PermissionDto;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.port.inbound.persona.PersonaDetails;
import me.moirai.storyengine.core.port.inbound.persona.UpdatePersona;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@CommandHandler
public class UpdatePersonaHandler extends AbstractCommandHandler<UpdatePersona, PersonaDetails> {

    private static final String PERSONA_NOT_FOUND = "Persona was not found";
    private static final String ID_CANNOT_BE_NULL_OR_EMPTY = "Persona ID cannot be null or empty";

    private final PersonaRepository repository;
    private final UserRepository userRepository;

    public UpdatePersonaHandler(
            PersonaRepository repository,
            UserRepository userRepository) {

        this.repository = repository;
        this.userRepository = userRepository;
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

        var newPermissions = emptyIfNull(command.permissions()).stream()
                .map(dto -> {
                    var user = userRepository.findByPublicId(dto.userId())
                            .orElseThrow(() -> new AssetNotFoundException("User not found"));

                    return new Permission(user.getId(), dto.level());
                })
                .collect(Collectors.toSet());

        persona.updatePermissions(newPermissions);

        var updatedPersona = repository.save(persona);

        return mapResult(updatedPersona);
    }

    private PersonaDetails mapResult(Persona persona) {

        return new PersonaDetails(
                persona.getPublicId(),
                persona.getName(),
                persona.getPersonality(),
                persona.getVisibility(),
                persona.getPermissions().stream()
                        .map(permission -> {
                            var user = userRepository.findById(permission.userId())
                                    .orElseThrow(() -> new AssetNotFoundException("User not found"));

                            return new PermissionDto(user.getPublicId(), permission.level());
                        })
                        .collect(Collectors.toSet()),
                persona.getCreationDate(),
                persona.getLastUpdateDate());
    }
}
