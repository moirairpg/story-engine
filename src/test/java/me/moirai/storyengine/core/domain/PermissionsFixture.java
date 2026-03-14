package me.moirai.storyengine.core.domain;

import java.util.HashSet;
import java.util.Set;

public class PermissionsFixture {

    private static final String OWNER_DISCORD_ID = "586678721356875";

    public static Permissions.Builder samplePermissions() {

        Set<String> userList = new HashSet<>();
        userList.add("613226587696519");
        userList.add("910602820805797");
        userList.add("643337806686791");
        userList.add("559802401039646");

        return Permissions.builder()
                .ownerId(OWNER_DISCORD_ID)
                .usersAllowedToRead(userList)
                .usersAllowedToWrite(userList);
    }
}
