package me.moirai.storyengine.infrastructure.inbound.rest.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.application.usecase.discord.userdetails.result.UserDetailsResult;
import me.moirai.storyengine.infrastructure.inbound.rest.response.UserDataResponse;

@ExtendWith(MockitoExtension.class)
public class UserDataResponseMapperTest {

    @InjectMocks
    private UserDataResponseMapper mapper;

    @Test
    public void mapUserDataResponse_whenValidData_thenObjectIsMapped() {

        // Given
        UserDetailsResult input = UserDetailsResult.builder()
                .discordId("1234")
                .avatarUrl("https://img;com/avatar.jpg")
                .nickname("nickname")
                .username("username")
                .build();

        // When
        UserDataResponse result = mapper.toResponse(input);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDiscordId()).isEqualTo(input.getDiscordId());
        assertThat(result.getAvatar()).isEqualTo(input.getAvatarUrl());
        assertThat(result.getNickname()).isEqualTo(input.getNickname());
        assertThat(result.getUsername()).isEqualTo(input.getUsername());
    }
}
