package me.moirai.storyengine.core.port.inbound.userdetails;

import me.moirai.storyengine.common.cqs.command.Command;

public record AuthenticateUser(
        String authenticationCode,
        String redirectUrl)
        implements Command<AuthenticateUserResult> {
}
