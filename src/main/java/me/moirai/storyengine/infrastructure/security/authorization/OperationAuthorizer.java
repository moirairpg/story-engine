package me.moirai.storyengine.infrastructure.security.authorization;

public interface OperationAuthorizer {

    AuthorizationOperation getOperation();

    boolean authorize(AuthorizationContext context);
}
