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
 * Indicates that a class is a use case handler for processing specific use
 * cases.
 * <p>
 * This annotation should be used on classes that extend
 * {@code AbstractUseCaseHandler<A, B>},
 * where {@code A} is the input DTO for the use case and {@code B} is the output
 * DTO.
 * <p>
 * This annotation also implies that the class is transactional with
 * {@code REQUIRED}
 * propagation level. It will manage the transaction boundaries and ensure that
 * the use case
 * logic is executed within a transactional context.
 *
 * @see me.moirai.storyengine.common.usecases.AbstractUseCaseHandler
 */
@Deprecated
@Service
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Transactional(propagation = Propagation.REQUIRED)
public @interface UseCaseHandler {

    @AliasFor(annotation = Service.class, attribute = "value")
    String value() default "";
}
