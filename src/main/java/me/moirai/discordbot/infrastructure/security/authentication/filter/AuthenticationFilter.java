package me.moirai.discordbot.infrastructure.security.authentication.filter;

import static java.util.Arrays.asList;
import static me.moirai.discordbot.infrastructure.security.authentication.MoiraiCookie.REFRESH_COOKIE;
import static me.moirai.discordbot.infrastructure.security.authentication.MoiraiCookie.SESSION_COOKIE;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.List;

import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatusCode;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import me.moirai.discordbot.infrastructure.security.authentication.MoiraiPrincipal;
import me.moirai.discordbot.infrastructure.security.authentication.MoiraiUserDetailsService;
import me.moirai.discordbot.infrastructure.security.authentication.SecuritySessionContext;
import reactor.core.publisher.Mono;

public class AuthenticationFilter implements WebFilter {

    private static final HttpStatusCode HTTP_UNAUTHORIZED = HttpStatusCode.valueOf(401);

    private final List<String> ignoredPaths;
    private final String authenticationFailedPath;
    private final String authenticationTerminatedPath;
    private final MoiraiUserDetailsService userDetailsService;

    public AuthenticationFilter(String[] ignoredPaths,
            String authenticationFailedPath,
            String authenticationTerminatedPath,
            MoiraiUserDetailsService userDetailsService) {

        this.ignoredPaths = asList(ignoredPaths);
        this.authenticationFailedPath = authenticationFailedPath;
        this.authenticationTerminatedPath = authenticationTerminatedPath;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {

        String requestPath = exchange.getRequest().getPath().value();

        if (isPathInExceptionList(requestPath)) {
            return chain.filter(exchange);
        }

        HttpCookie sessionCookie = exchange.getRequest().getCookies().getFirst(SESSION_COOKIE.getName());
        HttpCookie refreshCookie = exchange.getRequest().getCookies().getFirst(REFRESH_COOKIE.getName());
        if (sessionCookie == null || isBlank(sessionCookie.getValue())) {
            exchange.getResponse().setStatusCode(HTTP_UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return userDetailsService.findByUsername(String.format("%s / %s", sessionCookie.getValue(), refreshCookie.getValue()))
                .flatMap(userDetails -> {
                    MoiraiPrincipal user = (MoiraiPrincipal) userDetails;
                    UsernamePasswordAuthenticationToken authenticatedPrincipal = new UsernamePasswordAuthenticationToken(
                            user, null, user.getAuthorities());

                    return chain.filter(exchange)
                            .contextWrite(SecuritySessionContext.createContext(authenticatedPrincipal));
                });

    }

    private boolean isPathInExceptionList(String path) {

        boolean isAuthFailPath = authenticationFailedPath.equals(path);
        boolean isAuthLogoutPath = authenticationTerminatedPath.equals(path);
        boolean isPathInExceptionList = ignoredPaths.stream().anyMatch(ignoredPath -> ignoredPath.contains(path));

        return isPathInExceptionList || isAuthFailPath || isAuthLogoutPath;
    }
}
