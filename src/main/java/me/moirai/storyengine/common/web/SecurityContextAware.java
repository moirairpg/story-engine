package me.moirai.storyengine.common.web;

import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;

import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;

public abstract class SecurityContextAware {

    protected MoiraiPrincipal getAuthenticatedUser() {
        return (MoiraiPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    protected UUID authenticatedUserId() {
        return getAuthenticatedUser().publicId();
    }

    protected String authenticatedUsername() {
        return getAuthenticatedUser().username();
    }
}
