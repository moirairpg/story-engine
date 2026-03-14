package me.moirai.storyengine.infrastructure.inbound.rest.errorhandler;

import static java.lang.String.format;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.resource.NoResourceFoundException;

import io.micrometer.common.util.StringUtils;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.exception.AuthenticationFailedException;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
import me.moirai.storyengine.common.exception.DiscordApiException;
import me.moirai.storyengine.common.exception.ModerationException;
import me.moirai.storyengine.common.exception.OpenAiApiException;
import me.moirai.storyengine.infrastructure.inbound.rest.response.ErrorResponse;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class WebApiExceptionHandler extends AbstractErrorWebExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(WebApiExceptionHandler.class);

    private static final String ERROR_PROP_CONVERSION = "Failed to convert property value of type";
    private static final String INVALID_VALUE_FOR_FIELD = "Invalid value for field %s";
    private static final String TOPIC_FLAGGED_IN_CONTENT = "Topic flagged in content: %s";
    private static final String UNKNOWN_ERROR = "An error has occurred. Please contact support.";
    private static final String ASSET_NOT_FOUND_ERROR = "The asset requested could not be found.";
    private static final String RESOURCE_NOT_FOUND_ERROR = "The endpoint requested could not be found.";

    public WebApiExceptionHandler(ErrorAttributes errorAttributes, WebProperties webProperties,
            ApplicationContext applicationContext, ServerCodecConfigurer configurer) {

        super(errorAttributes, webProperties.getResources(), applicationContext);
        this.setMessageWriters(configurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {

        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {

        Throwable originalException = getError(request);

        if (originalException instanceof AuthenticationFailedException) {
            return handleAuthenticationError(originalException);
        }

        if (originalException instanceof DiscordApiException) {
            return handleDiscordApiError(originalException);
        }

        if (originalException instanceof OpenAiApiException) {
            return handleOpenAiApiError(originalException);
        }

        LOG.error("Unknown exception caught", originalException);
        return ServerResponse.status(500)
                .bodyValue(ErrorResponse.builder()
                        .code(HttpStatus.INTERNAL_SERVER_ERROR)
                        .message(UNKNOWN_ERROR)
                        .build());
    }

    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    @ExceptionHandler(AssetNotFoundException.class)
    public ResponseEntity<ErrorResponse> assetNotFound(AssetNotFoundException exception) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(HttpStatus.NOT_FOUND)
                .message(ASSET_NOT_FOUND_ERROR)
                .details(Collections.singletonList(exception.getMessage()))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> resourceNotFound(NoResourceFoundException exception) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(HttpStatus.NOT_FOUND)
                .message(RESOURCE_NOT_FOUND_ERROR)
                .details(Collections.singletonList(exception.getMessage()))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(code = HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ErrorResponse> assetNotFound(BusinessRuleViolationException exception) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(HttpStatus.UNPROCESSABLE_ENTITY)
                .details(Collections.singletonList(exception.getMessage()))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorResponse> validationFailed(WebExchangeBindException exception) {

        List<String> errorMessages = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> {
                    if (error.getDefaultMessage().contains(ERROR_PROP_CONVERSION)) {
                        return format(INVALID_VALUE_FOR_FIELD, error.getField());
                    }

                    return format("%s %s", error.getField(), error.getDefaultMessage());
                })
                .toList();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(HttpStatus.BAD_REQUEST)
                .message(exception.getReason())
                .details(errorMessages)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(code = HttpStatus.FORBIDDEN)
    @ExceptionHandler(AssetAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> accessDeniedError(AssetAccessDeniedException exception) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(HttpStatus.FORBIDDEN)
                .message(exception.getMessage())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ResponseStatus(code = HttpStatus.FORBIDDEN)
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> accessDeniedError(AuthorizationDeniedException exception) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(HttpStatus.FORBIDDEN)
                .message(exception.getMessage())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ResponseStatus(code = HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ErrorResponse> authenticationError(AuthenticationFailedException exception) {

        LOG.error("Error during authentication", exception);
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> unknownError(Exception exception) {

        LOG.error("An unknown error has occurred", exception);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR)
                .message(UNKNOWN_ERROR)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ResponseStatus(code = HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(ModerationException.class)
    public ResponseEntity<ErrorResponse> moderationFailed(ModerationException exception) {

        List<String> details = exception.getFlaggedTopics().stream()
                .map(topic -> format(TOPIC_FLAGGED_IN_CONTENT, topic))
                .toList();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(HttpStatus.UNPROCESSABLE_ENTITY)
                .message(exception.getMessage())
                .details(details)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    private Mono<ServerResponse> handleAuthenticationError(Throwable originalException) {

        AuthenticationFailedException exception = (AuthenticationFailedException) originalException;
        ErrorResponse.Builder errorResponseBuilder = ErrorResponse.builder();
        errorResponseBuilder.code(HttpStatus.UNAUTHORIZED);

        if (StringUtils.isNotBlank(exception.getMessage())) {
            errorResponseBuilder.message(exception.getMessage());
        }

        if (StringUtils.isNotBlank(exception.getResponseMessage())) {
            errorResponseBuilder.details(Collections.singletonList(exception.getResponseMessage()));
        }

        return ServerResponse.status(401)
                .bodyValue(errorResponseBuilder.build());
    }

    private Mono<ServerResponse> handleDiscordApiError(Throwable originalException) {

        DiscordApiException exception = (DiscordApiException) originalException;
        ErrorResponse.Builder errorResponseBuilder = ErrorResponse.builder();
        errorResponseBuilder.code(exception.getHttpStatusCode());

        if (StringUtils.isNotBlank(exception.getMessage())) {
            errorResponseBuilder.message(exception.getMessage());
        }

        if (StringUtils.isNotBlank(exception.getErrorDescription())) {
            errorResponseBuilder.details(Collections.singletonList(exception.getErrorDescription()));
        }

        return ServerResponse.status(exception.getHttpStatusCode())
                .bodyValue(errorResponseBuilder.build());
    }

    private Mono<ServerResponse> handleOpenAiApiError(Throwable originalException) {

        OpenAiApiException exception = (OpenAiApiException) originalException;
        ErrorResponse.Builder errorResponseBuilder = ErrorResponse.builder();
        errorResponseBuilder.code(exception.getHttpStatusCode());

        if (StringUtils.isNotBlank(exception.getMessage())) {
            errorResponseBuilder.message(exception.getMessage());
        }

        if (StringUtils.isNotBlank(exception.getErrorDescription())) {
            errorResponseBuilder.details(Collections.singletonList(exception.getErrorDescription()));
        }

        return ServerResponse.status(exception.getHttpStatusCode())
                .bodyValue(errorResponseBuilder.build());
    }
}
