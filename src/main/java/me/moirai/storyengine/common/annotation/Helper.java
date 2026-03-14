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
 * Indicates that a class is a helper service providing supporting
 * functionalities.
 * <p>
 * This annotation should be used on classes that offer auxiliary services which
 * support other components or services but are not directly involved in
 * specific
 * use cases. It is transactional with {@code REQUIRED} propagation level and
 * configured as read-only to ensure that no modifications are made during the
 * transaction.
 *
 * @see Transactional
 */
@Service
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public @interface Helper {

    @AliasFor(annotation = Service.class, attribute = "value")
    String value() default "";
}
