package me.moirai.discordbot.core.domain.notification;

import static me.moirai.discordbot.core.domain.notification.NotificationType.URGENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

import me.moirai.discordbot.common.exception.BusinessRuleViolationException;

public class NotificationTypeTest {

    @Test
    public void getNotificationType_whenInvalidType_thenThrowException() {

        // Given
        String type = "invalid";

        // Then
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> NotificationType.fromString(type));
    }

    @Test
    public void getNotificationType_whenTypeIsBlank_thenThrowException() {

        // Given
        String type = null;

        // Then
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> NotificationType.fromString(type));
    }

    @Test
    public void getNotificationType_whenValidTypeLowerCase_thenReturnType() {

        // Given
        String type = "urgent";

        // When
        NotificationType result = NotificationType.fromString(type);

        // Then
        assertThat(result).isEqualTo(URGENT);
    }

    @Test
    public void getNotificationType_whenValidTypeUpperCase_thenReturnType() {

        // Given
        String type = "URGENT";

        // When
        NotificationType result = NotificationType.fromString(type);

        // Then
        assertThat(result).isEqualTo(URGENT);
    }
}
