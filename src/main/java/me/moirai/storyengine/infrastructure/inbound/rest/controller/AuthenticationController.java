package me.moirai.storyengine.infrastructure.inbound.rest.controller;

import static me.moirai.storyengine.common.security.authentication.MoiraiCookie.EXPIRY_COOKIE;
import static me.moirai.storyengine.common.security.authentication.MoiraiCookie.REFRESH_COOKIE;
import static me.moirai.storyengine.common.security.authentication.MoiraiCookie.SESSION_COOKIE;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import me.moirai.storyengine.common.cqs.query.QueryRunner;
import me.moirai.storyengine.common.security.authentication.MoiraiCookie;
import me.moirai.storyengine.common.web.SecurityContextAware;
import me.moirai.storyengine.core.port.inbound.userdetails.AuthenticateUser;
import me.moirai.storyengine.core.port.inbound.userdetails.AuthenticateUserResult;
import me.moirai.storyengine.core.port.inbound.userdetails.GetUserDetailsById;
import me.moirai.storyengine.core.port.inbound.userdetails.RefreshSessionToken;
import me.moirai.storyengine.core.port.inbound.userdetails.UserDetailsResult;
import me.moirai.storyengine.core.port.outbound.discord.DiscordAuthenticationPort;

@Hidden
@RestController
@RequestMapping("/auth")
public class AuthenticationController extends SecurityContextAware {

    private static final String NONE = "None";
    private static final String ROOT = "/";
    private static final String TOKEN_TYPE_HINT = "access_token";
    private static final int EXPIRE_IMMEDIATELY = 0;
    private static final boolean SECURE = true;

    private final String clientId;
    private final String clientSecret;
    private final String successPath;
    private final String failPath;
    private final String logoutPath;
    private final DiscordAuthenticationPort discordAuthenticationPort;
    private final QueryRunner queryRunner;

    public AuthenticationController(
            @Value("${moirai.discord.oauth.client-id}") String clientId,
            @Value("${moirai.discord.oauth.client-secret}") String clientSecret,
            @Value("${moirai.discord.oauth.redirect-url}") String redirectUrl,
            @Value("${moirai.security.redirect-path.success}") String successPath,
            @Value("${moirai.security.redirect-path.fail}") String failPath,
            @Value("${moirai.security.redirect-path.logout}") String logoutPath,
            DiscordAuthenticationPort discordAuthenticationPort,
            QueryRunner queryRunner) {

        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.successPath = successPath;
        this.logoutPath = logoutPath;
        this.failPath = failPath;
        this.discordAuthenticationPort = discordAuthenticationPort;
        this.queryRunner = queryRunner;
    }

    @GetMapping("/code")
    @ResponseStatus(code = HttpStatus.OK)
    public void codeExchange(
            @RequestParam(required = false) String code,
            HttpServletResponse response) throws IOException {

        if (isBlank(code)) {
            response.sendRedirect(failPath);
            return;
        }

        var query = new AuthenticateUser(code);
        var authenticatedUser = queryRunner.run(query);

        handleSessionAuthentication(response, authenticatedUser, successPath);
    }

    @PostMapping("/logout")
    @ResponseStatus(code = HttpStatus.OK)
    public void logout(HttpServletResponse response) throws IOException {

        discordAuthenticationPort.logout(clientId, clientSecret,
                getAuthenticatedUser().authorizationToken(), TOKEN_TYPE_HINT);

        handleSessionTermination(response);
    }

    @PostMapping("/refresh")
    @ResponseStatus(code = HttpStatus.OK)
    public void refreshSession(HttpServletResponse response) throws IOException {

        var query = new RefreshSessionToken(getAuthenticatedUser().refreshToken());
        var authenticatedUser = queryRunner.run(query);

        handleSessionAuthentication(response, authenticatedUser, successPath);
    }

    @GetMapping("/user")
    @ResponseStatus(code = HttpStatus.OK)
    public UserDetailsResult getAuthenticatedUserDetails() {

        var query = new GetUserDetailsById(
                getAuthenticatedUser().publicId(),
                getAuthenticatedUser().authorizationToken());

        return queryRunner.run(query);
    }

    private void handleSessionAuthentication(
            HttpServletResponse response,
            AuthenticateUserResult authResult,
            String redirectPath) throws IOException {

        response.addCookie(createCookie(SESSION_COOKIE, authResult.accessToken()));
        response.addCookie(createCookie(REFRESH_COOKIE, authResult.refreshToken()));
        response.addCookie(createCookie(EXPIRY_COOKIE, String.valueOf(authResult.expiresIn())));
        response.sendRedirect(redirectPath);
    }

    private void handleSessionTermination(HttpServletResponse response) throws IOException {

        response.addCookie(expireCookie(SESSION_COOKIE));
        response.addCookie(expireCookie(REFRESH_COOKIE));
        response.addCookie(expireCookie(EXPIRY_COOKIE));
        response.sendRedirect(logoutPath);
    }

    private Cookie createCookie(MoiraiCookie cookie, String cookieValue) {

        var servletCookie = new Cookie(cookie.getName(), cookieValue);
        servletCookie.setHttpOnly(cookie.isHttpOnly());
        servletCookie.setPath(ROOT);
        servletCookie.setAttribute("SameSite", NONE);
        servletCookie.setSecure(SECURE);

        return servletCookie;
    }

    private Cookie expireCookie(MoiraiCookie cookie) {

        var servletCookie = new Cookie(cookie.getName(), null);
        servletCookie.setHttpOnly(cookie.isHttpOnly());
        servletCookie.setPath(ROOT);
        servletCookie.setAttribute("SameSite", NONE);
        servletCookie.setSecure(SECURE);
        servletCookie.setMaxAge(EXPIRE_IMMEDIATELY);

        return servletCookie;
    }
}
