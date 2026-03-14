package me.moirai.storyengine.infrastructure.inbound.rest.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class CreateUserRequest {

    @NotEmpty(message = "cannot be empty")
    @NotNull(message = "cannot be null")
    private String discordId;

    public CreateUserRequest() {
    }

    public String getDiscordId() {
        return discordId;
    }

    public void setDiscordId(String discordId) {
        this.discordId = discordId;
    }
}
