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
 * Indicates that a class is a query handler for read-only use cases.
 * <p>
 * This annotation should be used on classes that extend
 * {@code AbstractQueryHandler<A, T>},
 * where {@code A} is the query input DTO and {@code T} is the output DTO.
 * <p>
 * Query handlers use JDBC with database readers and do not interact with the
 * domain layer. They deal only with DTOs.
 * <p>
 * This annotation implies a read-only transaction with {@code REQUIRED}
 * propagation level, preventing writes and allowing database read optimizations.
 *
 * @see me.moirai.storyengine.common.cqs.query.AbstractQueryHandler
 */
@Service
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public @interface QueryHandler {

    @AliasFor(annotation = Service.class, attribute = "value")
    String value() default "";
}
