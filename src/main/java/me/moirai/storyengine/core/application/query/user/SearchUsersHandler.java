package me.moirai.storyengine.core.application.query.user;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.core.port.inbound.userdetails.SearchUsers;
import me.moirai.storyengine.core.port.inbound.userdetails.UserSummary;
import me.moirai.storyengine.core.port.outbound.userdetails.UserSearchReader;

@QueryHandler
public class SearchUsersHandler extends AbstractQueryHandler<SearchUsers, PaginatedResult<UserSummary>> {

    private final UserSearchReader reader;

    public SearchUsersHandler(UserSearchReader reader) {
        this.reader = reader;
    }

    @Override
    public PaginatedResult<UserSummary> execute(SearchUsers query) {

        return reader.search(query);
    }
}
