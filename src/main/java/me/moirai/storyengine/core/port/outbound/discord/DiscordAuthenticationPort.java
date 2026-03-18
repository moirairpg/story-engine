package me.moirai.storyengine.core.port.outbound.discord;

import me.moirai.storyengine.core.port.inbound.discord.userdetails.AuthenticateUserResult;

public interface DiscordAuthenticationPort {

    AuthenticateUserResult authenticate(DiscordAuthRequest request);

    AuthenticateUserResult refreshSessionToken(RefreshSessionTokenRequest request);

    DiscordUserDataResponse retrieveLoggedUser(String token);

    void logout(String clientId, String clientSecret, String token, String tokenTypeHint);
}
