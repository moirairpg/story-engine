package me.moirai.storyengine.core.application.query.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.core.port.inbound.userdetails.SearchUsers;
import me.moirai.storyengine.core.port.inbound.userdetails.UserSummary;
import me.moirai.storyengine.core.port.outbound.userdetails.UserSearchReader;

@ExtendWith(MockitoExtension.class)
public class SearchUsersHandlerTest {

    @Mock
    private UserSearchReader reader;

    @InjectMocks
    private SearchUsersHandler handler;

    @Test
    public void shouldReturnTheReaderResultWhenTheSearchRuns() {

        // given
        var query = new SearchUsers(null, null, null, null, null, null, null, 1, 10);
        var summary = new UserSummary(UUID.randomUUID(), "john.doe", Role.PLAYER, true, Instant.now());
        var expected = PaginatedResult.of(List.of(summary), 1L, 1, 10);

        when(reader.search(query)).thenReturn(expected);

        // when
        var result = handler.handle(query);

        // then
        assertThat(result).isSameAs(expected);
        assertThat(result.data()).singleElement().isEqualTo(summary);
    }

    @Test
    public void shouldPassEveryFilterToTheReaderWhenTheSearchIsNarrowed() {

        // given
        var registeredFrom = Instant.parse("2026-01-01T00:00:00Z");
        var registeredTo = Instant.parse("2026-09-19T23:59:59Z");
        var query = new SearchUsers("john", Role.ADMIN, false, registeredFrom, registeredTo, null, null, 2, 5);

        when(reader.search(query)).thenReturn(PaginatedResult.of(List.of(), 0L, 2, 5));

        // when
        handler.handle(query);

        // then
        verify(reader).search(query);
    }

    @Test
    public void shouldReturnAnEmptyPageWhenNoUsersMatch() {

        // given
        var query = new SearchUsers("nobody", null, null, null, null, null, null, 1, 10);

        when(reader.search(query)).thenReturn(PaginatedResult.of(List.of(), 0L, 1, 10));

        // when
        var result = handler.handle(query);

        // then
        assertThat(result.data()).isEmpty();
        assertThat(result.totalItems()).isZero();
    }
}
