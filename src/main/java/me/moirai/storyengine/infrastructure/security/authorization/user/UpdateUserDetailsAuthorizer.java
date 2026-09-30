package me.moirai.storyengine.infrastructure.security.authorization.user;

import org.springframework.stereotype.Component;

import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.security.authorization.OperationAuthorizer;

@Component
public class UpdateUserDetailsAuthorizer implements OperationAuthorizer {

    @Override
    public AuthorizationOperation getOperation() {
        return AuthorizationOperation.UPDATE_USER_DETAILS;
    }

    @Override
    public boolean authorize(AuthorizationContext context) {

        var username = context.getFieldAsString("username");
        var principal = context.getPrincipal();

        return principal.username().equalsIgnoreCase(username) || principal.isAdmin();
    }
}
