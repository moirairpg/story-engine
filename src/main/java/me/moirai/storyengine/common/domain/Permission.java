package me.moirai.storyengine.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import me.moirai.storyengine.common.enums.PermissionLevel;

@Embeddable
public record Permission(
        @Column(name = "user_id") Long userId,
        @Enumerated(EnumType.STRING) @Column(name = "permission") PermissionLevel level) {}
