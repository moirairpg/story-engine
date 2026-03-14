package me.moirai.storyengine.core.port.inbound.adventure;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = SearchAdventureLorebookEntriesResult.Builder.class)
public final class SearchAdventureLorebookEntriesResult {

    private final int page;
    private final int items;
    private final long totalItems;
    private final int totalPages;
    private final List<AdventureLorebookEntryDetails> results;

    private SearchAdventureLorebookEntriesResult(Builder builder) {

        this.page = builder.page;
        this.items = builder.items;
        this.totalItems = builder.totalItems;
        this.totalPages = builder.totalPages;
        this.results = unmodifiableList(builder.results);
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

    public List<AdventureLorebookEntryDetails> getResults() {
        return results;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {

        private int page;
        private int items;
        private long totalItems;
        private int totalPages;
        private List<AdventureLorebookEntryDetails> results = new ArrayList<>();

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

        public Builder results(List<AdventureLorebookEntryDetails> results) {

            if (results != null) {
                this.results = results;
            }

            return this;
        }

        public SearchAdventureLorebookEntriesResult build() {
            return new SearchAdventureLorebookEntriesResult(this);
        }
    }
}