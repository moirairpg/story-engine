package me.moirai.storyengine.common.security.authorization;

public enum AuthorizationOperation {
    UPDATE_PERSONA,
    DELETE_PERSONA,
    VIEW_PERSONA,

    UPDATE_WORLD,
    DELETE_WORLD,
    VIEW_WORLD,

    UPDATE_ADVENTURE,
    DELETE_ADVENTURE,
    VIEW_ADVENTURE,

    MANAGE_USER,
    UPDATE_USER_USERNAME,
    UPDATE_USER_ROLE,
}
