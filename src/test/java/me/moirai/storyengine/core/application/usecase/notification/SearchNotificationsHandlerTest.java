package me.moirai.storyengine.core.application.usecase.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.list;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.port.inbound.notification.SearchNotifications;
import me.moirai.storyengine.core.port.inbound.notification.NotificationResult;
import me.moirai.storyengine.core.port.inbound.notification.SearchNotificationsResult;
import me.moirai.storyengine.core.port.outbound.notification.NotificationRepository;

@ExtendWith(MockitoExtension.class)
public class SearchNotificationsHandlerTest {

    @Mock
    private NotificationRepository repository;

    @InjectMocks
    private SearchNotificationsHandler handler;

    @Test
    public void searchNotifications() {

        // Given
        SearchNotifications request = SearchNotifications.builder()
                .page(1)
                .size(10)
                .direction("ASC")
                .isGlobal(false)
                .isInteractable(true)
                .build();

        SearchNotificationsResult expectedResult = SearchNotificationsResult.builder()
                .page(1)
                .totalItems(10)
                .items(10)
                .totalPages(1)
                .results(list(NotificationResult.builder().build()))
                .build();

        when(repository.search(any())).thenReturn(expectedResult);

        // When
        SearchNotificationsResult result = handler.handle(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getItems()).isEqualTo(expectedResult.getItems());
        assertThat(result.getPage()).isEqualTo(expectedResult.getPage());
        assertThat(result.getResults()).hasSameElementsAs(expectedResult.getResults());
        assertThat(result.getTotalItems()).isEqualTo(expectedResult.getTotalItems());
        assertThat(result.getTotalPages()).isEqualTo(expectedResult.getTotalPages());
    }

    @Test
    public void searchNotificationsWhenResultIsNullThenListShouldBeEmpty() {

        // Given
        SearchNotifications request = SearchNotifications.builder()
                .page(1)
                .size(10)
                .direction("ASC")
                .isGlobal(false)
                .isInteractable(true)
                .build();

        SearchNotificationsResult expectedResult = SearchNotificationsResult.builder()
                .page(1)
                .totalItems(10)
                .items(10)
                .totalPages(1)
                .results(null)
                .build();

        when(repository.search(any())).thenReturn(expectedResult);

        // When
        SearchNotificationsResult result = handler.handle(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getResults()).isNotNull().isEmpty();
    }
}
