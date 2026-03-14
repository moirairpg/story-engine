package me.moirai.storyengine.infrastructure.inbound.rest.mapper;

import org.springframework.stereotype.Component;

import me.moirai.storyengine.core.port.inbound.discord.userdetails.UserDetailsResult;
import me.moirai.storyengine.infrastructure.inbound.rest.response.UserDataResponse;

@Component
public class UserDataResponseMapper {

    public UserDataResponse toResponse(UserDetailsResult discordUser) {

        return UserDataResponse.builder()
                .discordId(discordUser.getDiscordId())
                .avatar(discordUser.getAvatarUrl())
                .nickname(discordUser.getNickname())
                .username(discordUser.getUsername())
                .joinDate(discordUser.getJoinDate())
                .build();
    }
}
