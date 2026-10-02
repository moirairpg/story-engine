package me.moirai.storyengine.core.application.command.adventure;

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
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventurePermissions;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@CommandHandler
@Authorize(operation = AuthorizationOperation.MANAGE_ADVENTURE_PERMISSIONS, fields = "#request.adventureId")
public class UpdateAdventurePermissionsHandler
        extends AbstractCommandHandler<UpdateAdventurePermissions, List<AssetMember>> {

    private static final String ADVENTURE_NOT_FOUND = "Adventure to be shared was not found";
    private static final String USER_NOT_FOUND = "User to be granted access was not found: %s";

    private final AdventureRepository repository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public UpdateAdventurePermissionsHandler(
            AdventureRepository repository,
            UserRepository userRepository,
            ApplicationEventPublisher eventPublisher) {

        this.repository = repository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public List<AssetMember> execute(UpdateAdventurePermissions command) {

        var adventure = repository.findByPublicId(command.adventureId())
                .orElseThrow(() -> new NotFoundException(ADVENTURE_NOT_FOUND));

        var newPermissions = command.members().stream()
                .map(member -> {
                    var user = userRepository.findByUsername(member.username())
                            .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND.formatted(member.username())));

                    return new Permission(user.getId(), member.level());
                })
                .collect(Collectors.toSet());

        adventure.updateVisibility(command.visibility());
        adventure.updatePermissions(newPermissions);

        var savedAdventure = repository.save(adventure);

        adventure.drainEvents().forEach(eventPublisher::publishEvent);

        var savedPermissions = savedAdventure.getPermissions();

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
