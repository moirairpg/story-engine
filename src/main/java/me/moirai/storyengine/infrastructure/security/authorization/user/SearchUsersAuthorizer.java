package me.moirai.storyengine.infrastructure.security.authorization.user;

import org.springframework.stereotype.Component;

import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.security.authorization.OperationAuthorizer;

@Component
public class SearchUsersAuthorizer implements OperationAuthorizer {

    @Override
    public AuthorizationOperation getOperation() {
        return AuthorizationOperation.SEARCH_USERS;
    }

    @Override
    public boolean authorize(AuthorizationContext context) {
        return context.getPrincipal().isAdmin();
    }
}
