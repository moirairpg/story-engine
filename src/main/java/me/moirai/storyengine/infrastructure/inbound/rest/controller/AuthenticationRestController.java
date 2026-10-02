package me.moirai.storyengine.infrastructure.inbound.rest.controller;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import me.moirai.storyengine.common.cqs.command.CommandRunner;
import me.moirai.storyengine.common.cqs.query.QueryRunner;
import me.moirai.storyengine.common.security.authentication.SessionCookieWriter;
import me.moirai.storyengine.common.web.SecurityContextAware;
import me.moirai.storyengine.core.port.inbound.userdetails.AuthenticateUser;
import me.moirai.storyengine.core.port.inbound.userdetails.AuthenticateUserResult;
import me.moirai.storyengine.core.port.inbound.userdetails.CreateUser;
import me.moirai.storyengine.core.port.inbound.userdetails.GetAuthenticatedUserDetails;
import me.moirai.storyengine.core.port.inbound.userdetails.GetSignUpDetails;
import me.moirai.storyengine.core.port.inbound.userdetails.SignUpDetailsResult;
import me.moirai.storyengine.core.port.inbound.userdetails.UserDetailsResult;
import me.moirai.storyengine.core.port.outbound.discord.DiscordAuthenticationPort;
import me.moirai.storyengine.infrastructure.inbound.rest.request.CreateUserRequest;

@Hidden
@RestController
@RequestMapping("/auth")
public class AuthenticationRestController extends SecurityContextAware {

    private static final String TOKEN_TYPE_HINT = "access_token";
    private static final String SESSION_COOKIE_NAME = "moirai_sstk";

    private final String clientId;
    private final String clientSecret;
    private final String signInRedirectUri;
    private final String signUpRedirectUri;
    private final String successPath;
    private final String failPath;
    private final String logoutPath;
    private final String signupPath;
    private final String notRegisteredPath;
    private final DiscordAuthenticationPort discordAuthenticationPort;
    private final QueryRunner queryRunner;
    private final CommandRunner commandRunner;
    private final SessionCookieWriter sessionCookieWriter;

    public AuthenticationRestController(
            @Value("${moirai.discord.oauth.client-id}") String clientId,
            @Value("${moirai.discord.oauth.client-secret}") String clientSecret,
            @Value("${moirai.discord.oauth.signin-redirect-url}") String signInRedirectUri,
            @Value("${moirai.discord.oauth.signup-redirect-url}") String signUpRedirectUri,
            @Value("${moirai.security.redirect-path.success}") String successPath,
            @Value("${moirai.security.redirect-path.fail}") String failPath,
            @Value("${moirai.security.redirect-path.logout}") String logoutPath,
            @Value("${moirai.security.redirect-path.signup}") String signupPath,
            @Value("${moirai.security.redirect-path.not-registered}") String notRegisteredPath,
            DiscordAuthenticationPort discordAuthenticationPort,
            QueryRunner queryRunner,
            CommandRunner commandRunner,
            SessionCookieWriter sessionCookieWriter) {

        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.signInRedirectUri = signInRedirectUri;
        this.signUpRedirectUri = signUpRedirectUri;
        this.successPath = successPath;
        this.logoutPath = logoutPath;
        this.failPath = failPath;
        this.signupPath = signupPath;
        this.notRegisteredPath = notRegisteredPath;
        this.discordAuthenticationPort = discordAuthenticationPort;
        this.queryRunner = queryRunner;
        this.commandRunner = commandRunner;
        this.sessionCookieWriter = sessionCookieWriter;
    }

    @GetMapping("/signin/code")
    @ResponseStatus(code = HttpStatus.OK)
    public void signInCodeExchange(
            @RequestParam(required = true) String code,
            HttpServletResponse response) throws IOException {

        if (isBlank(code)) {
            response.sendRedirect(failPath);
            return;
        }

        var authenticatedUser = commandRunner.run(new AuthenticateUser(code, signInRedirectUri));

        if (!authenticatedUser.isRegistered()) {
            response.sendRedirect(notRegisteredPath);
            return;
        }

        handleSessionAuthentication(response, authenticatedUser, successPath);
    }

    @GetMapping("/signup/code")
    @ResponseStatus(code = HttpStatus.OK)
    public void signUpCodeExchange(
            @RequestParam(required = true) String code,
            HttpServletResponse response) throws IOException {

        if (isBlank(code)) {
            response.sendRedirect(failPath);
            return;
        }

        var authenticatedUser = commandRunner.run(new AuthenticateUser(code, signUpRedirectUri));
        var redirectPath = authenticatedUser.isRegistered() ? successPath : signupPath;

        handleSessionAuthentication(response, authenticatedUser, redirectPath);
    }

    @GetMapping("/signup/details")
    @ResponseStatus(code = HttpStatus.OK)
    public SignUpDetailsResult signUpDetails(
            @CookieValue(name = SESSION_COOKIE_NAME) String sessionToken) {

        return queryRunner.run(new GetSignUpDetails(sessionToken));
    }

    @PostMapping("/signup")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void signUp(
            @Valid @RequestBody CreateUserRequest request,
            @CookieValue(name = SESSION_COOKIE_NAME) String sessionToken) {

        commandRunner.run(new CreateUser(sessionToken, request.username(), request.displayName()));
    }

    @PostMapping("/logout")
    @ResponseStatus(code = HttpStatus.OK)
    public void logout(HttpServletResponse response) throws IOException {

        discordAuthenticationPort.logout(clientId, clientSecret,
                getAuthenticatedUser().authorizationToken(), TOKEN_TYPE_HINT);

        handleSessionTermination(response);
    }

    @GetMapping("/user")
    @ResponseStatus(code = HttpStatus.OK)
    public UserDetailsResult getAuthenticatedUserDetails() {

        var query = new GetAuthenticatedUserDetails(getAuthenticatedUser().authorizationToken());

        return queryRunner.run(query);
    }

    private void handleSessionAuthentication(
            HttpServletResponse response,
            AuthenticateUserResult authResult,
            String redirectPath) throws IOException {

        sessionCookieWriter.addSessionCookies(response, authResult);
        response.sendRedirect(redirectPath);
    }

    private void handleSessionTermination(HttpServletResponse response) throws IOException {

        sessionCookieWriter.expireSessionCookies(response);
        response.sendRedirect(logoutPath);
    }
}
