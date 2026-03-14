package me.moirai.storyengine.core.port.inbound.adventure;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = SearchAdventuresResult.Builder.class)
public final class SearchAdventuresResult {

    private final int page;
    private final int totalPages;
    private final int items;
    private final long totalItems;
    private final List<AdventureDetails> results;

    private SearchAdventuresResult(Builder builder) {
        this.page = builder.page;
        this.totalPages = builder.totalPages;
        this.items = builder.items;
        this.totalItems = builder.totalItems;
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

    public List<AdventureDetails> getResults() {
        return results;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {

        private int page;
        private int totalPages;
        private int items;
        private long totalItems;
        private List<AdventureDetails> results;

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

        public Builder results(List<AdventureDetails> results) {
            this.results = results;
            return this;
        }

        public SearchAdventuresResult build() {
            return new SearchAdventuresResult(this);
        }
    }
}
