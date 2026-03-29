package me.moirai.storyengine.core.application.command.world;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import java.util.stream.Collectors;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.dto.PermissionDto;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.port.inbound.world.CreateWorld;
import me.moirai.storyengine.core.port.inbound.world.WorldDetails;
import me.moirai.storyengine.core.port.inbound.world.WorldLorebookEntryDetails;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@CommandHandler
public class CreateWorldHandler extends AbstractCommandHandler<CreateWorld, WorldDetails> {

    private final WorldRepository repository;
    private final UserRepository userRepository;

    public CreateWorldHandler(
            WorldRepository repository,
            UserRepository userRepository) {

        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Override
    public WorldDetails execute(CreateWorld command) {

        var permissions = emptyIfNull(command.permissions()).stream()
                .map(dto -> {
                    var user = userRepository.findByPublicId(dto.userId())
                            .orElseThrow(() -> new NotFoundException("User not found"));

                    return new Permission(user.getId(), dto.level());
                })
                .collect(Collectors.toSet());

        var world = repository.save(World.builder()
                .name(command.name())
                .description(command.description())
                .adventureStart(command.adventureStart())
                .visibility(command.visibility())
                .permissions(permissions.toArray(Permission[]::new))
                .build());

        command.lorebookEntries().forEach(entry -> world.addLorebookEntry(
                entry.name(),
                entry.description()));

        return mapResult(repository.save(world));
    }

    private WorldDetails mapResult(World world) {

        return new WorldDetails(
                world.getPublicId(),
                world.getName(),
                world.getDescription(),
                world.getAdventureStart(),
                world.getVisibility().name(),
                world.getPermissions().stream()
                        .map(permission -> {
                            var user = userRepository.findById(permission.userId())
                                    .orElseThrow(() -> new NotFoundException("User not found"));

                            return new PermissionDto(user.getPublicId(), permission.level());
                        })
                        .collect(Collectors.toSet()),
                world.getLorebook().stream()
                        .map(entry -> new WorldLorebookEntryDetails(
                                entry.getPublicId(),
                                world.getPublicId(),
                                entry.getName(),
                                entry.getDescription(),
                                entry.getCreationDate(),
                                entry.getLastUpdateDate()))
                        .collect(Collectors.toSet()),
                world.getCreationDate(),
                world.getLastUpdateDate());
    }
}
