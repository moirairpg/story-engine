package me.moirai.storyengine.infrastructure.security.authorization.user;

import org.springframework.stereotype.Component;

import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.security.authorization.OperationAuthorizer;

@Component
public class UpdateUserRoleAuthorizer implements OperationAuthorizer {

    @Override
    public AuthorizationOperation getOperation() {
        return AuthorizationOperation.UPDATE_USER_ROLE;
    }

    @Override
    public boolean authorize(AuthorizationContext context) {
        return context.getPrincipal().role().equals(Role.ADMIN);
    }
}
