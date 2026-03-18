package me.moirai.storyengine.infrastructure.security.authorization;

public enum AuthorizationOperation {
    READ_PERSONA,
    MODIFY_PERSONA,
    READ_ADVENTURE,
    MODIFY_ADVENTURE,
    DELETE_ADVENTURE,
    CREATE_ADVENTURE,
    READ_WORLD,
    MODIFY_WORLD,
    ADMIN_ONLY,
    AUTHENTICATED_USER
}
