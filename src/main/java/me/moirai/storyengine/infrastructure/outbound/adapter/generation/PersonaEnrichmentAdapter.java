package me.moirai.storyengine.infrastructure.outbound.adapter.generation;

import static java.lang.String.format;
import static me.moirai.storyengine.common.util.DefaultStringProcessors.replacePersonaNamePlaceholderWith;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.util.StringProcessor;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.port.outbound.generation.ModelConfigurationRequest;
import me.moirai.storyengine.core.port.outbound.generation.PersonaEnrichmentPort;
import me.moirai.storyengine.core.port.outbound.generation.TokenizerPort;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import reactor.core.publisher.Mono;

@Component
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class PersonaEnrichmentAdapter implements PersonaEnrichmentPort {

    private static final String PERSONA_DESCRIPTION = "[ DEBUG MODE ON: You are an actor interpreting the role of {name}. {name}'s persona is as follows, and you are to maintain character during this conversation: %s ]";
    private static final String PERSONA = "persona";
    private static final String PERSONA_NAME = "personaName";
    private static final String PERSONA_NOT_FOUND = "Persona not found";
    private static final String PERSONA_IS_TOO_BIG_FOR_CONTEXT = "Persona is too big to fit in context";

    private final TokenizerPort tokenizerPort;
    private final PersonaRepository personaRepository;
    private final ChatMessageAdapter chatMessageService;

    public PersonaEnrichmentAdapter(
            TokenizerPort tokenizerPort,
            PersonaRepository personaRepository,
            ChatMessageAdapter chatMessageService) {

        this.tokenizerPort = tokenizerPort;
        this.personaRepository = personaRepository;
        this.chatMessageService = chatMessageService;
    }

    @Override
    public Mono<Map<String, Object>> enrichContextWithPersona(Map<String, Object> context, String personaId,
            ModelConfigurationRequest modelConfiguration) {

        int totalTokens = modelConfiguration.getAiModel().getHardTokenLimit();
        int reservedTokensForPersona = (int) Math.floor(totalTokens * 0.20);

        return Mono.just(personaRepository.findById(personaId)
                .orElseThrow(() -> new AssetNotFoundException(PERSONA_NOT_FOUND)))
                .map(persona -> addPersonaToContext(persona, context, reservedTokensForPersona))
                .map(ctx -> chatMessageService.addMessagesToContext(ctx, reservedTokensForPersona));
    }

    private Map<String, Object> addPersonaToContext(Persona persona,
            Map<String, Object> context, int reservedTokensForPersona) {

        StringProcessor processor = new StringProcessor();
        processor.addRule(replacePersonaNamePlaceholderWith(persona.getName()));
        String formattedPersona = processor.process(format(PERSONA_DESCRIPTION, persona.getPersonality()));

        int tokensInPersona = tokenizerPort.getTokenCountFrom(formattedPersona);

        if (tokensInPersona > reservedTokensForPersona) {
            throw new IllegalStateException(PERSONA_IS_TOO_BIG_FOR_CONTEXT);
        }

        context.put(PERSONA_NAME, persona.getName());
        context.put(PERSONA, formattedPersona);

        return context;
    }
}
