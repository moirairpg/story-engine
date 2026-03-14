package me.moirai.storyengine.infrastructure.inbound.rest.request;

import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchDirection;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchGameMode;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchModel;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchModeration;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchOperation;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchSortingField;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchVisibility;

public class AdventureSearchParameters {

    private String name;
    private String world;
    private String persona;
    private String ownerId;
    private boolean favorites;
    private boolean isMultiplayer;
    private Integer page;
    private Integer size;
    private SearchModel model;
    private SearchGameMode gameMode;
    private SearchModeration moderation;
    private SearchSortingField sortingField;
    private SearchDirection direction;
    private SearchVisibility visibility;
    private SearchOperation operation;

    public AdventureSearchParameters() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public String getPersona() {
        return persona;
    }

    public void setPersona(String persona) {
        this.persona = persona;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerDiscordId(String ownerId) {
        this.ownerId = ownerId;
    }

    public boolean isFavorites() {
        return favorites;
    }

    public void setFavorites(boolean favorites) {
        this.favorites = favorites;
    }

    public boolean isMultiplayer() {
        return isMultiplayer;
    }

    public void setMultiplayer(boolean isMultiplayer) {
        this.isMultiplayer = isMultiplayer;
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

    public SearchModel getModel() {
        return model;
    }

    public void setModel(SearchModel model) {
        this.model = model;
    }

    public SearchGameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(SearchGameMode gameMode) {
        this.gameMode = gameMode;
    }

    public SearchModeration getModeration() {
        return moderation;
    }

    public void setModeration(SearchModeration moderation) {
        this.moderation = moderation;
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
