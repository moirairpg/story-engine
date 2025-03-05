package me.moirai.discordbot.core.application.usecase.persona.result;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.util.ArrayList;
import java.util.List;

public final class SearchPersonasResult {

    private final int page;
    private final int items;
    private final long totalItems;
    private final int totalPages;
    private final List<GetPersonaResult> results;

    private SearchPersonasResult(Builder builder) {

        this.page = builder.page;
        this.items = builder.items;
        this.totalItems = builder.totalItems;
        this.totalPages = builder.totalPages;
        this.results = unmodifiableList(new ArrayList<>(isEmpty(builder.results) ? emptyList() : builder.results));
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getPage() {
        return page;
    }

    public int getItems() {
        return items;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public List<GetPersonaResult> getResults() {
        return results;
    }

    public static final class Builder {

        private int page;
        private int items;
        private long totalItems;
        private int totalPages;
        private List<GetPersonaResult> results;

        private Builder() {
        }

        public Builder page(int page) {
            this.page = page;
            return this;
        }

        public Builder items(int items) {
            this.items = items;
            return this;
        }

        public Builder totalItems(long totalItems) {
            this.totalItems = totalItems;
            return this;
        }

        public Builder totalPages(int totalPages) {
            this.totalPages = totalPages;
            return this;
        }

        public Builder results(List<GetPersonaResult> results) {
            this.results = results;
            return this;
        }

        public SearchPersonasResult build() {
            return new SearchPersonasResult(this);
        }
    }
}