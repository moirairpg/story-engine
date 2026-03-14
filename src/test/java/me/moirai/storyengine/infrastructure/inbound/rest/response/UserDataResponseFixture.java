package me.moirai.storyengine.infrastructure.inbound.rest.response;

public class UserDataResponseFixture {

    public static UserDataResponse.Builder create() {

        return UserDataResponse.builder()
                .discordId("1234")
                .nickname("nickname")
                .username("username")
                .avatar("https://img.com/avatar.jpg");
    }
}
