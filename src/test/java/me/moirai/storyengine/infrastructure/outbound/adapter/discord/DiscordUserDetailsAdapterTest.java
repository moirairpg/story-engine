package me.moirai.storyengine.infrastructure.outbound.adapter.discord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDetails;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DiscordUserDetailsAdapterTest {

    @Mock
    private User user;

    @Mock
    private Member member;

    @Mock
    private Guild guild;

    @Mock
    private JDA jda;

    @InjectMocks
    private DiscordUserDetailsAdapter adapter;

    @Test
    void getUserById_whenUserIsFound_thenUserIsReturned() {

        // Given
        String userId = "USRID";
        String username = "user.name";
        String mention = "<@USRID>";

        CacheRestAction<User> userCachedAction = mock(CacheRestAction.class);

        when(jda.retrieveUserById(anyString())).thenReturn(userCachedAction);
        when(userCachedAction.complete()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(user.getName()).thenReturn(username);
        when(user.getAsMention()).thenReturn(mention);

        // When
        Optional<DiscordUserDetails> result = adapter.getUserById(userId);

        // Then
        assertThat(result).isNotNull().isNotEmpty();
        assertThat(result.get().getUsername()).isEqualTo(username);
        assertThat(result.get().getMention()).isEqualTo(mention);
        assertThat(result.get().getId()).isEqualTo(userId);
    }

    @Test
    void getUserById_whenErrorIsThrown_thenEmptyResult() {

        // Given
        String userId = "USRID";

        when(jda.retrieveUserById(anyString())).thenThrow(IllegalStateException.class);

        // When
        Optional<DiscordUserDetails> result = adapter.getUserById(userId);

        // Then
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    void getGuildMemberById_whenUserIsFound_thenUserIsReturned() {

        // Given
        String guildId = "GLDID";
        String userId = "USRID";
        String username = "user.name";
        String nickname = "nickname";
        String mention = "<@USRID>";

        CacheRestAction<Member> userCachedAction = mock(CacheRestAction.class);

        when(jda.getGuildById(anyString())).thenReturn(guild);
        when(guild.retrieveMemberById(anyString())).thenReturn(userCachedAction);
        when(userCachedAction.complete()).thenReturn(member);
        when(member.getId()).thenReturn(userId);
        when(member.getAsMention()).thenReturn(mention);
        when(member.getNickname()).thenReturn(nickname);
        when(member.getUser()).thenReturn(user);
        when(user.getName()).thenReturn(username);

        // When
        Optional<DiscordUserDetails> result = adapter.getGuildMemberById(guildId, userId);

        // Then
        assertThat(result).isNotNull().isNotEmpty();
        assertThat(result.get().getUsername()).isEqualTo(username);
        assertThat(result.get().getMention()).isEqualTo(mention);
        assertThat(result.get().getId()).isEqualTo(userId);
        assertThat(result.get().getNickname()).isEqualTo(nickname);
    }

    @Test
    void getGuildMemberById_whenErrorIsThrown_thenEmptyResult() {

        // Given
        String guildId = "GLDID";
        String userId = "USRID";

        when(jda.getGuildById(anyString())).thenThrow(IllegalStateException.class);

        // When
        Optional<DiscordUserDetails> result = adapter.getGuildMemberById(guildId, userId);

        // Then
        assertThat(result).isNotNull().isEmpty();
    }
}
