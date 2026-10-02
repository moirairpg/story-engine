package me.moirai.storyengine.common.security.authentication.filter;

import static java.util.Arrays.asList;
import static me.moirai.storyengine.common.security.authentication.MoiraiCookie.EXPIRY_COOKIE;
import static me.moirai.storyengine.common.security.authentication.MoiraiCookie.REFRESH_COOKIE;
import static me.moirai.storyengine.common.security.authentication.MoiraiCookie.SESSION_COOKIE;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.IOException;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.moirai.storyengine.common.exception.AuthenticationFailedException;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authentication.MoiraiSecurityContext;
import me.moirai.storyengine.common.security.authentication.MoiraiUserDetailsService;
import me.moirai.storyengine.common.security.authentication.SessionRenewalService;
import me.moirai.storyengine.core.port.inbound.userdetails.AuthenticateUserResult;

public class AuthenticationFilter extends OncePerRequestFilter {

    public static final String SESSION_RENEWED_HEADER = "X-Session-Renewed";

    private static final int HTTP_UNAUTHORIZED = 401;
    private static final String WEBSOCKET_PATH = "/ws";
    private static final String LOGOUT_PATH = "/auth/logout";

    private final List<String> unsecuredPaths;
    private final String authenticationFailedPath;
    private final String authenticationTerminatedPath;
    private final MoiraiUserDetailsService userDetailsService;
    private final SessionRenewalService sessionRenewalService;

    public AuthenticationFilter(
            String[] unsecuredPaths,
            String authenticationFailedPath,
            String authenticationTerminatedPath,
            MoiraiUserDetailsService userDetailsService,
            SessionRenewalService sessionRenewalService) {

        this.unsecuredPaths = asList(unsecuredPaths);
        this.authenticationFailedPath = authenticationFailedPath;
        this.authenticationTerminatedPath = authenticationTerminatedPath;
        this.userDetailsService = userDetailsService;
        this.sessionRenewalService = sessionRenewalService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        var requestPath = request.getRequestURI();

        if (isPathInExceptionList(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        var sessionCookieValue = getCookieValue(request, SESSION_COOKIE.getName());
        var refreshCookieValue = getCookieValue(request, REFRESH_COOKIE.getName());

        if (isBlank(sessionCookieValue)) {
            response.setStatus(HTTP_UNAUTHORIZED);
            return;
        }

        var tokenCluster = String.format("%s / %s", sessionCookieValue, refreshCookieValue);

        try {
            var userDetails = userDetailsService.loadUserByUsername(tokenCluster);
            var user = (MoiraiPrincipal) userDetails;
            var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (AuthenticationFailedException e) {
            response.setStatus(HTTP_UNAUTHORIZED);
            return;
        }

        var isRenewable = !requestPath.startsWith(WEBSOCKET_PATH) && !requestPath.equals(LOGOUT_PATH);

        if (isRenewable) {
            var sessionExpiry = getCookieValue(request, EXPIRY_COOKIE.getName());
            sessionRenewalService.renewWhenDue(refreshCookieValue, sessionExpiry, response)
                    .ifPresent(renewedSession -> useRenewedSession(renewedSession, response));
        }

        filterChain.doFilter(request, response);
    }

    private void useRenewedSession(AuthenticateUserResult renewedSession, HttpServletResponse response) {

        var user = MoiraiSecurityContext.getAuthenticatedUser()
                .withTokens(renewedSession.accessToken(), renewedSession.refreshToken());

        var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
        response.setHeader(SESSION_RENEWED_HEADER, Boolean.TRUE.toString());
    }

    private String getCookieValue(HttpServletRequest request, String cookieName) {

        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    private boolean isPathInExceptionList(String path) {

        var isAuthFailPath = authenticationFailedPath.equals(path);
        var isAuthLogoutPath = authenticationTerminatedPath.equals(path);
        var isPathInExceptionList = unsecuredPaths.stream().anyMatch(ignoredPath -> ignoredPath.contains(path));

        return isPathInExceptionList || isAuthFailPath || isAuthLogoutPath;
    }
}
