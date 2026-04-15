package me.moirai.storyengine.infrastructure.inbound.rest.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ModeratedLorebookValidator.class)
public @interface ModeratedLorebook {

    String message() default "lorebook content flagged by moderation";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
