package me.moirai.storyengine.common.security.authentication;

import static me.moirai.storyengine.common.security.authentication.MoiraiCookie.EXPIRY_COOKIE;
import static me.moirai.storyengine.common.security.authentication.MoiraiCookie.REFRESH_COOKIE;
import static me.moirai.storyengine.common.security.authentication.MoiraiCookie.SESSION_COOKIE;

import java.time.Instant;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import me.moirai.storyengine.core.port.inbound.userdetails.AuthenticateUserResult;

@Component
public class SessionCookieWriter {

    private static final String STRICT = "Strict";
    private static final String ROOT = "/";
    private static final String SAME_SITE = "SameSite";
    private static final int EXPIRE_IMMEDIATELY = 0;
    private static final boolean SECURE = true;

    public void addSessionCookies(HttpServletResponse response, AuthenticateUserResult session) {

        var maxAge = Math.toIntExact(session.expiresIn());
        var expiresAt = Instant.now().plusSeconds(session.expiresIn()).getEpochSecond();

        response.addCookie(createCookie(SESSION_COOKIE, session.accessToken(), maxAge));
        response.addCookie(createCookie(REFRESH_COOKIE, session.refreshToken(), maxAge));
        response.addCookie(createCookie(EXPIRY_COOKIE, String.valueOf(expiresAt), maxAge));
    }

    public void expireSessionCookies(HttpServletResponse response) {

        response.addCookie(createCookie(SESSION_COOKIE, null, EXPIRE_IMMEDIATELY));
        response.addCookie(createCookie(REFRESH_COOKIE, null, EXPIRE_IMMEDIATELY));
        response.addCookie(createCookie(EXPIRY_COOKIE, null, EXPIRE_IMMEDIATELY));
    }

    private Cookie createCookie(MoiraiCookie cookie, String cookieValue, int maxAge) {

        var servletCookie = new Cookie(cookie.getName(), cookieValue);
        servletCookie.setHttpOnly(cookie.isHttpOnly());
        servletCookie.setPath(ROOT);
        servletCookie.setAttribute(SAME_SITE, STRICT);
        servletCookie.setSecure(SECURE);
        servletCookie.setMaxAge(maxAge);

        return servletCookie;
    }
}
