package me.moirai.storyengine.common.security.authorization;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class OperationAuthorizerFactory {

    private final Map<AuthorizationOperation, OperationAuthorizer> authorizers;

    public OperationAuthorizerFactory(List<OperationAuthorizer> authorizers) {
        this.authorizers = authorizers.stream()
                .collect(Collectors.toMap(OperationAuthorizer::getOperation, Function.identity()));
    }

    public OperationAuthorizer getAuthorizer(AuthorizationOperation operation) {
        OperationAuthorizer authorizer = authorizers.get(operation);
        if (authorizer == null) {
            throw new IllegalStateException("No authorizer registered for operation: " + operation);
        }
        return authorizer;
    }
}
