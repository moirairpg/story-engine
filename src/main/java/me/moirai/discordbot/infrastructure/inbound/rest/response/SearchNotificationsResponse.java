package me.moirai.discordbot.infrastructure.inbound.rest.response;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchNotificationsResponse {

    private int page;
    private int totalPages;
    private int resultsInPage;
    private long totalResults;
    private List<NotificationResponse> results;

    public SearchNotificationsResponse() {
    }

    private SearchNotificationsResponse(Builder builder) {
        this.page = builder.page;
        this.totalPages = builder.totalPages;
        this.resultsInPage = builder.resultsInPage;
        this.totalResults = builder.totalResults;
        this.results = builder.results;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getPage() {
        return page;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getResultsInPage() {
        return resultsInPage;
    }

    public long getTotalResults() {
        return totalResults;
    }

    public List<NotificationResponse> getResults() {
        return results;
    }

    public static final class Builder {
        private int page;
        private int totalPages;
        private int resultsInPage;
        private long totalResults;
        private List<NotificationResponse> results;

        private Builder() {
        }

        public Builder page(int page) {
            this.page = page;
            return this;
        }

        public Builder totalPages(int totalPages) {
            this.totalPages = totalPages;
            return this;
        }

        public Builder resultsInPage(int resultsInPage) {
            this.resultsInPage = resultsInPage;
            return this;
        }

        public Builder totalResults(long totalResults) {
            this.totalResults = totalResults;
            return this;
        }

        public Builder results(List<NotificationResponse> results) {

            this.results = emptyIfNull(results);
            return this;
        }

        public SearchNotificationsResponse build() {
            return new SearchNotificationsResponse(this);
        }
    }
}
