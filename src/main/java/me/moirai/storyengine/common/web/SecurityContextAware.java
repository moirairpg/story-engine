package me.moirai.storyengine.common.web;

import org.springframework.security.core.context.SecurityContextHolder;

import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;

public abstract class SecurityContextAware {

    protected MoiraiPrincipal getAuthenticatedUser() {
        return (MoiraiPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    protected String authenticatedUserId() {
        return getAuthenticatedUser().discordId();
    }

    protected String authenticatedUsername() {
        return getAuthenticatedUser().username();
    }
}
