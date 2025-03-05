package me.moirai.discordbot.infrastructure.inbound.rest.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class CreateLorebookEntryRequest {

    @NotEmpty(message = "cannot be empty")
    @NotNull(message = "cannot be null")
    private String name;
    private String regex;

    @NotEmpty(message = "cannot be empty")
    @NotNull(message = "cannot be null")
    private String description;
    private String playerDiscordId;

    public CreateLorebookEntryRequest() {
    }

    public String getName() {
        return name;
    }

    public String getRegex() {
        return regex;
    }

    public String getDescription() {
        return description;
    }

    public String getPlayerDiscordId() {
        return playerDiscordId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPlayerDiscordId(String playerDiscordId) {
        this.playerDiscordId = playerDiscordId;
    }
}
