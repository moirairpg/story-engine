package me.moirai.storyengine.common.dbutil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import me.moirai.storyengine.common.enums.SortDirection;

public class PaginatedQuery {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;
    private static final SortDirection DEFAULT_DIRECTION = SortDirection.DESC;

    private final QueryBuilder queryBuilder;
    private final String sortField;
    private final SortDirection direction;
    private final int page;
    private final int size;

    private PaginatedQuery(
            QueryBuilder queryBuilder,
            String sortField,
            SortDirection direction,
            int page,
            int size) {

        this.queryBuilder = queryBuilder;
        this.sortField = sortField;
        this.direction = direction;
        this.page = page;
        this.size = size;
    }

    public String sql() {

        var sb = new StringBuilder(queryBuilder.selectClause());
        queryBuilder.appendWhereClause(sb);

        sb.append(" ORDER BY ").append(sortField).append(" ").append(direction.name());
        sb.append(" LIMIT :limit OFFSET :offset");

        return sb.toString();
    }

    public String countSql() {

        var sb = new StringBuilder("SELECT COUNT(*) FROM (");
        sb.append(queryBuilder.selectClause());
        queryBuilder.appendWhereClause(sb);
        sb.append(") AS count_query");

        return sb.toString();
    }

    public Map<String, Object> parameters() {

        var params = new HashMap<>(queryBuilder.parameters());
        params.put("limit", size);
        params.put("offset", (page - 1) * size);

        return params;
    }

    public Map<String, Object> countParameters() {
        return queryBuilder.parameters();
    }

    public int page() {
        return page;
    }

    public int size() {
        return size;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String selectClause;
        private final List<Filter> filters = new ArrayList<>();
        private String sortField;
        private SortDirection direction;
        private Integer page;
        private Integer size;

        public Builder select(String selectClause) {
            this.selectClause = selectClause;
            return this;
        }

        public Builder filter(Optional<Filter> filter) {
            filter.ifPresent(filters::add);
            return this;
        }

        public Builder sortBy(String sortField, SortDirection direction) {
            this.sortField = sortField;
            this.direction = direction;
            return this;
        }

        public Builder page(Integer page, Integer size) {
            this.page = page;
            this.size = size;
            return this;
        }

        public PaginatedQuery build() {

            if (sortField == null || sortField.isBlank()) {
                throw new IllegalArgumentException("Sort field is required");
            }

            var queryBuilder = QueryBuilder.builder()
                    .select(selectClause)
                    .filters(filters)
                    .build();

            return new PaginatedQuery(
                    queryBuilder,
                    sortField,
                    resolveDirection(direction),
                    resolvePage(page),
                    resolveSize(size));
        }

        private SortDirection resolveDirection(SortDirection direction) {
            return direction == null ? DEFAULT_DIRECTION : direction;
        }

        private int resolvePage(Integer page) {
            return page == null || page < 1 ? DEFAULT_PAGE : page;
        }

        private int resolveSize(Integer size) {
            return size == null || size < 1 ? DEFAULT_SIZE : size;
        }
    }
}
