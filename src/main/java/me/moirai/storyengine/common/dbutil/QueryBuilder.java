package me.moirai.storyengine.common.dbutil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class QueryBuilder {

    private final String selectClause;
    private final List<Filter> filters;

    private QueryBuilder(String selectClause, List<Filter> filters) {
        this.selectClause = selectClause;
        this.filters = filters;
    }

    public String sql() {

        var sb = new StringBuilder(selectClause);
        appendWhereClause(sb);

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
            sb.append(filters.get(i).clause());
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

        public QueryBuilder build() {
            return new QueryBuilder(selectClause, List.copyOf(filters));
        }
    }
}
