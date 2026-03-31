package me.moirai.storyengine.core.port.outbound.message;

import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.core.port.inbound.adventure.MessageSummary;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureMessages;

public interface MessageSearchReader {

    PaginatedResult<MessageSummary> search(SearchAdventureMessages query);
}
