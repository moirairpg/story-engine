package me.moirai.storyengine.infrastructure.security.authorization.user;

import org.springframework.stereotype.Component;

import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.security.authorization.OperationAuthorizer;

@Component
public class UpdateUsersActiveStateAuthorizer implements OperationAuthorizer {

    @Override
    public AuthorizationOperation getOperation() {
        return AuthorizationOperation.UPDATE_USERS_ACTIVE_STATE;
    }

    @Override
    public boolean authorize(AuthorizationContext context) {
        return context.getPrincipal().isAdmin();
    }
}
