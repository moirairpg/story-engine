package me.moirai.storyengine.infrastructure.inbound.rest.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ModeratedContentValidator.class)
public @interface Moderated {

    String message() default "content failed moderation";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
