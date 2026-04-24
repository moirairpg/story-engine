package me.moirai.storyengine.core.port.inbound.message;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;

public record SendMessage(
        UUID adventureId,
        String content,
        String username)
        implements Command<MessageResult> {
}
