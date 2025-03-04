package me.moirai.discordbot.infrastructure.inbound.rest.controller;

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
import org.springframework.boot.test.mock.mockito.MockBean;

import me.moirai.discordbot.AbstractRestWebTest;
import me.moirai.discordbot.core.application.usecase.discord.userdetails.request.DeleteUserByDiscordId;
import me.moirai.discordbot.core.application.usecase.discord.userdetails.request.GetUserDetailsByDiscordId;
import me.moirai.discordbot.core.application.usecase.discord.userdetails.result.UserDetailsResult;
import me.moirai.discordbot.core.application.usecase.notification.request.GetNotificationsByUserId;
import me.moirai.discordbot.core.application.usecase.notification.result.NotificationReadResult;
import me.moirai.discordbot.core.application.usecase.notification.result.NotificationResult;
import me.moirai.discordbot.infrastructure.inbound.rest.mapper.NotificationResponseMapper;
import me.moirai.discordbot.infrastructure.inbound.rest.mapper.UserDataResponseMapper;
import me.moirai.discordbot.infrastructure.inbound.rest.response.NotificationReadResponse;
import me.moirai.discordbot.infrastructure.inbound.rest.response.NotificationResponse;
import me.moirai.discordbot.infrastructure.inbound.rest.response.UserDataResponse;
import me.moirai.discordbot.infrastructure.inbound.rest.response.UserDataResponseFixture;
import me.moirai.discordbot.infrastructure.security.authentication.config.AuthenticationSecurityConfig;

@WebFluxTest(controllers = {
        UserDetailsController.class
}, excludeAutoConfiguration = {
        ReactiveSecurityAutoConfiguration.class,
        AuthenticationSecurityConfig.class
})
public class UserDetailsControllerTest extends AbstractRestWebTest {

    private static final String USER_ID_BASE_URL = "/user/%s";

    @MockBean
    private UserDataResponseMapper responseMapper;

    @MockBean
    private NotificationResponseMapper notificationResponseMapper;

    @Test
    public void http200WhenUserIsFound() {

        // Given
        String userId = "1234";
        UserDataResponse response = UserDataResponseFixture.create()
                .discordId(userId)
                .build();

        UserDetailsResult result = UserDetailsResult.builder()
                .avatarUrl(response.getAvatar())
                .discordId(response.getDiscordId())
                .nickname(response.getNickname())
                .username(response.getUsername())
                .joinDate(response.getJoinDate())
                .build();

        when(useCaseRunner.run(any(GetUserDetailsByDiscordId.class))).thenReturn(result);
        when(responseMapper.toResponse(any(UserDetailsResult.class))).thenReturn(response);

        // Then
        webTestClient.get()
                .uri(String.format(USER_ID_BASE_URL, userId))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(UserDataResponse.class)
                .value(r -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getDiscordId()).isEqualTo(r.getDiscordId());
                    assertThat(response.getNickname()).isEqualTo(r.getNickname());
                    assertThat(response.getUsername()).isEqualTo(r.getUsername());
                    assertThat(response.getAvatar()).isEqualTo(r.getAvatar());
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
        NotificationResponse response = NotificationResponse.builder()
                .isGlobal(false)
                .isInteractable(true)
                .message("some message")
                .receiverDiscordId("12345")
                .senderDiscordId("12345")
                .type("INFO")
                .metadata(Maps.newHashMap("someKey", "someValue"))
                .notificationsRead(list(NotificationReadResponse.builder()
                        .readAt(readAt)
                        .userId("12345")
                        .build()))
                .build();

        List<NotificationResult> result = list(NotificationResult.builder()
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
        when(notificationResponseMapper.toResponse(any(NotificationResult.class))).thenReturn(response);

        // Then
        webTestClient.get()
                .uri(String.format("/user/%s/notifications", userId))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(NotificationResponse.class)
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
