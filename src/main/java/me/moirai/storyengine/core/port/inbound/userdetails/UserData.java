package me.moirai.storyengine.core.port.inbound.userdetails;

import java.time.OffsetDateTime;
import java.util.UUID;

import me.moirai.storyengine.common.enums.Role;

public record UserData(UUID publicId, String discordId, Role role, OffsetDateTime creationDate) {}
