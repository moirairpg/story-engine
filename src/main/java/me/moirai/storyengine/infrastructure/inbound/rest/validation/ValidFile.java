package me.moirai.storyengine.infrastructure.inbound.rest.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = ValidFileValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFile {

    String message() default "Invalid file";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String[] types() default {"application/json", "image/jpeg", "image/png"};

    long maxSizeMb() default 10;
}
