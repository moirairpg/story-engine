package me.moirai.storyengine.common.dbutil;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class SearchPredicates {

    private static final String PERMISSIONS = "permissions";
    private static final String USERS_ALLOWED_TO_WRITE = "usersAllowedToWriteString";
    private static final String USERS_ALLOWED_TO_READ = "usersAllowedToReadString";
    private static final String OWNER_DISCORD_ID = "ownerId";
    private static final String VISIBILITY = "visibility";

    private SearchPredicates() {
    }

    public static <T> Predicate contains(CriteriaBuilder cb, Root<T> root, String fieldName, String fieldValue) {

        return cb.like(cb.upper(cb.toString(root.get(fieldName))),
                "%" + fieldValue.toUpperCase() + "%");
    }

    public static <T> Predicate contains(CriteriaBuilder cb, Root<T> root,
            String superFieldName, String subFieldName, String fieldValue) {

        return cb.like(cb.upper(root.get(superFieldName).get(subFieldName)),
                "%" + fieldValue.toUpperCase() + "%");
    }

    public static <T> Predicate canUserRead(CriteriaBuilder cb, Root<T> root, String discordUserId) {

        Predicate isPublic = contains(cb, root, VISIBILITY, "public");
        Predicate isOwner = cb.equal(root.get(PERMISSIONS).get(OWNER_DISCORD_ID), discordUserId);
        Predicate isAllowedToRead = contains(cb, root, PERMISSIONS, USERS_ALLOWED_TO_READ, discordUserId);
        Predicate isAllowedToWrite = contains(cb, root, PERMISSIONS, USERS_ALLOWED_TO_WRITE, discordUserId);

        return cb.or(isPublic, isOwner, isAllowedToRead, isAllowedToWrite);
    }

    public static <T> Predicate canUserWrite(CriteriaBuilder cb, Root<T> root, String discordUserId) {

        Predicate isOwner = cb.equal(root.get(PERMISSIONS).get(OWNER_DISCORD_ID), discordUserId);
        Predicate isAllowedToWrite = contains(cb, root, PERMISSIONS, USERS_ALLOWED_TO_WRITE, discordUserId);

        return cb.or(isOwner, isAllowedToWrite);
    }
}
