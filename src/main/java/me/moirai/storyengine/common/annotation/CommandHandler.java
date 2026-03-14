package me.moirai.storyengine.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Indicates that a class is a command handler for state-mutating use cases.
 * <p>
 * This annotation should be used on classes that extend
 * {@code AbstractCommandHandler<A, T>},
 * where {@code A} is the command input DTO and {@code T} is the output DTO.
 * <p>
 * Command handlers use JPA with repositories and interact with the domain layer.
 * <p>
 * This annotation implies a transactional context with {@code REQUIRED}
 * propagation level, managing transaction boundaries for write operations.
 *
 * @see me.moirai.storyengine.common.cqs.command.AbstractCommandHandler
 */
@Service
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Transactional(propagation = Propagation.REQUIRED)
public @interface CommandHandler {

    @AliasFor(annotation = Service.class, attribute = "value")
    String value() default "";
}
