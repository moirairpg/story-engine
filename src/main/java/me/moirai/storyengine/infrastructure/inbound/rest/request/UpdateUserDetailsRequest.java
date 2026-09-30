package me.moirai.storyengine.infrastructure.inbound.rest.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import me.moirai.storyengine.infrastructure.inbound.rest.validation.Moderated;

public record UpdateUserDetailsRequest(
        @Moderated
        @NotEmpty(message = "cannot be empty")
        @Size(min = 2, max = 32, message = "must be between 2 and 32 characters") String displayName,

        @Size(max = 2000, message = "cannot be longer than 2000 characters") String bio) {
}
