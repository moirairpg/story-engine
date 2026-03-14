package me.moirai.storyengine.core.application.usecase.notification;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.port.inbound.notification.SearchNotifications;
import me.moirai.storyengine.core.port.inbound.notification.SearchNotificationsResult;
import me.moirai.storyengine.core.domain.notification.NotificationRepository;

@UseCaseHandler
public class SearchNotificationsHandler extends AbstractUseCaseHandler<SearchNotifications, SearchNotificationsResult> {

    private final NotificationRepository repository;

    public SearchNotificationsHandler(NotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public SearchNotificationsResult execute(SearchNotifications request) {

        return repository.search(request);
    }
}
