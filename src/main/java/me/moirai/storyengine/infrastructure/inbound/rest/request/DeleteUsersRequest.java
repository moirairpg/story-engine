package me.moirai.storyengine.infrastructure.inbound.rest.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public record DeleteUsersRequest(
        @NotEmpty(message = "cannot be empty") List<String> usernames) {
}
