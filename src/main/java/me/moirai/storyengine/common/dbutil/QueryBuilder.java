package me.moirai.storyengine.common.dbutil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import me.moirai.storyengine.common.enums.SortDirection;

public class QueryBuilder {

    private final String selectClause;
    private final List<Filter> filters;
    private final String sortField;
    private final SortDirection sortDirection;
    private final Integer limit;

    private QueryBuilder(String selectClause, List<Filter> filters, String sortField, SortDirection sortDirection, Integer limit) {
        this.selectClause = selectClause;
        this.filters = filters;
        this.sortField = sortField;
        this.sortDirection = sortDirection;
        this.limit = limit;
    }

    public String sql() {

        var sb = new StringBuilder(selectClause);
        appendWhereClause(sb);

        if (sortField != null) {
            sb.append(" ORDER BY ").append(sortField).append(" ").append(sortDirection.name());
        }

        if (limit != null) {
            sb.append(" LIMIT ").append(limit);
        }

        return sb.toString();
    }

    public Map<String, Object> parameters() {

        var params = new HashMap<String, Object>();
        for (Filter filter : filters) {
            if (filter.paramName() != null) {
                params.put(filter.paramName(), filter.value());
            }
        }

        return params;
    }

    protected void appendWhereClause(StringBuilder sb) {

        if (filters.isEmpty()) {
            return;
        }

        sb.append(" WHERE ");

        for (int i = 0; i < filters.size(); i++) {
            if (i > 0) {
                sb.append(" AND ");
            }
            sb.append("(").append(filters.get(i).clause()).append(")");
        }
    }

    protected List<Filter> filters() {
        return filters;
    }

    protected String selectClause() {
        return selectClause;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String selectClause;
        private final List<Filter> filters = new ArrayList<>();
        private String sortField;
        private SortDirection sortDirection;
        private Integer limit;

        public Builder select(String selectClause) {
            this.selectClause = selectClause;
            return this;
        }

        public Builder filter(Optional<Filter> filter) {
            filter.ifPresent(filters::add);
            return this;
        }

        public Builder filters(List<Filter> filters) {
            this.filters.addAll(filters);
            return this;
        }

        public Builder sortBy(String sortField, SortDirection direction) {
            this.sortField = sortField;
            this.sortDirection = direction;
            return this;
        }

        public Builder limit(int size) {
            this.limit = size;
            return this;
        }

        public QueryBuilder build() {
            return new QueryBuilder(selectClause, List.copyOf(filters), sortField, sortDirection, limit);
        }
    }
}
