package me.moirai.storyengine.core.application.command.message;

import static me.moirai.storyengine.common.util.DefaultStringProcessors.addChatPrefix;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.enums.AiRole;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.message.Message;
import me.moirai.storyengine.core.port.inbound.message.MessageResult;
import me.moirai.storyengine.core.port.inbound.message.Say;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.message.MessageRepository;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;

@CommandHandler
public class SayHandler extends AbstractCommandHandler<Say, MessageResult> {

    private final AdventureRepository adventureRepository;
    private final PersonaRepository personaRepository;
    private final MessageRepository messageRepository;

    public SayHandler(
            AdventureRepository adventureRepository,
            PersonaRepository personaRepository,
            MessageRepository messageRepository) {

        this.adventureRepository = adventureRepository;
        this.personaRepository = personaRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    public void validate(Say command) {
        if (command.adventureId() == null) {
            throw new IllegalArgumentException("Adventure ID cannot be null");
        }

        if (command.content() == null || command.content().isBlank()) {
            throw new IllegalArgumentException("Message content cannot be blank");
        }
    }

    @Override
    public MessageResult execute(Say command) {

        var adventure = adventureRepository.findByPublicId(command.adventureId())
                .orElseThrow(() -> new NotFoundException("Adventure not found"));

        var persona = personaRepository.findById(adventure.getPersonaId())
                .orElseThrow(() -> new NotFoundException("Persona not found"));

        var message = Message.builder()
                .adventureId(adventure.getId())
                .role(AiRole.ASSISTANT)
                .content(addChatPrefix(persona.getName()).apply(command.content()))
                .build();

        var saved = messageRepository.save(message);

        return new MessageResult(
                saved.getPublicId(),
                command.content(),
                saved.getRole(),
                saved.getCreationDate());
    }
}
