package me.moirai.storyengine.common.security.authentication;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.time.Instant;
import java.util.Optional;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import jakarta.servlet.http.HttpServletResponse;
import me.moirai.storyengine.common.exception.AuthenticationFailedException;
import me.moirai.storyengine.common.exception.RestException;
import me.moirai.storyengine.core.port.inbound.userdetails.AuthenticateUserResult;
import me.moirai.storyengine.core.port.outbound.discord.DiscordAuthenticationPort;
import me.moirai.storyengine.core.port.outbound.discord.RefreshSessionTokenRequest;

@Service
public class SessionRenewalService {

    private static final Logger LOG = LoggerFactory.getLogger(SessionRenewalService.class);

    private static final String DISCORD_GRANT_TYPE = "refresh_token";
    private static final long RENEWAL_MARGIN_SECONDS = 24 * 60 * 60;
    private static final String RENEWAL_FAILED = "Session renewal failed; the current session stays valid until it expires";

    private final String clientId;
    private final String clientSecret;
    private final DiscordAuthenticationPort discordAuthenticationPort;
    private final SessionCookieWriter sessionCookieWriter;

    public SessionRenewalService(
            @Value("${moirai.discord.oauth.client-id}") String clientId,
            @Value("${moirai.discord.oauth.client-secret}") String clientSecret,
            DiscordAuthenticationPort discordAuthenticationPort,
            SessionCookieWriter sessionCookieWriter) {

        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.discordAuthenticationPort = discordAuthenticationPort;
        this.sessionCookieWriter = sessionCookieWriter;
    }

    public Optional<AuthenticateUserResult> renewWhenDue(
            String refreshToken,
            String sessionExpiry,
            HttpServletResponse response) {

        if (isBlank(refreshToken) || !isRenewalDue(sessionExpiry)) {
            return Optional.empty();
        }

        try {
            var renewedSession = discordAuthenticationPort.refreshSessionToken(createRefreshRequest(refreshToken));
            sessionCookieWriter.addSessionCookies(response, renewedSession);

            return Optional.of(renewedSession);
        } catch (AuthenticationFailedException | RestException | RestClientException e) {
            LOG.warn(RENEWAL_FAILED, e);
            return Optional.empty();
        }
    }

    private boolean isRenewalDue(String sessionExpiry) {

        var expiresAt = NumberUtils.toLong(sessionExpiry);
        var secondsLeft = expiresAt - Instant.now().getEpochSecond();

        return secondsLeft <= RENEWAL_MARGIN_SECONDS;
    }

    private RefreshSessionTokenRequest createRefreshRequest(String refreshToken) {

        return RefreshSessionTokenRequest.builder()
                .refreshToken(refreshToken)
                .grantType(DISCORD_GRANT_TYPE)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
    }
}
