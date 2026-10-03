package me.moirai.storyengine.common.security.authentication;

import static java.nio.charset.StandardCharsets.UTF_8;
import static me.moirai.storyengine.common.security.authentication.MoiraiCookie.STATE_COOKIE;
import static org.apache.commons.lang3.StringUtils.isAnyBlank;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthorizationStateCookie {

    private static final String LAX = "Lax";
    private static final String ROOT = "/";
    private static final String SAME_SITE = "SameSite";
    private static final int STATE_BYTES = 32;
    private static final int TEN_MINUTES = 600;
    private static final int EXPIRE_IMMEDIATELY = 0;
    private static final boolean SECURE = true;

    private final SecureRandom secureRandom = new SecureRandom();

    public String issue(HttpServletResponse response) {

        var stateBytes = new byte[STATE_BYTES];
        secureRandom.nextBytes(stateBytes);

        var state = Base64.getUrlEncoder().withoutPadding().encodeToString(stateBytes);
        response.addCookie(createCookie(state, TEN_MINUTES));

        return state;
    }

    public boolean matches(String issuedState, String returnedState) {

        if (isAnyBlank(issuedState, returnedState)) {
            return false;
        }

        return MessageDigest.isEqual(issuedState.getBytes(UTF_8), returnedState.getBytes(UTF_8));
    }

    public void expire(HttpServletResponse response) {

        response.addCookie(createCookie(null, EXPIRE_IMMEDIATELY));
    }

    private Cookie createCookie(String cookieValue, int maxAge) {

        var servletCookie = new Cookie(STATE_COOKIE.getName(), cookieValue);
        servletCookie.setHttpOnly(STATE_COOKIE.isHttpOnly());
        servletCookie.setPath(ROOT);
        servletCookie.setAttribute(SAME_SITE, LAX);
        servletCookie.setSecure(SECURE);
        servletCookie.setMaxAge(maxAge);

        return servletCookie;
    }
}
