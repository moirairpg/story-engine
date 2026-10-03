package me.moirai.storyengine.common.security.authentication.filter;

import static me.moirai.storyengine.common.security.authentication.filter.CrossSiteRequestFilter.CLIENT_HEADER;
import static me.moirai.storyengine.common.security.authentication.filter.CrossSiteRequestFilter.FETCH_SITE_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;

@ExtendWith(MockitoExtension.class)
class CrossSiteRequestFilterTest {

    private static final String CLIENT_NAME = "moirai-ui";
    private static final String REQUEST_PATH = "/worlds";
    private static final String ALLOWED_ORIGIN = "https://moirai.app";
    private static final String[] ALLOWED_ORIGINS = { ALLOWED_ORIGIN };
    private static final int HTTP_OK = 200;
    private static final int HTTP_FORBIDDEN = 403;

    @Mock
    private FilterChain filterChain;

    private CrossSiteRequestFilter filter;

    @BeforeEach
    void setUp() {

        filter = new CrossSiteRequestFilter(ALLOWED_ORIGINS);
    }

    @ParameterizedTest
    @ValueSource(strings = { "POST", "PUT", "PATCH", "DELETE", "PROPFIND" })
    void shouldRejectRequestWhenStateChangingMethodHasNoClientHeader(String method) throws Exception {

        // given
        var request = new MockHttpServletRequest(method, REQUEST_PATH);
        request.addHeader(HttpHeaders.ORIGIN, ALLOWED_ORIGIN);
        var response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(response.getStatus()).isEqualTo(HTTP_FORBIDDEN);
        verifyNoInteractions(filterChain);
    }

    @ParameterizedTest
    @ValueSource(strings = { "application/x-www-form-urlencoded", "multipart/form-data", "text/plain", "application/json" })
    void shouldRejectRequestWhenClientHeaderIsMissingWhateverTheContentType(String contentType) throws Exception {

        // given
        var request = new MockHttpServletRequest("POST", REQUEST_PATH);
        request.setContentType(contentType);
        request.addHeader(HttpHeaders.ORIGIN, ALLOWED_ORIGIN);
        var response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(response.getStatus()).isEqualTo(HTTP_FORBIDDEN);
        verifyNoInteractions(filterChain);
    }

    @Test
    void shouldRejectRequestWhenClientHeaderIsBlank() throws Exception {

        // given
        var request = new MockHttpServletRequest("POST", REQUEST_PATH);
        request.addHeader(CLIENT_HEADER, " ");
        request.addHeader(HttpHeaders.ORIGIN, ALLOWED_ORIGIN);
        var response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(response.getStatus()).isEqualTo(HTTP_FORBIDDEN);
        verifyNoInteractions(filterChain);
    }

    @ParameterizedTest
    @ValueSource(strings = { "POST", "PUT", "PATCH", "DELETE" })
    void shouldContinueChainWhenWriteComesFromAllowedOriginWithClientHeader(String method) throws Exception {

        // given
        var request = new MockHttpServletRequest(method, REQUEST_PATH);
        request.addHeader(CLIENT_HEADER, CLIENT_NAME);
        request.addHeader(HttpHeaders.ORIGIN, ALLOWED_ORIGIN);
        request.addHeader(FETCH_SITE_HEADER, "same-origin");
        var response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(response.getStatus()).isEqualTo(HTTP_OK);
        verify(filterChain).doFilter(request, response);
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://evil.com", "https://moirai.app.evil.com", "http://moirai.app", "https://moirai.app/", "null" })
    void shouldRejectRequestWhenWriteComesFromForeignOrigin(String origin) throws Exception {

        // given
        var request = new MockHttpServletRequest("POST", REQUEST_PATH);
        request.addHeader(CLIENT_HEADER, CLIENT_NAME);
        request.addHeader(HttpHeaders.ORIGIN, origin);
        var response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(response.getStatus()).isEqualTo(HTTP_FORBIDDEN);
        verifyNoInteractions(filterChain);
    }

    @Test
    void shouldContinueChainWhenWriteHasNoOriginButRefererFromAllowedOrigin() throws Exception {

        // given
        var request = new MockHttpServletRequest("POST", REQUEST_PATH);
        request.addHeader(CLIENT_HEADER, CLIENT_NAME);
        request.addHeader(HttpHeaders.REFERER, ALLOWED_ORIGIN + "/world/123/edit?tab=details");
        var response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(response.getStatus()).isEqualTo(HTTP_OK);
        verify(filterChain).doFilter(request, response);
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://evil.com/page", "https://moirai.app.evil.com/world/1", "not a url", "/relative/path" })
    void shouldRejectRequestWhenWriteHasNoOriginAndRefererIsForeignOrInvalid(String referer) throws Exception {

        // given
        var request = new MockHttpServletRequest("POST", REQUEST_PATH);
        request.addHeader(CLIENT_HEADER, CLIENT_NAME);
        request.addHeader(HttpHeaders.REFERER, referer);
        var response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(response.getStatus()).isEqualTo(HTTP_FORBIDDEN);
        verifyNoInteractions(filterChain);
    }

    @Test
    void shouldRejectRequestWhenWriteHasNeitherOriginNorReferer() throws Exception {

        // given
        var request = new MockHttpServletRequest("POST", REQUEST_PATH);
        request.addHeader(CLIENT_HEADER, CLIENT_NAME);
        var response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(response.getStatus()).isEqualTo(HTTP_FORBIDDEN);
        verifyNoInteractions(filterChain);
    }

    @ParameterizedTest
    @ValueSource(strings = { "cross-site", "same-site" })
    void shouldRejectRequestWhenFetchMetadataMarksWriteAsNotSameOrigin(String fetchSite) throws Exception {

        // given
        var request = new MockHttpServletRequest("POST", REQUEST_PATH);
        request.addHeader(CLIENT_HEADER, CLIENT_NAME);
        request.addHeader(HttpHeaders.ORIGIN, ALLOWED_ORIGIN);
        request.addHeader(FETCH_SITE_HEADER, fetchSite);
        var response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(response.getStatus()).isEqualTo(HTTP_FORBIDDEN);
        verifyNoInteractions(filterChain);
    }

    @ParameterizedTest
    @ValueSource(strings = { "same-origin", "none" })
    void shouldContinueChainWhenFetchMetadataIsTrusted(String fetchSite) throws Exception {

        // given
        var request = new MockHttpServletRequest("POST", REQUEST_PATH);
        request.addHeader(CLIENT_HEADER, CLIENT_NAME);
        request.addHeader(HttpHeaders.ORIGIN, ALLOWED_ORIGIN);
        request.addHeader(FETCH_SITE_HEADER, fetchSite);
        var response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(response.getStatus()).isEqualTo(HTTP_OK);
        verify(filterChain).doFilter(request, response);
    }

    @ParameterizedTest
    @ValueSource(strings = { "GET", "HEAD", "OPTIONS" })
    void shouldContinueChainWhenSafeMethodComesFromAnywhere(String method) throws Exception {

        // given
        var request = new MockHttpServletRequest(method, REQUEST_PATH);
        request.addHeader(HttpHeaders.ORIGIN, "https://evil.com");
        request.addHeader(FETCH_SITE_HEADER, "cross-site");
        var response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(response.getStatus()).isEqualTo(HTTP_OK);
        verify(filterChain).doFilter(request, response);
    }
}
