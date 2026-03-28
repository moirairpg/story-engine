package me.moirai.storyengine.infrastructure.security.authorization.user;

import org.springframework.stereotype.Component;

import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.security.authorization.OperationAuthorizer;
import me.moirai.storyengine.core.port.outbound.userdetails.UserReader;

@Component
public class UpdateUsernameAuthorizer implements OperationAuthorizer {

    private final UserReader reader;

    public UpdateUsernameAuthorizer(UserReader reader) {
        this.reader = reader;
    }

    @Override
    public AuthorizationOperation getOperation() {
        return AuthorizationOperation.UPDATE_USER_USERNAME;
    }

    @Override
    public boolean authorize(AuthorizationContext context) {
        var userId = context.getFieldAsUuid("userId");
        var principal = context.getPrincipal();

        var user = reader.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return user.publicId().equals(principal.publicId()) || principal.role().equals(Role.ADMIN);
    }
}
