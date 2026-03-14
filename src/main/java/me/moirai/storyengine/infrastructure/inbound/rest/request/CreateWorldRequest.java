package me.moirai.storyengine.infrastructure.inbound.rest.request;

import java.util.List;
import java.util.Set;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class CreateWorldRequest {

    @NotEmpty(message = "cannot be empty")
    @NotNull(message = "cannot be null")
    private String name;

    @NotEmpty(message = "cannot be empty")
    @NotNull(message = "cannot be null")
    private String description;

    @NotEmpty(message = "cannot be empty")
    @NotNull(message = "cannot be null")
    private String adventureStart;

    @NotEmpty(message = "cannot be empty")
    @NotNull(message = "cannot be null")
    private String visibility;
    private Set<String> usersAllowedToWrite;
    private Set<String> usersAllowedToRead;
    private List<CreateLorebookEntryRequest> lorebook;

    public CreateWorldRequest() {
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

    public Set<String> getUsersAllowedToWrite() {
        return usersAllowedToWrite;
    }

    public Set<String> getUsersAllowedToRead() {
        return usersAllowedToRead;
    }

    public List<CreateLorebookEntryRequest> getLorebook() {
        return lorebook;
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

    public void setUsersAllowedToWrite(Set<String> usersAllowedToWrite) {
        this.usersAllowedToWrite = usersAllowedToWrite;
    }

    public void setUsersAllowedToRead(Set<String> usersAllowedToRead) {
        this.usersAllowedToRead = usersAllowedToRead;
    }

    public void setLorebook(List<CreateLorebookEntryRequest> lorebook) {
        this.lorebook = lorebook;
    }
}
