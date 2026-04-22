package me.moirai.storyengine.infrastructure.security.authorization.notification;

import org.springframework.stereotype.Component;

import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.security.authorization.OperationAuthorizer;

@Component
public class ManageNotificationAuthorizer implements OperationAuthorizer {

    @Override
    public AuthorizationOperation getOperation() {
        return AuthorizationOperation.MANAGE_NOTIFICATION;
    }

    @Override
    public boolean authorize(AuthorizationContext context) {
        return context.getPrincipal().role().equals(Role.ADMIN);
    }
}
