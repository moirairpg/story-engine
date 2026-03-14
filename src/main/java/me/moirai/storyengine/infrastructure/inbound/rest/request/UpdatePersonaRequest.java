package me.moirai.storyengine.infrastructure.inbound.rest.request;

import java.util.Set;

public class UpdatePersonaRequest {

    private String name;
    private String personality;
    private String visibility;
    private Set<String> usersAllowedToWriteToAdd;
    private Set<String> usersAllowedToWriteToRemove;
    private Set<String> usersAllowedToReadToAdd;
    private Set<String> usersAllowedToReadToRemove;

    public UpdatePersonaRequest() {
    }

    public String getName() {
        return name;
    }

    public String getPersonality() {
        return personality;
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

    public void setPersonality(String personality) {
        this.personality = personality;
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