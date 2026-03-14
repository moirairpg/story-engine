package me.moirai.storyengine.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.hibernate.annotations.IdGeneratorType;

import me.moirai.storyengine.common.dbutil.UuidIdentifierGenerator;

@IdGeneratorType(UuidIdentifierGenerator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
public @interface UuidIdentifier {
}
