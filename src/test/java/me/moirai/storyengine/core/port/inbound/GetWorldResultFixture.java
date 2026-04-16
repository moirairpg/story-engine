package me.moirai.storyengine.core.port.inbound;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import me.moirai.storyengine.common.dto.PermissionDto;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.world.WorldDetails;

public class GetWorldResultFixture {

    public static WorldDetails publicWorld() {

        var world = WorldFixture.publicWorldWithIdAndPermissions();

        var permissions = world.getPermissions().stream()
                .map(permission -> new PermissionDto(
                        UUID.fromString("d6622c6c-85bb-41ba-aa53-93fa68681f85"),
                        PermissionLevel.OWNER))
                .collect(Collectors.toSet());

        return new WorldDetails(
                world.getPublicId(),
                world.getName(),
                world.getDescription(),
                world.getAdventureStart(),
                world.getNarratorName(),
                world.getNarratorPersonality(),
                world.getVisibility().name(),
                null,
                permissions,
                Set.of(),
                world.getCreationDate(),
                world.getLastUpdateDate());
    }

    public static WorldDetails privateWorld() {

        var world = WorldFixture.privateWorldWithIdAndPermissions();

        var permissions = world.getPermissions().stream()
                .map(permission -> new PermissionDto(
                        UUID.fromString("d6622c6c-85bb-41ba-aa53-93fa68681f85"),
                        PermissionLevel.OWNER))
                .collect(Collectors.toSet());

        return new WorldDetails(
                world.getPublicId(),
                world.getName(),
                world.getDescription(),
                world.getAdventureStart(),
                world.getNarratorName(),
                world.getNarratorPersonality(),
                world.getVisibility().name(),
                null,
                permissions,
                Set.of(),
                world.getCreationDate(),
                world.getLastUpdateDate());
    }
}
