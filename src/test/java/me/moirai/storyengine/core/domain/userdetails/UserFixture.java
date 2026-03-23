package me.moirai.storyengine.core.domain.userdetails;

import static me.moirai.storyengine.common.enums.Role.ADMIN;
import static me.moirai.storyengine.common.enums.Role.PLAYER;

import java.util.UUID;

import org.springframework.test.util.ReflectionTestUtils;

public class UserFixture {

    public static final UUID PUBLIC_ID = UUID.fromString("aabbccdd-1111-0000-0000-000000000000");
    public static final Long NUMERIC_ID = 1L;

    public static User.Builder player() {

        return User.builder()
                .discordId("12345")
                .role(PLAYER);
    }

    public static User.Builder admin() {

        return User.builder()
                .discordId("12345")
                .role(ADMIN);
    }

    public static User playerWithId() {

        var user = player().build();
        ReflectionTestUtils.setField(user, "id", NUMERIC_ID);
        ReflectionTestUtils.setField(user, "publicId", PUBLIC_ID);
        return user;
    }

    public static User adminWithId() {

        var user = admin().build();
        ReflectionTestUtils.setField(user, "id", NUMERIC_ID);
        ReflectionTestUtils.setField(user, "publicId", PUBLIC_ID);
        return user;
    }
}
