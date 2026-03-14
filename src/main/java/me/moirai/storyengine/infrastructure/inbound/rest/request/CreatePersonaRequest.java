package me.moirai.storyengine.infrastructure.inbound.rest.request;

import java.util.Set;

public class CreatePersonaRequest {

    private String name;
    private String personality;
    private String visibility;
    private Set<String> usersAllowedToWrite;
    private Set<String> usersAllowedToRead;

    public CreatePersonaRequest() {
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

    public Set<String> getUsersAllowedToWrite() {
        return usersAllowedToWrite;
    }

    public Set<String> getUsersAllowedToRead() {
        return usersAllowedToRead;
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

    public void setUsersAllowedToWrite(Set<String> usersAllowedToWrite) {
        this.usersAllowedToWrite = usersAllowedToWrite;
    }

    public void setUsersAllowedToRead(Set<String> usersAllowedToRead) {
        this.usersAllowedToRead = usersAllowedToRead;
    }
}
