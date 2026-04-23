package me.moirai.storyengine.infrastructure.security.authorization.notification;

import static me.moirai.storyengine.common.enums.Role.ADMIN;
import static me.moirai.storyengine.core.domain.notification.NotificationType.BROADCAST;

import org.springframework.stereotype.Component;

import me.moirai.storyengine.common.cqs.query.QueryRunner;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.security.authorization.OperationAuthorizer;
import me.moirai.storyengine.core.port.inbound.notification.GetNotificationBasicData;
import me.moirai.storyengine.core.port.inbound.notification.NotificationBasicData;

@Component
public class ViewNotificationAuthorizer implements OperationAuthorizer {

    private final QueryRunner queryRunner;

    public ViewNotificationAuthorizer(QueryRunner queryRunner) {
        this.queryRunner = queryRunner;
    }

    @Override
    public AuthorizationOperation getOperation() {
        return AuthorizationOperation.VIEW_NOTIFICATION;
    }

    @Override
    public boolean authorize(AuthorizationContext context) {

        var notificationId = context.getFieldAsUuid("notificationId");
        var principal = context.getPrincipal();

        var basicData = queryRunner.run(new GetNotificationBasicData(notificationId));

        return canView(basicData, principal);
    }

    private boolean canView(NotificationBasicData basicData, MoiraiPrincipal principal) {

        if (principal.role() == ADMIN) {
            return true;
        }

        if (!basicData.targetUsernames().isEmpty()) {
            return basicData.targetUsernames().contains(principal.username());
        }

        return basicData.type() == BROADCAST;
    }
}
