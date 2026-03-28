package me.moirai.storyengine.infrastructure.inbound.rest.errorhandler;

import static java.lang.String.format;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import io.micrometer.common.util.StringUtils;
import me.moirai.storyengine.common.exception.AccessDeniedException;
import me.moirai.storyengine.common.exception.AuthenticationFailedException;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
import me.moirai.storyengine.common.exception.ModerationException;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.exception.RestException;
import me.moirai.storyengine.common.exception.TechnicalException;
import me.moirai.storyengine.infrastructure.inbound.rest.response.ErrorResponse;

@RestControllerAdvice
public class WebApiExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(WebApiExceptionHandler.class);

    private static final String TOPIC_FLAGGED_IN_CONTENT = "Topic flagged in content: %s";
    private static final String UNKNOWN_ERROR = "An error has occurred. Please contact support.";
    private static final String ASSET_NOT_FOUND_ERROR = "The asset requested could not be found.";
    private static final String RESOURCE_NOT_FOUND_ERROR = "The endpoint requested could not be found.";

    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> assetNotFound(NotFoundException exception) {

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

    @ResponseStatus(code = HttpStatus.UNPROCESSABLE_CONTENT)
    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ErrorResponse> assetNotFound(BusinessRuleViolationException exception) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(HttpStatus.UNPROCESSABLE_CONTENT)
                .details(Collections.singletonList(exception.getMessage()))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validationFailed(MethodArgumentNotValidException exception) {

        List<String> errorMessages = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> format("%s %s", error.getField(), error.getDefaultMessage()))
                .toList();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(HttpStatus.BAD_REQUEST)
                .details(errorMessages)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(code = HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> accessDeniedError(AccessDeniedException exception) {

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

    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(TechnicalException.class)
    public ResponseEntity<ErrorResponse> technicalException(TechnicalException exception) {

        LOG.error("An unknown error has occurred", exception);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR)
                .message(UNKNOWN_ERROR)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> httpMessageNotReadableException(HttpMessageNotReadableException exception) {

        LOG.error("An unknown error has occurred", exception);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(HttpStatus.BAD_REQUEST)
                .message(exception.getMessage())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(code = HttpStatus.UNPROCESSABLE_CONTENT)
    @ExceptionHandler(ModerationException.class)
    public ResponseEntity<ErrorResponse> moderationFailed(ModerationException exception) {

        List<String> details = exception.getFlaggedTopics().stream()
                .map(topic -> format(TOPIC_FLAGGED_IN_CONTENT, topic))
                .toList();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(HttpStatus.UNPROCESSABLE_CONTENT)
                .message(exception.getMessage())
                .details(details)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RestException.class)
    public ResponseEntity<ErrorResponse> restError(RestException exception) {

        ErrorResponse.Builder errorResponseBuilder = ErrorResponse.builder();
        errorResponseBuilder.code(exception.getHttpStatusCode());

        if (StringUtils.isNotBlank(exception.getMessage())) {
            errorResponseBuilder.message(exception.getMessage());
        }

        if (StringUtils.isNotBlank(exception.getErrorDescription())) {
            errorResponseBuilder.details(Collections.singletonList(exception.getErrorDescription()));
        }

        return new ResponseEntity<>(errorResponseBuilder.build(), exception.getHttpStatusCode());
    }
}
