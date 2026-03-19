package me.moirai.storyengine.infrastructure.security.authorization.user;

import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.security.authorization.AuthorizationContext;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.security.authorization.OperationAuthorizer;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

public class ManageUserAuthorizer implements OperationAuthorizer {

    private final UserRepository userRepository;

    public ManageUserAuthorizer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public AuthorizationOperation getOperation() {
        return AuthorizationOperation.MANAGE_USER;
    }

    @Override
    public boolean authorize(AuthorizationContext context) {

        var userId = context.getFieldAsString("discordUserId");
        var principal = context.getPrincipal();

        var user = userRepository.findByDiscordId(userId)
                .orElseThrow(() -> new AssetNotFoundException("User not found"));

        return user.getRole().equals(Role.ADMIN) || user.getDiscordId().equals(principal.discordId());
    }
}
