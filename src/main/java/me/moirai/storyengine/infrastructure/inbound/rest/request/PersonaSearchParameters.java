package me.moirai.storyengine.infrastructure.inbound.rest.request;

import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchDirection;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchOperation;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchSortingField;

public class PersonaSearchParameters {

    private String name;
    private String ownerId;
    private Integer page;
    private Integer size;
    private SearchSortingField sortingField;
    private SearchDirection direction;
    private Visibility visibility;
    private SearchOperation operation;

    public PersonaSearchParameters() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerDiscordId(String ownerId) {
        this.ownerId = ownerId;
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

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public SearchOperation getOperation() {
        return operation;
    }

    public void setOperation(SearchOperation operation) {
        this.operation = operation;
    }
}
