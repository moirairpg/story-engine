package me.moirai.storyengine.infrastructure.security.authorization.user;

import org.springframework.stereotype.Component;

import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.security.authorization.OperationAuthorizer;

@Component
public class UpdateUserAuthorizer implements OperationAuthorizer {

    @Override
    public AuthorizationOperation getOperation() {
        return AuthorizationOperation.UPDATE_USER;
    }

    @Override
    public boolean authorize(AuthorizationContext context) {

        var userId = context.getFieldAsUuid("userId");
        var principal = context.getPrincipal();

        return principal.publicId().equals(userId) || principal.isAdmin();
    }
}
