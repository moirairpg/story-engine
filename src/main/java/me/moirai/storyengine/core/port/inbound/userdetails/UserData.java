package me.moirai.storyengine.core.port.inbound.userdetails;

import java.time.Instant;
import java.util.UUID;

import me.moirai.storyengine.common.enums.Role;

public record UserData(UUID publicId, String discordId, Role role, Instant creationDate) {}
