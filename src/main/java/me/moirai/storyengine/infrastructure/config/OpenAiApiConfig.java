package me.moirai.storyengine.infrastructure.config;

import java.io.IOException;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.client.RestClient;

import me.moirai.storyengine.common.exception.RestException;
import me.moirai.storyengine.infrastructure.outbound.adapter.generation.CompletionResponseError;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class OpenAiApiConfig {

    private static final Logger LOG = LoggerFactory.getLogger(OpenAiApiConfig.class);

    private static final String AUTHENTICATION_ERROR = "Error authenticating user on OpenAI";
    private static final String UNKNOWN_ERROR = "Something went wrong. Contact support.";
    private static final String BAD_REQUEST_ERROR = "Bad request calling OpenAI API";
    private static final String BEARER = "Bearer ";

    private static final Predicate<HttpStatusCode> BAD_REQUEST = statusCode -> statusCode
            .isSameCodeAs(HttpStatusCode.valueOf(400));

    private static final Predicate<HttpStatusCode> UNAUTHORIZED = statusCode -> statusCode
            .isSameCodeAs(HttpStatusCode.valueOf(401));

    private final String baseUrl;
    private final String apiToken;
    private final JsonMapper jsonMapper;

    public OpenAiApiConfig(
            @Value("${moirai.openai.api.base-url}") String baseUrl,
            @Value("${moirai.openai.api.token}") String apiToken,
            JsonMapper jsonMapper) {

        this.baseUrl = baseUrl;
        this.apiToken = apiToken;
        this.jsonMapper = jsonMapper;

    }

    @Bean
    RestClient openAiClient() {

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, BEARER + apiToken)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE)
                .defaultStatusHandler(UNAUTHORIZED, this::handleUnauthorized)
                .defaultStatusHandler(BAD_REQUEST, this::handleBadRequest)
                .defaultStatusHandler(HttpStatusCode::isError, this::handleUnknownError)
                .build();
    }

    private void handleUnauthorized(HttpRequest request, ClientHttpResponse response) throws IOException {

        var error = mapErrorResponse(response);
        LOG.error(AUTHENTICATION_ERROR + " -> {}", error);
        throw new RestException(HttpStatus.INTERNAL_SERVER_ERROR, UNKNOWN_ERROR);
    }

    private void handleBadRequest(HttpRequest request, ClientHttpResponse response) throws IOException {

        var error = mapErrorResponse(response);
        LOG.error(BAD_REQUEST_ERROR + " -> {}", error);
        throw new RestException(HttpStatus.BAD_REQUEST, error.getType(), error.getMessage(),
                String.format(BAD_REQUEST_ERROR, error.getType(), error.getMessage()));
    }

    private void handleUnknownError(HttpRequest request, ClientHttpResponse response) throws IOException {

        var error = mapErrorResponse(response);
        LOG.error(UNKNOWN_ERROR + " -> {}", error);
        throw new RestException(HttpStatus.INTERNAL_SERVER_ERROR, error.getType(), error.getMessage(),
                String.format(UNKNOWN_ERROR, error.getType(), error.getMessage()));
    }

    private CompletionResponseError mapErrorResponse(ClientHttpResponse response) throws IOException {
        return jsonMapper.readValue(response.getBody(), CompletionResponseError.class);
    }
}
