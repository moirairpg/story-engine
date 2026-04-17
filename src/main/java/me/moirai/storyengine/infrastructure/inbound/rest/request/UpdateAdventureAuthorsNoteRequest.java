package me.moirai.storyengine.infrastructure.inbound.rest.request;

import me.moirai.storyengine.infrastructure.inbound.rest.validation.Moderated;

public record UpdateAdventureAuthorsNoteRequest(
        @Moderated String authorsNote) {
}
