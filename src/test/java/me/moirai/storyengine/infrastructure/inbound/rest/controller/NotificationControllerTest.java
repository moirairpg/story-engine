package me.moirai.storyengine.infrastructure.inbound.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.list;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;

import me.moirai.storyengine.AbstractRestWebTest;
import me.moirai.storyengine.core.port.inbound.notification.GetNotificationById;
import me.moirai.storyengine.core.port.inbound.notification.NotificationReadResult;
import me.moirai.storyengine.core.port.inbound.notification.NotificationDetails;
import me.moirai.storyengine.core.port.inbound.notification.ReadNotification;
import me.moirai.storyengine.core.port.inbound.notification.SearchNotifications;
import me.moirai.storyengine.core.port.inbound.notification.SearchNotificationsResult;
import me.moirai.storyengine.core.application.usecase.notification.result.NotificationResultFixture;
import me.moirai.storyengine.infrastructure.security.authentication.config.AuthenticationSecurityConfig;

@WebFluxTest(controllers = {
        NotificationController.class
}, excludeAutoConfiguration = {
        ReactiveSecurityAutoConfiguration.class,
        AuthenticationSecurityConfig.class
})
public class NotificationControllerTest extends AbstractRestWebTest {

    @Test
    public void http200WhenNotificationById() {

        // Given
        String userId = "1234";
        OffsetDateTime readAt = OffsetDateTime.now().minusMonths(1);

        NotificationDetails result = NotificationResultFixture.targetedUnreadInfo()
                .notificationsRead(list(NotificationReadResult.builder()
                        .readAt(readAt)
                        .userId("12345")
                        .build()))
                .build();

        when(useCaseRunner.run(any(GetNotificationById.class))).thenReturn(result);

        // Then
        webTestClient.get()
                .uri("/notification/" + userId)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(NotificationDetails.class)
                .value(r -> {
                    assertThat(r).isNotNull();
                    assertThat(result.getMessage()).isEqualTo(r.getMessage());
                    assertThat(result.getMetadata()).isEqualTo(r.getMetadata());
                    assertThat(result.getReceiverDiscordId()).isEqualTo(r.getReceiverDiscordId());
                    assertThat(result.getSenderDiscordId()).isEqualTo(r.getSenderDiscordId());

                    assertThat(result.getNotificationsRead().get(0).getUserId())
                            .isEqualTo(r.getNotificationsRead().get(0).getUserId());

                    assertThat(result.getNotificationsRead().get(0).getReadAt())
                            .isEqualTo(r.getNotificationsRead().get(0).getReadAt());
                });
    }

    @Test
    public void http200WhenSearchNotification() {

        // Given
        OffsetDateTime readAt = OffsetDateTime.now().minusMonths(1);

        SearchNotificationsResult result = SearchNotificationsResult.builder()
                .page(1)
                .items(10)
                .totalPages(1)
                .totalItems(10)
                .results(list(NotificationResultFixture.targetedUnreadInfo()
                        .notificationsRead(list(NotificationReadResult.builder()
                                .readAt(readAt)
                                .userId("12345")
                                .build()))
                        .build()))
                .build();

        when(useCaseRunner.run(any(SearchNotifications.class))).thenReturn(result);

        // Then
        webTestClient.get()
                .uri("/notification")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(SearchNotificationsResult.class)
                .value(r -> {
                    assertThat(r).isNotNull();
                    assertThat(result.getItems()).isEqualTo(r.getItems());
                    assertThat(result.getPage()).isEqualTo(r.getPage());
                    assertThat(result.getTotalItems()).isEqualTo(r.getTotalItems());
                    assertThat(result.getTotalPages()).isEqualTo(r.getTotalPages());

                    assertThat(result.getResults().get(0).getMessage())
                            .isEqualTo(r.getResults().get(0).getMessage());

                    assertThat(result.getResults().get(0).getMetadata())
                            .isEqualTo(r.getResults().get(0).getMetadata());

                    assertThat(result.getResults().get(0).getReceiverDiscordId())
                            .isEqualTo(r.getResults().get(0).getReceiverDiscordId());

                    assertThat(result.getResults().get(0).getSenderDiscordId())
                            .isEqualTo(r.getResults().get(0).getSenderDiscordId());

                    assertThat(result.getResults().get(0).getType())
                            .isEqualTo(r.getResults().get(0).getType());

                    assertThat(result.getResults().get(0).getNotificationsRead().get(0).getUserId())
                            .isEqualTo(r.getResults().get(0).getNotificationsRead().get(0).getUserId());

                    assertThat(result.getResults().get(0).getNotificationsRead().get(0).getReadAt())
                            .isEqualTo(r.getResults().get(0).getNotificationsRead().get(0).getReadAt());
                });
    }

    @Test
    public void http200WhenSearchNotificationWithParameters() {

        // Given
        OffsetDateTime readAt = OffsetDateTime.now().minusMonths(1);

        SearchNotificationsResult result = SearchNotificationsResult.builder()
                .page(1)
                .items(10)
                .totalPages(1)
                .totalItems(10)
                .results(list(NotificationResultFixture.targetedUnreadInfo()
                        .notificationsRead(list(NotificationReadResult.builder()
                                .readAt(readAt)
                                .userId("12345")
                                .build()))
                        .build()))
                .build();

        when(useCaseRunner.run(any(SearchNotifications.class))).thenReturn(result);

        // Then
        webTestClient.get()
                .uri(uri -> uri.path("/notification")
                        .queryParam("global", "true")
                        .queryParam("interactable", "false")
                        .queryParam("page", 1)
                        .queryParam("size", 10)
                        .queryParam("sortingField", "TYPE")
                        .queryParam("direction", "ASC")
                        .build())
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(SearchNotificationsResult.class)
                .value(r -> {
                    assertThat(r).isNotNull();
                    assertThat(result.getItems()).isEqualTo(r.getItems());
                    assertThat(result.getPage()).isEqualTo(r.getPage());
                    assertThat(result.getTotalItems()).isEqualTo(r.getTotalItems());
                    assertThat(result.getTotalPages()).isEqualTo(r.getTotalPages());

                    assertThat(result.getResults().get(0).getMessage())
                            .isEqualTo(r.getResults().get(0).getMessage());

                    assertThat(result.getResults().get(0).getMetadata())
                            .isEqualTo(r.getResults().get(0).getMetadata());

                    assertThat(result.getResults().get(0).getReceiverDiscordId())
                            .isEqualTo(r.getResults().get(0).getReceiverDiscordId());

                    assertThat(result.getResults().get(0).getSenderDiscordId())
                            .isEqualTo(r.getResults().get(0).getSenderDiscordId());

                    assertThat(result.getResults().get(0).getType())
                            .isEqualTo(r.getResults().get(0).getType());

                    assertThat(result.getResults().get(0).getNotificationsRead().get(0).getUserId())
                            .isEqualTo(r.getResults().get(0).getNotificationsRead().get(0).getUserId());

                    assertThat(result.getResults().get(0).getNotificationsRead().get(0).getReadAt())
                            .isEqualTo(r.getResults().get(0).getNotificationsRead().get(0).getReadAt());
                });
    }

    @Test
    public void http200WhenReadNotification() {

        // Given
        String userId = "12345";
        OffsetDateTime readAt = OffsetDateTime.now();
        NotificationReadResult result = NotificationReadResult.builder()
                .userId(userId)
                .readAt(readAt)
                .build();

        when(useCaseRunner.run(any(ReadNotification.class))).thenReturn(result);

        // Then
        webTestClient.patch()
                .uri("/notification/" + userId)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(NotificationReadResult.class)
                .value(r -> {
                    assertThat(r).isNotNull();
                    assertThat(result.getUserId()).isEqualTo(r.getUserId());
                    assertThat(result.getReadAt()).isEqualTo(r.getReadAt());
                });
    }
}
