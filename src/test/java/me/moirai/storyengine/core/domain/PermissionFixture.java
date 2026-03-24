package me.moirai.storyengine.core.domain;

import java.util.HashSet;
import java.util.Set;

import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.enums.PermissionLevel;

public class PermissionFixture {

    public static final Long OWNER_ID = 1001L;
    public static final Long WRITER_ID = 1002L;
    public static final Long READER_ID = 1003L;

    public static Set<Permission> samplePermissions() {

        var permissions = new HashSet<Permission>();
        permissions.add(new Permission(OWNER_ID, PermissionLevel.OWNER));
        permissions.add(new Permission(WRITER_ID, PermissionLevel.WRITE));
        permissions.add(new Permission(READER_ID, PermissionLevel.READ));
        return permissions;
    }
}
