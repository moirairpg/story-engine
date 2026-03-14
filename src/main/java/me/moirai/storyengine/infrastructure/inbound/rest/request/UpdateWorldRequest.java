package me.moirai.storyengine.infrastructure.inbound.rest.request;

import java.util.Set;

public class UpdateWorldRequest {

    private String name;
    private String description;
    private String adventureStart;
    private String visibility;
    private Set<String> usersAllowedToWriteToAdd;
    private Set<String> usersAllowedToWriteToRemove;
    private Set<String> usersAllowedToReadToAdd;
    private Set<String> usersAllowedToReadToRemove;

    public UpdateWorldRequest() {
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getAdventureStart() {
        return adventureStart;
    }

    public String getVisibility() {
        return visibility;
    }

    public Set<String> getUsersAllowedToWriteToAdd() {
        return usersAllowedToWriteToAdd;
    }

    public Set<String> getUsersAllowedToWriteToRemove() {
        return usersAllowedToWriteToRemove;
    }

    public Set<String> getUsersAllowedToReadToAdd() {
        return usersAllowedToReadToAdd;
    }

    public Set<String> getUsersAllowedToReadToRemove() {
        return usersAllowedToReadToRemove;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAdventureStart(String adventureStart) {
        this.adventureStart = adventureStart;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public void setUsersAllowedToWriteToAdd(Set<String> usersAllowedToWriteToAdd) {
        this.usersAllowedToWriteToAdd = usersAllowedToWriteToAdd;
    }

    public void setUsersAllowedToWriteToRemove(Set<String> usersAllowedToWriteToRemove) {
        this.usersAllowedToWriteToRemove = usersAllowedToWriteToRemove;
    }

    public void setUsersAllowedToReadToAdd(Set<String> usersAllowedToReadToAdd) {
        this.usersAllowedToReadToAdd = usersAllowedToReadToAdd;
    }

    public void setUsersAllowedToReadToRemove(Set<String> usersAllowedToReadToRemove) {
        this.usersAllowedToReadToRemove = usersAllowedToReadToRemove;
    }
}