package me.moirai.storyengine.core.port.inbound.adventure;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;
import reactor.core.publisher.Mono;

public record UpdateAdventureLorebookEntry(
        UUID entryId,
        UUID adventureId,
        String name,
        String regex,
        String description,
        String playerId,
        String requesterId)
        implements Command<Mono<AdventureLorebookEntryDetails>> {
}
