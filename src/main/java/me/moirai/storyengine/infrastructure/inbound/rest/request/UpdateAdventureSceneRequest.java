package me.moirai.storyengine.infrastructure.inbound.rest.request;

import me.moirai.storyengine.infrastructure.inbound.rest.validation.Moderated;

public record UpdateAdventureSceneRequest(
        @Moderated String scene) {
}
