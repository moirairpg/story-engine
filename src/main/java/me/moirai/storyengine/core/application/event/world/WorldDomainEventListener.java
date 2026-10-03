package me.moirai.storyengine.core.application.event.world;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import me.moirai.storyengine.core.domain.userdetails.UserDeletedEvent;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@Component
public class WorldDomainEventListener {

    private final WorldRepository worldRepository;
    private final StoragePort storagePort;

    public WorldDomainEventListener(WorldRepository worldRepository, StoragePort storagePort) {

        this.worldRepository = worldRepository;
        this.storagePort = storagePort;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    @EventListener
    public void onUserDeleted(UserDeletedEvent event) {

        var deletedIds = deleteOwnedWorlds(event.getUserId());

        worldRepository.findAllInvolving(event.getUserId()).stream()
                .filter(world -> !deletedIds.contains(world.getId()))
                .forEach(world -> revokeFrom(event.getUserId(), world));
    }

    private Set<Long> deleteOwnedWorlds(Long userId) {

        var owned = worldRepository.findAllOwnedBy(userId);

        owned.forEach(world -> {
            worldRepository.deleteByPublicId(world.getPublicId());
            removeImage(world);
        });

        return owned.stream()
                .map(World::getId)
                .collect(Collectors.toSet());
    }

    private void removeImage(World world) {

        if (world.getImageKey() != null) {
            storagePort.delete(world.getImageKey());
        }
    }

    private void revokeFrom(Long userId, World world) {

        world.revoke(userId);

        worldRepository.save(world);
    }
}
