package me.moirai.storyengine.common.security.authorization;

public interface OperationAuthorizer {

    AuthorizationOperation getOperation();

    boolean authorize(AuthorizationContext context);
}
