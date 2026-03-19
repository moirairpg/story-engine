package me.moirai.storyengine.common.security.authentication.filter;

import static java.util.Arrays.asList;
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
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authentication.MoiraiUserDetailsService;

public class AuthenticationFilter extends OncePerRequestFilter {

    private static final int HTTP_UNAUTHORIZED = 401;

    private final List<String> unsecuredPaths;
    private final String authenticationFailedPath;
    private final String authenticationTerminatedPath;
    private final MoiraiUserDetailsService userDetailsService;

    public AuthenticationFilter(
            String[] unsecuredPaths,
            String authenticationFailedPath,
            String authenticationTerminatedPath,
            MoiraiUserDetailsService userDetailsService) {

        this.unsecuredPaths = asList(unsecuredPaths);
        this.authenticationFailedPath = authenticationFailedPath;
        this.authenticationTerminatedPath = authenticationTerminatedPath;
        this.userDetailsService = userDetailsService;
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
        var userDetails = userDetailsService.loadUserByUsername(tokenCluster);
        var user = (MoiraiPrincipal) userDetails;
        var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
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
