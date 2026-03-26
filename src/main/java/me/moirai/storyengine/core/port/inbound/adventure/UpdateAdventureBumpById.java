package me.moirai.storyengine.core.port.inbound.adventure;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;

public record UpdateAdventureBumpById(
        String bump,
        int bumpFrequency,
        UUID adventureId)
        implements Command<Void> {
}