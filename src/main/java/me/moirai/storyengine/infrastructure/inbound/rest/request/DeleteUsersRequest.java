package me.moirai.storyengine.infrastructure.inbound.rest.request;

import java.util.List;
import java.util.UUID;

public record DeleteUsersRequest(List<UUID> userIds) {
}
