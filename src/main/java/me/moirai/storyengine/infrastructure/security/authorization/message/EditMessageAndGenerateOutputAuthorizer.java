package me.moirai.storyengine.infrastructure.security.authorization.message;

import org.springframework.stereotype.Component;

import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.security.authorization.OperationAuthorizer;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureAuthorizationReader;
import me.moirai.storyengine.core.port.outbound.message.MessageAuthorizationReader;

@Component
public class EditMessageAndGenerateOutputAuthorizer implements OperationAuthorizer {

    private final AdventureAuthorizationReader adventureAuthorizationReader;
    private final MessageAuthorizationReader messageAuthorizationReader;

    public EditMessageAndGenerateOutputAuthorizer(
            AdventureAuthorizationReader adventureAuthorizationReader,
            MessageAuthorizationReader messageAuthorizationReader) {

        this.adventureAuthorizationReader = adventureAuthorizationReader;
        this.messageAuthorizationReader = messageAuthorizationReader;
    }

    @Override
    public AuthorizationOperation getOperation() {
        return AuthorizationOperation.EDIT_MESSAGE_AND_GENERATE_OUTPUT;
    }

    @Override
    public boolean authorize(AuthorizationContext context) {

        var adventureId = context.getFieldAsUuid("adventureId");
        var messageId = context.getFieldAsUuid("messageId");
        var principal = context.getPrincipal();
        var authorizationData = adventureAuthorizationReader.getAuthorizationData(adventureId);

        if (authorizationData.isEmpty()) {
            return false;
        }

        if (canManage(authorizationData.get(), principal)) {
            return true;
        }

        return messageAuthorizationReader.getLastPlayerMessage(adventureId)
                .filter(lastPlayerMessage -> lastPlayerMessage.messageId().equals(messageId))
                .map(lastPlayerMessage -> principal.publicId().equals(lastPlayerMessage.authorId()))
                .orElse(false);
    }
}
