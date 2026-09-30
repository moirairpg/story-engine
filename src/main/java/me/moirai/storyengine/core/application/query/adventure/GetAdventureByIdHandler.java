package me.moirai.storyengine.core.application.query.adventure;

import static me.moirai.storyengine.common.enums.PermissionLevel.OWNER;
import static me.moirai.storyengine.common.enums.PermissionLevel.WRITE;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureMembershipSummary;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureById;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureReader;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRosterReader;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;

@QueryHandler
public class GetAdventureByIdHandler extends AbstractQueryHandler<GetAdventureById, AdventureDetails> {

    private static final String ADVENTURE_NOT_FOUND = "Adventure to be viewed was not found";

    private final AdventureReader reader;
    private final AdventureRosterReader adventureRosterReader;
    private final StoragePort storagePort;

    public GetAdventureByIdHandler(
            AdventureReader reader,
            AdventureRosterReader adventureRosterReader,
            StoragePort storagePort) {

        this.reader = reader;
        this.adventureRosterReader = adventureRosterReader;
        this.storagePort = storagePort;
    }

    @Override
    public AdventureDetails execute(GetAdventureById query) {

        var adventure = reader.getAdventureById(query.adventureId())
                .orElseThrow(() -> new NotFoundException(ADVENTURE_NOT_FOUND));

        var roster = adventureRosterReader.getAllByAdventurePublicId(query.adventureId()).stream()
                .map(row -> new AdventureMembershipSummary(
                        row.playerCharacterId(),
                        row.playerId(),
                        row.playerUsername(),
                        row.playerDisplayName(),
                        row.name(),
                        row.characterClass(),
                        storagePort.resolveUrl(row.imageKey()),
                        row.uiImagePositionX(),
                        row.uiImagePositionY()))
                .toList();

        var permissions = adventure.permissions();

        var isOwner = permissions.stream()
                .anyMatch(permission -> permission.level() == OWNER
                        && permission.userId().equals(query.requesterId()));

        var canManage = isOwner || permissions.stream()
                .anyMatch(permission -> permission.level() == WRITE
                        && permission.userId().equals(query.requesterId()));

        return new AdventureDetails(
                adventure.id(),
                adventure.name(),
                adventure.description(),
                adventure.adventureStart(),
                adventure.worldId(),
                adventure.narratorName(),
                adventure.narratorPersonality(),
                adventure.visibility(),
                adventure.moderation(),
                adventure.rpgMechanicsEnabled(),
                storagePort.resolveUrl(adventure.imageKey()),
                adventure.creationDate(),
                adventure.lastUpdateDate(),
                adventure.modelConfiguration(),
                adventure.contextAttributes(),
                canManage,
                isOwner,
                adventure.lorebook(),
                roster,
                adventure.uiImagePositionX(),
                adventure.uiImagePositionY());
    }
}
