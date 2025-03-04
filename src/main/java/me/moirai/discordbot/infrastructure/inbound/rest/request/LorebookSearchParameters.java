package me.moirai.discordbot.infrastructure.inbound.rest.request;

import me.moirai.discordbot.infrastructure.inbound.rest.request.enums.SearchDirection;
import me.moirai.discordbot.infrastructure.inbound.rest.request.enums.SearchSortingField;

public class LorebookSearchParameters {

    private String name;
    private Integer page;
    private Integer size;
    private SearchSortingField sortingField;
    private SearchDirection direction;

    public LorebookSearchParameters() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public SearchSortingField getSortingField() {
        return sortingField;
    }

    public void setSortingField(SearchSortingField sortingField) {
        this.sortingField = sortingField;
    }

    public SearchDirection getDirection() {
        return direction;
    }

    public void setDirection(SearchDirection direction) {
        this.direction = direction;
    }
}
