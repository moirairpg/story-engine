package me.moirai.storyengine.infrastructure.inbound.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.list;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;

import org.assertj.core.util.Maps;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;

import me.moirai.storyengine.AbstractRestWebTest;
import me.moirai.storyengine.core.port.inbound.discord.userdetails.DeleteUserByDiscordId;
import me.moirai.storyengine.core.port.inbound.discord.userdetails.GetUserDetailsByDiscordId;
import me.moirai.storyengine.core.port.inbound.discord.userdetails.UserDetailsResult;
import me.moirai.storyengine.core.port.inbound.notification.GetNotificationsByUserId;
import me.moirai.storyengine.core.port.inbound.notification.NotificationReadResult;
import me.moirai.storyengine.core.port.inbound.notification.NotificationDetails;
import me.moirai.storyengine.infrastructure.security.authentication.config.AuthenticationSecurityConfig;

@WebFluxTest(controllers = {
        UserDetailsController.class
}, excludeAutoConfiguration = {
        ReactiveSecurityAutoConfiguration.class,
        AuthenticationSecurityConfig.class
})
public class UserDetailsControllerTest extends AbstractRestWebTest {

    private static final String USER_ID_BASE_URL = "/user/%s";

    @Test
    public void http200WhenUserIsFound() {

        // Given
        String userId = "1234";
        UserDetailsResult result = UserDetailsResult.builder()
                .discordId(userId)
                .avatarUrl("someAvatarUrl")
                .nickname("someNickname")
                .username("someUsername")
                .joinDate(OffsetDateTime.now())
                .build();

        when(useCaseRunner.run(any(GetUserDetailsByDiscordId.class))).thenReturn(result);

        // Then
        webTestClient.get()
                .uri(String.format(USER_ID_BASE_URL, userId))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(UserDetailsResult.class)
                .value(r -> {
                    assertThat(r).isNotNull();
                    assertThat(result.getDiscordId()).isEqualTo(r.getDiscordId());
                    assertThat(result.getNickname()).isEqualTo(r.getNickname());
                    assertThat(result.getUsername()).isEqualTo(r.getUsername());
                    assertThat(result.getAvatarUrl()).isEqualTo(r.getAvatarUrl());
                });
    }

    @Test
    public void http200WhenUserIsDeleted() {

        // Given
        String userId = "1234";

        when(useCaseRunner.run(any(DeleteUserByDiscordId.class))).thenReturn(null);

        // Then
        webTestClient.delete()
                .uri(String.format(USER_ID_BASE_URL, userId))
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    public void http200WhenUserNotifications() {

        // Given
        String userId = "1234";
        OffsetDateTime readAt = OffsetDateTime.now().minusMonths(1);

        List<NotificationDetails> result = list(NotificationDetails.builder()
                .isGlobal(false)
                .isInteractable(true)
                .message("some message")
                .receiverDiscordId("12345")
                .senderDiscordId("12345")
                .type("INFO")
                .metadata(Maps.newHashMap("someKey", "someValue"))
                .notificationsRead(list(NotificationReadResult.builder()
                        .readAt(readAt)
                        .userId("12345")
                        .build()))
                .build());

        when(useCaseRunner.run(any(GetNotificationsByUserId.class))).thenReturn(result);

        // Then
        webTestClient.get()
                .uri(String.format("/user/%s/notifications", userId))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(NotificationDetails.class)
                .value(r -> {
                    assertThat(r).isNotNull();
                    assertThat(result.get(0).getMessage()).isEqualTo(r.get(0).getMessage());
                    assertThat(result.get(0).getMetadata()).isEqualTo(r.get(0).getMetadata());
                    assertThat(result.get(0).getReceiverDiscordId()).isEqualTo(r.get(0).getReceiverDiscordId());
                    assertThat(result.get(0).getSenderDiscordId()).isEqualTo(r.get(0).getSenderDiscordId());

                    assertThat(result.get(0).getNotificationsRead().get(0).getUserId())
                            .isEqualTo(r.get(0).getNotificationsRead().get(0).getUserId());

                    assertThat(result.get(0).getNotificationsRead().get(0).getReadAt())
                            .isEqualTo(r.get(0).getNotificationsRead().get(0).getReadAt());
                });
    }
}
