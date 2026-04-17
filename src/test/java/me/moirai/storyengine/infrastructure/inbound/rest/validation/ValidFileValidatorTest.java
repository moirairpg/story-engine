package me.moirai.storyengine.infrastructure.inbound.rest.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.tika.Tika;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;

@ExtendWith(MockitoExtension.class)
public class ValidFileValidatorTest {

    private static final byte[] JPEG_BYTES = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 0x4A, 0x46, 0x49, 0x46};
    private static final byte[] PNG_BYTES = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final int BYTES_200KB = 200 * 1024;
    private static final int BYTES_2MB = 2 * 1024 * 1024;
    private static final int BYTES_11MB = 11 * 1024 * 1024;

    @Mock
    private Tika tika;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintViolationBuilder violationBuilder;

    private ValidFileValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ValidFileValidator(tika);
    }

    private ValidFile annotationWithDefaults() {
        var annotation = mock(ValidFile.class);
        when(annotation.maxSizeMb()).thenReturn(10L);
        when(annotation.types()).thenReturn(new String[]{"image/jpeg", "image/png"});
        return annotation;
    }

    private ValidFile annotationWithMaxSize(long maxSizeMb) {
        var annotation = mock(ValidFile.class);
        when(annotation.maxSizeMb()).thenReturn(maxSizeMb);
        when(annotation.types()).thenReturn(new String[]{"image/jpeg", "image/png"});
        return annotation;
    }

    private void stubViolation() {
        when(context.buildConstraintViolationWithTemplate(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(violationBuilder);
        when(violationBuilder.addConstraintViolation()).thenReturn(context);
    }

    @Test
    public void shouldPassWhenFileIsValidJpeg() throws Exception {

        // given
        var annotation = annotationWithDefaults();
        validator.initialize(annotation);

        var file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn((long) BYTES_200KB);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(JPEG_BYTES));
        when(file.getContentType()).thenReturn("image/jpeg");
        when(tika.detect(org.mockito.ArgumentMatchers.any(InputStream.class))).thenReturn("image/jpeg");

        // when
        var result = validator.isValid(file, context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void shouldPassWhenFileIsValidPng() throws Exception {

        // given
        var annotation = annotationWithDefaults();
        validator.initialize(annotation);

        var file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn((long) BYTES_200KB);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(PNG_BYTES));
        when(file.getContentType()).thenReturn("image/png");
        when(tika.detect(org.mockito.ArgumentMatchers.any(InputStream.class))).thenReturn("image/png");

        // when
        var result = validator.isValid(file, context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void shouldRejectWhenFileBelowMinSize() {

        // given
        var annotation = annotationWithDefaults();
        validator.initialize(annotation);
        stubViolation();

        var file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(100L);

        // when
        var result = validator.isValid(file, context);

        // then
        assertThat(result).isFalse();
    }

    @Test
    public void shouldRejectWhenFileExceedsAnnotationMaxSize() {

        // given
        var annotation = annotationWithMaxSize(1L);
        validator.initialize(annotation);
        stubViolation();

        var file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn((long) BYTES_2MB);

        // when
        var result = validator.isValid(file, context);

        // then
        assertThat(result).isFalse();
    }

    @Test
    public void shouldRejectWhenFileExceedsHardCap() {

        // given
        var annotation = annotationWithDefaults();
        validator.initialize(annotation);
        stubViolation();

        var file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn((long) BYTES_11MB);

        // when
        var result = validator.isValid(file, context);

        // then
        assertThat(result).isFalse();
    }

    @Test
    public void shouldRejectWhenDeclaredTypeMismatchesDetectedType() throws Exception {

        // given
        var annotation = annotationWithDefaults();
        validator.initialize(annotation);
        stubViolation();

        var file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn((long) BYTES_200KB);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(PNG_BYTES));
        when(file.getContentType()).thenReturn("image/jpeg");
        when(tika.detect(org.mockito.ArgumentMatchers.any(InputStream.class))).thenReturn("image/png");

        // when
        var result = validator.isValid(file, context);

        // then
        assertThat(result).isFalse();
    }

    @Test
    public void shouldRejectWhenDetectedTypeIsMalicious() throws Exception {

        // given
        var annotation = annotationWithDefaults();
        validator.initialize(annotation);
        stubViolation();

        var file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn((long) BYTES_200KB);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("<html>test</html>".getBytes()));
        when(file.getContentType()).thenReturn("text/html");
        when(tika.detect(org.mockito.ArgumentMatchers.any(InputStream.class))).thenReturn("text/html");

        // when
        var result = validator.isValid(file, context);

        // then
        assertThat(result).isFalse();
    }

    @Test
    public void shouldRejectWhenDetectedTypeNotInAllowedList() throws Exception {

        // given
        var annotation = annotationWithDefaults();
        validator.initialize(annotation);
        stubViolation();

        var file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn((long) BYTES_200KB);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[BYTES_200KB]));
        when(file.getContentType()).thenReturn("application/pdf");
        when(tika.detect(org.mockito.ArgumentMatchers.any(InputStream.class))).thenReturn("application/pdf");

        // when
        var result = validator.isValid(file, context);

        // then
        assertThat(result).isFalse();
    }

    @Test
    public void shouldThrowOnInitializeWhenAnnotationMaxExceedsHardCap() {

        // given
        var annotation = mock(ValidFile.class);
        when(annotation.maxSizeMb()).thenReturn(11L);

        // then
        assertThatThrownBy(() -> validator.initialize(annotation))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
