package me.moirai.discordbot.infrastructure.inbound.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.list;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import me.moirai.discordbot.AbstractRestWebTest;
import me.moirai.discordbot.core.application.usecase.notification.request.GetNotificationById;
import me.moirai.discordbot.core.application.usecase.notification.request.ReadNotification;
import me.moirai.discordbot.core.application.usecase.notification.request.SearchNotifications;
import me.moirai.discordbot.core.application.usecase.notification.result.NotificationReadResult;
import me.moirai.discordbot.core.application.usecase.notification.result.NotificationResult;
import me.moirai.discordbot.core.application.usecase.notification.result.NotificationResultFixture;
import me.moirai.discordbot.core.application.usecase.notification.result.SearchNotificationsResult;
import me.moirai.discordbot.infrastructure.inbound.rest.mapper.NotificationResponseMapper;
import me.moirai.discordbot.infrastructure.inbound.rest.response.NotificationReadResponse;
import me.moirai.discordbot.infrastructure.inbound.rest.response.NotificationResponse;
import me.moirai.discordbot.infrastructure.inbound.rest.response.NotificationResponseFixture;
import me.moirai.discordbot.infrastructure.inbound.rest.response.SearchNotificationsResponse;
import me.moirai.discordbot.infrastructure.security.authentication.config.AuthenticationSecurityConfig;

@WebFluxTest(controllers = {
        NotificationController.class
}, excludeAutoConfiguration = {
        ReactiveSecurityAutoConfiguration.class,
        AuthenticationSecurityConfig.class
})
public class NotificationControllerTest extends AbstractRestWebTest {

    @MockBean
    private NotificationResponseMapper notificationResponseMapper;

    @Test
    public void http200WhenNotificationById() {

        // Given
        String userId = "1234";
        OffsetDateTime readAt = OffsetDateTime.now().minusMonths(1);
        NotificationResponse response = NotificationResponseFixture.targetedUnreadInfo()
                .notificationsRead(list(NotificationReadResponse.builder()
                        .readAt(readAt)
                        .userId("12345")
                        .build()))
                .build();

        NotificationResult result = NotificationResultFixture.targetedUnreadInfo()
                .notificationsRead(list(NotificationReadResult.builder()
                        .readAt(readAt)
                        .userId("12345")
                        .build()))
                .build();

        when(useCaseRunner.run(any(GetNotificationById.class))).thenReturn(result);
        when(notificationResponseMapper.toResponse(any(NotificationResult.class))).thenReturn(response);

        // Then
        webTestClient.get()
                .uri("/notification/" + userId)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(NotificationResponse.class)
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
        SearchNotificationsResponse response = SearchNotificationsResponse.builder()
                .page(1)
                .resultsInPage(10)
                .totalPages(1)
                .totalResults(10)
                .results(list(NotificationResponseFixture.targetedUnreadInfo()
                        .notificationsRead(list(NotificationReadResponse.builder()
                                .readAt(readAt)
                                .userId("12345")
                                .build()))
                        .build()))
                .build();

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
        when(notificationResponseMapper.toResponse(any(SearchNotificationsResult.class))).thenReturn(response);

        // Then
        webTestClient.get()
                .uri("/notification")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(SearchNotificationsResponse.class)
                .value(r -> {
                    assertThat(r).isNotNull();
                    assertThat(result.getItems()).isEqualTo(r.getResultsInPage());
                    assertThat(result.getPage()).isEqualTo(r.getPage());
                    assertThat(result.getTotalItems()).isEqualTo(r.getTotalResults());
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
        SearchNotificationsResponse response = SearchNotificationsResponse.builder()
                .page(1)
                .resultsInPage(10)
                .totalPages(1)
                .totalResults(10)
                .results(list(NotificationResponseFixture.targetedUnreadInfo()
                        .notificationsRead(list(NotificationReadResponse.builder()
                                .readAt(readAt)
                                .userId("12345")
                                .build()))
                        .build()))
                .build();

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
        when(notificationResponseMapper.toResponse(any(SearchNotificationsResult.class))).thenReturn(response);

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
                .expectBody(SearchNotificationsResponse.class)
                .value(r -> {
                    assertThat(r).isNotNull();
                    assertThat(result.getItems()).isEqualTo(r.getResultsInPage());
                    assertThat(result.getPage()).isEqualTo(r.getPage());
                    assertThat(result.getTotalItems()).isEqualTo(r.getTotalResults());
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

        NotificationReadResponse response = NotificationReadResponse.builder()
                .userId(userId)
                .readAt(readAt)
                .build();

        when(useCaseRunner.run(any(ReadNotification.class))).thenReturn(result);
        when(notificationResponseMapper.toResponse(any(NotificationReadResult.class))).thenReturn(response);

        // Then
        webTestClient.patch()
                .uri("/notification/" + userId)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(NotificationReadResponse.class)
                .value(r -> {
                    assertThat(r).isNotNull();
                    assertThat(result.getUserId()).isEqualTo(r.getUserId());
                    assertThat(result.getReadAt()).isEqualTo(r.getReadAt());
                });
    }
}
