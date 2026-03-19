package me.moirai.storyengine.common.security.authorization;

import java.util.Map;

import org.springframework.stereotype.Service;

import me.moirai.storyengine.common.exception.UnauthorizedException;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;

@Service
public class AuthorizationService {

    private final OperationAuthorizerFactory authorizerFactory;

    public AuthorizationService(OperationAuthorizerFactory authorizerFactory) {
        this.authorizerFactory = authorizerFactory;
    }

    public void authorize(AuthorizationOperation operation, Map<String, Object> fields, MoiraiPrincipal principal) {
        var context = new AuthorizationContext(principal, fields);
        if (!authorizerFactory.getAuthorizer(operation).authorize(context))
            throw new UnauthorizedException("Access denied for operation: " + operation);
    }
}
