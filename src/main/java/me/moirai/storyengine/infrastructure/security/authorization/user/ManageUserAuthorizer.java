package me.moirai.storyengine.infrastructure.security.authorization.user;

import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.security.authorization.OperationAuthorizer;
import org.springframework.stereotype.Component;

import me.moirai.storyengine.core.port.outbound.userdetails.UserReader;

@Component
public class ManageUserAuthorizer implements OperationAuthorizer {

    private final UserReader reader;

    public ManageUserAuthorizer(UserReader reader) {
        this.reader = reader;
    }

    @Override
    public AuthorizationOperation getOperation() {
        return AuthorizationOperation.MANAGE_USER;
    }

    @Override
    public boolean authorize(AuthorizationContext context) {

        var userId = context.getFieldAsUuid("userId");
        var principal = context.getPrincipal();

        var user = reader.getUserById(userId)
                .orElseThrow(() -> new AssetNotFoundException("User not found"));

        return user.role().equals(Role.ADMIN) || user.publicId().equals(principal.publicId());
    }
}
