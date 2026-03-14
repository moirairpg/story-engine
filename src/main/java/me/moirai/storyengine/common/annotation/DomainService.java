package me.moirai.storyengine.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Service;

/**
 * Indicates that a class is a domain service.
 * <p>
 * This annotation is used to mark classes that provide domain-specific services
 * and
 * contain business logic related to domain entities. It is a specialization of
 * the
 * {@link Service} annotation and is used to identify domain services within the
 * application context.
 *
 * @see Service
 */
@Service
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DomainService {

    @AliasFor(annotation = Service.class, attribute = "value")
    String value() default "";
}