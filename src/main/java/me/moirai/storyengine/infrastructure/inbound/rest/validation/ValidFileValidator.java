package me.moirai.storyengine.infrastructure.inbound.rest.validation;

import java.util.Set;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class ValidFileValidator implements ConstraintValidator<ValidFile, MultipartFile> {

    private static final long HARD_MAX_MB = 10L;
    private static final long HARD_MIN_BYTES = toBytes(0.1);

    private final Tika tika;
    private String[] allowedTypes;
    private long maxSizeBytes;

    public ValidFileValidator(Tika tika) {
        this.tika = tika;
    }

    @Override
    public void initialize(ValidFile constraintAnnotation) {

        if (constraintAnnotation.maxSizeMb() > HARD_MAX_MB) {
            throw new IllegalArgumentException(
                    "maxSizeMb cannot exceed the hard cap of " + HARD_MAX_MB + " MB");
        }

        this.allowedTypes = constraintAnnotation.types();
        this.maxSizeBytes = toBytes(constraintAnnotation.maxSizeMb());
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {

        if (file == null) {
            return true;
        }

        try {
            var size = file.getSize();

            if (size < HARD_MIN_BYTES) {
                return violation(context, "File is too small");
            }

            if (size > maxSizeBytes) {
                return violation(context, "File exceeds the maximum allowed size");
            }

            var detectedType = tika.detect(file.getInputStream());

            if (!detectedType.equals(file.getContentType())) {
                return violation(context, "Declared file type does not match actual content");
            }

            if (MaliciousMimeType.contains(detectedType)) {
                return violation(context, "File type is not permitted");
            }

            if (!Set.of(allowedTypes).contains(detectedType)) {
                return violation(context, "File type '" + detectedType + "' is not accepted");
            }

            return true;
        } catch (Exception e) {
            return violation(context, "File content could not be read");
        }
    }

    private boolean violation(ConstraintValidatorContext context, String message) {

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        return false;
    }

    private static long toBytes(double mb) {
        return (long) (mb * 1024 * 1024);
    }
}
