package me.moirai.storyengine.common.dto;

import java.util.UUID;

import jakarta.persistence.Embeddable;
import me.moirai.storyengine.common.enums.PermissionLevel;

@Embeddable
public record PermissionDto(
        UUID userId,
        PermissionLevel level) {}
