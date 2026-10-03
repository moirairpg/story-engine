package me.moirai.storyengine.common.security.authentication.filter;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CrossSiteRequestFilter extends OncePerRequestFilter {

    public static final String CLIENT_HEADER = "X-Moirai-Client";
    public static final String FETCH_SITE_HEADER = "Sec-Fetch-Site";

    private static final int HTTP_FORBIDDEN = 403;
    private static final int NO_PORT = -1;
    private static final Set<String> SAFE_METHODS = Set.of(
            HttpMethod.GET.name(),
            HttpMethod.HEAD.name(),
            HttpMethod.OPTIONS.name());
    private static final Set<String> TRUSTED_FETCH_SITES = Set.of("same-origin", "none");

    private final List<String> allowedOrigins;

    public CrossSiteRequestFilter(String[] allowedOrigins) {

        this.allowedOrigins = asList(allowedOrigins);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        var isStateChanging = !SAFE_METHODS.contains(request.getMethod());

        if (isStateChanging && !isFromMoiraiClient(request)) {
            response.setStatus(HTTP_FORBIDDEN);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isFromMoiraiClient(HttpServletRequest request) {

        return hasClientHeader(request) && isFromTrustedSite(request) && isFromAllowedOrigin(request);
    }

    private boolean hasClientHeader(HttpServletRequest request) {

        return !isBlank(request.getHeader(CLIENT_HEADER));
    }

    private boolean isFromTrustedSite(HttpServletRequest request) {

        var fetchSite = request.getHeader(FETCH_SITE_HEADER);

        return fetchSite == null || TRUSTED_FETCH_SITES.contains(fetchSite);
    }

    private boolean isFromAllowedOrigin(HttpServletRequest request) {

        var origin = request.getHeader(HttpHeaders.ORIGIN);

        if (origin != null) {
            return allowedOrigins.contains(origin);
        }

        var referer = request.getHeader(HttpHeaders.REFERER);

        return referer != null && allowedOrigins.contains(originOf(referer));
    }

    private String originOf(String url) {

        try {
            var uri = URI.create(url);

            if (uri.getScheme() == null || uri.getHost() == null) {
                return null;
            }

            var port = uri.getPort() == NO_PORT ? "" : ":" + uri.getPort();

            return uri.getScheme() + "://" + uri.getHost() + port;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
