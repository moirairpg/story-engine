package me.moirai.storyengine.common.web;

import java.util.function.Function;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;

import me.moirai.storyengine.infrastructure.security.authentication.MoiraiPrincipal;
import reactor.core.publisher.Mono;

public abstract class SecurityContextAware {

    protected <T> Mono<T> mapWithAuthenticatedUser(Function<MoiraiPrincipal, T> function) {

        return mapWithAuthenticatedPrincipal(authentication -> (MoiraiPrincipal) authentication.getPrincipal())
                .map(function);
    }

    protected <T> Mono<T> flatMapWithAuthenticatedUser(
            Function<? super MoiraiPrincipal, ? extends Mono<? extends T>> function) {

        return mapWithAuthenticatedPrincipal(authentication -> (MoiraiPrincipal) authentication.getPrincipal())
                .flatMap(function);
    }

    protected <T> Mono<T> mapWithAuthenticatedPrincipal(Function<Authentication, T> function) {

        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(function);
    }
}
