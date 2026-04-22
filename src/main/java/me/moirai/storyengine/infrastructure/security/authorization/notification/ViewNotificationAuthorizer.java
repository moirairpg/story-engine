package me.moirai.storyengine.infrastructure.security.authorization.notification;

import static me.moirai.storyengine.common.enums.Role.ADMIN;
import static me.moirai.storyengine.core.domain.notification.NotificationType.BROADCAST;

import org.springframework.stereotype.Component;

import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.security.authorization.OperationAuthorizer;
import me.moirai.storyengine.core.port.inbound.notification.NotificationAuthorizationData;
import me.moirai.storyengine.core.port.outbound.notification.NotificationAuthorizationReader;

@Component
public class ViewNotificationAuthorizer implements OperationAuthorizer {

    private static final String NOTIFICATION_NOT_FOUND = "Notification not found";

    private final NotificationAuthorizationReader reader;

    public ViewNotificationAuthorizer(NotificationAuthorizationReader reader) {
        this.reader = reader;
    }

    @Override
    public AuthorizationOperation getOperation() {
        return AuthorizationOperation.VIEW_NOTIFICATION;
    }

    @Override
    public boolean authorize(AuthorizationContext context) {

        var notificationId = context.getFieldAsUuid("notificationId");
        var principal = context.getPrincipal();

        var authData = reader.getAuthorizationData(notificationId)
                .orElseThrow(() -> new NotFoundException(NOTIFICATION_NOT_FOUND));

        return canView(authData, principal);
    }

    private boolean canView(NotificationAuthorizationData authData, MoiraiPrincipal principal) {

        if (principal.role() == ADMIN) {
            return true;
        }

        if (authData.targetUserId() != null) {
            return authData.targetUserId().equals(principal.id());
        }

        return authData.type() == BROADCAST;
    }
}
