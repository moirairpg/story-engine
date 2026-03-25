package me.moirai.storyengine.core.port.inbound.persona;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.query.Query;

public record GetPersonaById(
        UUID personaId) implements Query<PersonaDetails> {
}