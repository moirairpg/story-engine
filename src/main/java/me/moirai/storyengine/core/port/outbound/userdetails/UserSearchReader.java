package me.moirai.storyengine.core.port.outbound.userdetails;

import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.core.port.inbound.userdetails.SearchUsers;
import me.moirai.storyengine.core.port.inbound.userdetails.UserSummary;

public interface UserSearchReader {

    PaginatedResult<UserSummary> search(SearchUsers query);
}
