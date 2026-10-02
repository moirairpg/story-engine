package me.moirai.storyengine.core.application.command.world;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;

import me.moirai.storyengine.common.annotation.Authorize;
import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.port.inbound.AssetMember;
import me.moirai.storyengine.core.port.inbound.world.UpdateWorldPermissions;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@CommandHandler
@Authorize(operation = AuthorizationOperation.MANAGE_WORLD_PERMISSIONS, fields = "#request.worldId")
public class UpdateWorldPermissionsHandler
        extends AbstractCommandHandler<UpdateWorldPermissions, List<AssetMember>> {

    private static final String WORLD_NOT_FOUND = "World to be shared was not found";
    private static final String USER_NOT_FOUND = "User to be granted access was not found: %s";

    private final WorldRepository repository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public UpdateWorldPermissionsHandler(
            WorldRepository repository,
            UserRepository userRepository,
            ApplicationEventPublisher eventPublisher) {

        this.repository = repository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public List<AssetMember> execute(UpdateWorldPermissions command) {

        var world = repository.findByPublicId(command.worldId())
                .orElseThrow(() -> new NotFoundException(WORLD_NOT_FOUND));

        var newPermissions = command.members().stream()
                .map(member -> {
                    var user = userRepository.findByUsername(member.username())
                            .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND.formatted(member.username())));

                    return new Permission(user.getId(), member.level());
                })
                .collect(Collectors.toSet());

        world.updateVisibility(command.visibility());
        world.updatePermissions(newPermissions);

        var savedWorld = repository.save(world);

        world.drainEvents().forEach(eventPublisher::publishEvent);

        var savedPermissions = savedWorld.getPermissions();

        var userIds = savedPermissions.stream()
                .map(Permission::userId)
                .toList();

        var usersById = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        return savedPermissions.stream()
                .map(permission -> {
                    var user = usersById.get(permission.userId());

                    return new AssetMember(
                            user.getPublicId(),
                            user.getUsername(),
                            user.getDisplayName(),
                            permission.level());
                })
                .toList();
    }
}
