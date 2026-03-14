package me.moirai.storyengine.infrastructure.security.authentication;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;

import reactor.util.context.Context;

public final class SecuritySessionContext {

    private static final ThreadLocal<MoiraiPrincipal> authenticatedUser = new ThreadLocal<>();
    private static final ThreadLocal<Authentication> authenticationContext = new ThreadLocal<>();

    public static void setCurrentUser(MoiraiPrincipal principal) {
        authenticatedUser.set(principal);
    }

    public static MoiraiPrincipal getAuthenticatedUser() {
        return authenticatedUser.get();
    }

    public static void setAuthenticationContext(Authentication context) {
        authenticationContext.set(context);
    }

    public static Authentication getAuthenticationContext() {
        return authenticationContext.get();
    }

    public static void clear() {
        authenticatedUser.remove();
        authenticationContext.remove();
    }

    public static Context createContext(UsernamePasswordAuthenticationToken authentication) {

        setAuthenticationContext(authentication);
        setCurrentUser((MoiraiPrincipal) authentication.getPrincipal());

        return ReactiveSecurityContextHolder.withAuthentication(authentication);
    }
}
