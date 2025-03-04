package me.moirai.discordbot.core.application.usecase.notification;

import me.moirai.discordbot.common.annotation.UseCaseHandler;
import me.moirai.discordbot.common.usecases.AbstractUseCaseHandler;
import me.moirai.discordbot.core.application.usecase.notification.request.SearchNotifications;
import me.moirai.discordbot.core.application.usecase.notification.result.SearchNotificationsResult;
import me.moirai.discordbot.core.domain.notification.NotificationRepository;

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
