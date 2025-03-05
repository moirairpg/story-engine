package me.moirai.discordbot.infrastructure.inbound.socket.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.test.context.ActiveProfiles;

import me.moirai.discordbot.MoiraiApplication;
import me.moirai.discordbot.common.usecases.UseCaseRunner;
import me.moirai.discordbot.core.application.helper.PersonaEnrichmentHelper;
import me.moirai.discordbot.core.application.port.DiscordChannelPort;
import me.moirai.discordbot.core.application.port.StorySummarizationPort;
import me.moirai.discordbot.core.application.port.TextCompletionPort;
import me.moirai.discordbot.core.application.port.TextModerationPort;
import me.moirai.discordbot.core.application.usecase.notification.request.SendNotification;
import me.moirai.discordbot.core.application.usecase.notification.request.StreamNotificationsForUser;
import me.moirai.discordbot.core.application.usecase.notification.result.NotificationResult;
import me.moirai.discordbot.core.application.usecase.notification.result.NotificationResultFixture;
import me.moirai.discordbot.core.application.usecase.notification.result.SendNotificationResult;
import me.moirai.discordbot.infrastructure.config.JdaConfig;
import me.moirai.discordbot.infrastructure.inbound.socket.request.SendNotificationRequest;
import me.moirai.discordbot.infrastructure.inbound.socket.response.NotificationResponse;
import me.moirai.discordbot.infrastructure.inbound.socket.response.SendNotificationResponse;
import net.dv8tion.jda.api.JDA;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@Disabled("Disabled until mocking RSocket is possible. This test causes other integration tests to fail, and passes locally but fails the pipeline.")
@ActiveProfiles({ "test", "prompts" })
@SpringBootTest(classes = MoiraiApplication.class)
public class NotificationSocketControllerTest {

    @MockBean
    private TextCompletionPort openAiPort;

    @MockBean
    private DiscordChannelPort discordChannelOperationsPort;

    @MockBean
    private PersonaEnrichmentHelper inputEnrichmentService;

    @MockBean
    private StorySummarizationPort contextSummarizationService;

    @MockBean
    private JDA jda;

    @MockBean
    private TextModerationPort textModerationPort;

    @MockBean
    private JdaConfig jdaConfig;

    @MockBean
    private UseCaseRunner useCaseRunner;

    @Autowired
    private RSocketStrategies rSocketStrategies;

    private RSocketRequester requester;

    @BeforeEach
    public void before() throws Exception {

        requester = RSocketRequester.builder()
                .rsocketStrategies(rSocketStrategies)
                .websocket(URI.create("ws://localhost:7000/rs"));
    }

    @AfterEach
    public void after() {

        requester.dispose();
    }

    @Test
    public void whenRequestingNotifications_thenReceiveNotifications() {

        // Given
        String userId = "12345";
        NotificationResult result = NotificationResultFixture.globalUnreadWarning().build();

        when(useCaseRunner.run(any(StreamNotificationsForUser.class))).thenReturn(Flux.just(result));

        // Then
        StepVerifier.create(requester.route("notifications.stream")
                .data(userId)
                .retrieveFlux(NotificationResponse.class))
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getMessage()).isEqualTo(result.getMessage());
                    assertThat(response.getReceiverDiscordId()).isEqualTo(result.getReceiverDiscordId());
                    assertThat(response.getSenderDiscordId()).isEqualTo(result.getSenderDiscordId());
                    assertThat(response.getType()).isEqualTo(result.getType());
                })
                .verifyComplete();
    }

    @Test
    public void whenSendingNotifications_thenRetrieveNotificationData() {

        // Given
        String notificationId = "12345";
        OffsetDateTime sendDate = OffsetDateTime.now().minusHours(1);

        SendNotificationResult result = SendNotificationResult
                .withIdAndCreationDateTime(notificationId, sendDate);

        SendNotificationRequest request = new SendNotificationRequest();
        request.setMessage("some message");

        when(useCaseRunner.run(any(SendNotification.class))).thenReturn(result);

        // Then
        StepVerifier.create(requester.route("notifications.send")
                .data(request)
                .retrieveMono(SendNotificationResponse.class))
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getId()).isEqualTo(result.getId());
                    assertThat(response.getCreationDateTime()).isEqualTo(result.getCreationDateTime());
                })
                .verifyComplete();
    }
}
