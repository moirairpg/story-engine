package me.moirai.storyengine.infrastructure.inbound.rest.request;

import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchDirection;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchOperation;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchNotificationSortingField;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchVisibility;

public class NotificationSearchParameters {

    private String senderDiscordId;
    private String receiverDiscordId;
    private String type;
    private String global;
    private String interactable;
    private Integer page;
    private Integer size;
    private SearchNotificationSortingField sortingField;
    private SearchDirection direction;
    private SearchVisibility visibility;
    private SearchOperation operation;

    public NotificationSearchParameters() {
    }

    public String getSenderDiscordId() {
        return senderDiscordId;
    }

    public void setSenderDiscordId(String name) {
        this.senderDiscordId = name;
    }

    public String getReceiverDiscordId() {
        return receiverDiscordId;
    }

    public void setReceiverDiscordId(String ownerId) {
        this.receiverDiscordId = ownerId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGlobal() {
        return global;
    }

    public void setGlobal(String favorites) {
        this.global = favorites;
    }

    public String getInteractable() {
        return interactable;
    }

    public void setInteractable(String isInteractable) {
        this.interactable = isInteractable;
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

    public SearchNotificationSortingField getSortingField() {
        return sortingField;
    }

    public void setSortingField(SearchNotificationSortingField sortingField) {
        this.sortingField = sortingField;
    }

    public SearchDirection getDirection() {
        return direction;
    }

    public void setDirection(SearchDirection direction) {
        this.direction = direction;
    }

    public SearchVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(SearchVisibility visibility) {
        this.visibility = visibility;
    }

    public SearchOperation getOperation() {
        return operation;
    }

    public void setOperation(SearchOperation operation) {
        this.operation = operation;
    }
}
