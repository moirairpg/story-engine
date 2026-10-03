package me.moirai.storyengine.common.security.authentication;

import static me.moirai.storyengine.common.security.authentication.MoiraiCookie.STATE_COOKIE;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class AuthorizationStateCookieTest {

    private static final String STATE = "f3Kq9VxW2mZ8bT1nR6yH4cL0aJ7sD5eG";
    private static final String OTHER_STATE = "Qp2Lm8Nz4Rt6Vx0Bc3Df5Gh7Jk9Wy1Ae";
    private static final String STATE_PATTERN = "^[A-Za-z0-9_-]{43}$";
    private static final int TEN_MINUTES = 600;

    @InjectMocks
    private AuthorizationStateCookie authorizationStateCookie;

    @Test
    void shouldSetHostPrefixedHttpOnlySecureLaxCookieHoldingTheIssuedStateWhenStateIsIssued() {

        // given
        var response = new MockHttpServletResponse();

        // when
        var state = authorizationStateCookie.issue(response);

        // then
        var cookie = response.getCookie(STATE_COOKIE.getName());
        assertThat(state).matches(STATE_PATTERN);
        assertThat(cookie).isNotNull();
        assertThat(cookie.getName()).isEqualTo("__Host-moirai_state");
        assertThat(cookie.getValue()).isEqualTo(state);
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getSecure()).isTrue();
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getDomain()).isNull();
        assertThat(cookie.getAttribute("SameSite")).isEqualTo("Lax");
        assertThat(cookie.getMaxAge()).isEqualTo(TEN_MINUTES);
    }

    @Test
    void shouldIssueDifferentStateWhenIssuedTwice() {

        // given
        var firstResponse = new MockHttpServletResponse();
        var secondResponse = new MockHttpServletResponse();

        // when
        var firstState = authorizationStateCookie.issue(firstResponse);
        var secondState = authorizationStateCookie.issue(secondResponse);

        // then
        assertThat(firstState).isNotEqualTo(secondState);
    }

    @Test
    void shouldMatchWhenReturnedStateEqualsIssuedState() {

        // when
        var matches = authorizationStateCookie.matches(STATE, STATE);

        // then
        assertThat(matches).isTrue();
    }

    @Test
    void shouldNotMatchWhenReturnedStateDiffersFromIssuedState() {

        // when
        var matches = authorizationStateCookie.matches(STATE, OTHER_STATE);

        // then
        assertThat(matches).isFalse();
    }

    @Test
    void shouldNotMatchWhenNoStateWasIssued() {

        // when
        var matches = authorizationStateCookie.matches(null, STATE);

        // then
        assertThat(matches).isFalse();
    }

    @Test
    void shouldNotMatchWhenNoStateWasReturned() {

        // when
        var matches = authorizationStateCookie.matches(STATE, null);

        // then
        assertThat(matches).isFalse();
    }

    @Test
    void shouldNotMatchWhenBothStatesAreBlank() {

        // when
        var matches = authorizationStateCookie.matches("", "");

        // then
        assertThat(matches).isFalse();
    }

    @Test
    void shouldClearCookieWhenStateIsExpired() {

        // given
        var response = new MockHttpServletResponse();

        // when
        authorizationStateCookie.expire(response);

        // then
        var cookie = response.getCookie(STATE_COOKIE.getName());
        assertThat(cookie).isNotNull();
        assertThat(cookie.getMaxAge()).isZero();
        assertThat(cookie.getPath()).isEqualTo("/");
    }
}
