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
import org.springframework.web.util.UriComponentsBuilder;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import me.moirai.storyengine.common.cqs.command.CommandRunner;
import me.moirai.storyengine.common.cqs.query.QueryRunner;
import me.moirai.storyengine.common.security.authentication.AuthorizationStateCookie;
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
    private static final String STATE_COOKIE_NAME = "__Host-moirai_state";
    private static final String CLIENT_ID_PARAM = "client_id";
    private static final String RESPONSE_TYPE_PARAM = "response_type";
    private static final String REDIRECT_URI_PARAM = "redirect_uri";
    private static final String SCOPE_PARAM = "scope";
    private static final String STATE_PARAM = "state";
    private static final String CODE_RESPONSE_TYPE = "code";
    private static final String IDENTIFY_SCOPE = "identify";

    private final String clientId;
    private final String clientSecret;
    private final String signInRedirectUri;
    private final String signUpRedirectUri;
    private final String authorizeUrl;
    private final String successPath;
    private final String failPath;
    private final String logoutPath;
    private final String signupPath;
    private final String notRegisteredPath;
    private final DiscordAuthenticationPort discordAuthenticationPort;
    private final QueryRunner queryRunner;
    private final CommandRunner commandRunner;
    private final SessionCookieWriter sessionCookieWriter;
    private final AuthorizationStateCookie authorizationStateCookie;

    public AuthenticationRestController(
            @Value("${moirai.discord.oauth.client-id}") String clientId,
            @Value("${moirai.discord.oauth.client-secret}") String clientSecret,
            @Value("${moirai.discord.oauth.signin-redirect-url}") String signInRedirectUri,
            @Value("${moirai.discord.oauth.signup-redirect-url}") String signUpRedirectUri,
            @Value("${moirai.discord.oauth.authorize-url}") String authorizeUrl,
            @Value("${moirai.security.redirect-path.success}") String successPath,
            @Value("${moirai.security.redirect-path.fail}") String failPath,
            @Value("${moirai.security.redirect-path.logout}") String logoutPath,
            @Value("${moirai.security.redirect-path.signup}") String signupPath,
            @Value("${moirai.security.redirect-path.not-registered}") String notRegisteredPath,
            DiscordAuthenticationPort discordAuthenticationPort,
            QueryRunner queryRunner,
            CommandRunner commandRunner,
            SessionCookieWriter sessionCookieWriter,
            AuthorizationStateCookie authorizationStateCookie) {

        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.signInRedirectUri = signInRedirectUri;
        this.signUpRedirectUri = signUpRedirectUri;
        this.authorizeUrl = authorizeUrl;
        this.successPath = successPath;
        this.logoutPath = logoutPath;
        this.failPath = failPath;
        this.signupPath = signupPath;
        this.notRegisteredPath = notRegisteredPath;
        this.discordAuthenticationPort = discordAuthenticationPort;
        this.queryRunner = queryRunner;
        this.commandRunner = commandRunner;
        this.sessionCookieWriter = sessionCookieWriter;
        this.authorizationStateCookie = authorizationStateCookie;
    }

    @GetMapping("/signin/authorize")
    @ResponseStatus(code = HttpStatus.FOUND)
    public void signInAuthorize(HttpServletResponse response) throws IOException {

        redirectToDiscord(response, signInRedirectUri);
    }

    @GetMapping("/signup/authorize")
    @ResponseStatus(code = HttpStatus.FOUND)
    public void signUpAuthorize(HttpServletResponse response) throws IOException {

        redirectToDiscord(response, signUpRedirectUri);
    }

    @GetMapping("/signin/code")
    @ResponseStatus(code = HttpStatus.OK)
    public void signInCodeExchange(
            @RequestParam(required = true) String code,
            @RequestParam(required = false) String state,
            @CookieValue(name = STATE_COOKIE_NAME, required = false) String issuedState,
            HttpServletResponse response) throws IOException {

        authorizationStateCookie.expire(response);

        if (isBlank(code) || !authorizationStateCookie.matches(issuedState, state)) {
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
            @RequestParam(required = false) String state,
            @CookieValue(name = STATE_COOKIE_NAME, required = false) String issuedState,
            HttpServletResponse response) throws IOException {

        authorizationStateCookie.expire(response);

        if (isBlank(code) || !authorizationStateCookie.matches(issuedState, state)) {
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

    private void redirectToDiscord(HttpServletResponse response, String redirectUri) throws IOException {

        var state = authorizationStateCookie.issue(response);
        var discordAuthorizeUrl = UriComponentsBuilder.fromUriString(authorizeUrl)
                .queryParam(CLIENT_ID_PARAM, clientId)
                .queryParam(RESPONSE_TYPE_PARAM, CODE_RESPONSE_TYPE)
                .queryParam(REDIRECT_URI_PARAM, redirectUri)
                .queryParam(SCOPE_PARAM, IDENTIFY_SCOPE)
                .queryParam(STATE_PARAM, state)
                .encode()
                .toUriString();

        response.sendRedirect(discordAuthorizeUrl);
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
