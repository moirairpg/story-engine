package me.moirai.storyengine.infrastructure.outbound.adapter.discord;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import me.moirai.storyengine.core.port.inbound.discord.DiscordUserDetails;
import me.moirai.storyengine.core.port.DiscordUserDetailsPort;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

@Component
public class DiscordUserDetailsAdapter implements DiscordUserDetailsPort {

    private static final String ERROR_RETRIEVING_DISCORD_USER = "Error when retrieving Discord user from API";

    private static final Logger LOG = LoggerFactory.getLogger(DiscordUserDetailsAdapter.class);

    private final JDA jda;

    @Lazy
    public DiscordUserDetailsAdapter(JDA jda) {
        this.jda = jda;
    }

    @Override
    public Optional<DiscordUserDetails> getUserById(String userId) {

        try {
            User user = jda.retrieveUserById(userId).complete();
            return Optional.of(DiscordUserDetails.builder()
                    .id(user.getId())
                    .username(user.getName())
                    .mention(user.getAsMention())
                    .nickname(user.getGlobalName())
                    .avatarUrl(user.getAvatarUrl())
                    .build());
        } catch (Exception e) {
            LOG.warn(ERROR_RETRIEVING_DISCORD_USER, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<DiscordUserDetails> getGuildMemberById(String userId, String guildId) {

        try {
            Member member = jda.getGuildById(guildId)
                    .retrieveMemberById(userId)
                    .complete();

            return Optional.of(DiscordUserDetails.builder()
                    .id(member.getId())
                    .username(member.getUser().getName())
                    .nickname(isNotBlank(member.getNickname()) ? member.getNickname() : member.getUser().getGlobalName())
                    .mention(member.getAsMention())
                    .avatarUrl(member.getAvatarUrl())
                    .build());
        } catch (Exception e) {
            LOG.warn(ERROR_RETRIEVING_DISCORD_USER, e);
            return Optional.empty();
        }
    }

    @Override
    public DiscordUserDetails getBotUser() {

        User bot = jda.getSelfUser();

        return DiscordUserDetails.builder()
                .id(bot.getId())
                .username(bot.getName())
                .nickname(bot.getGlobalName())
                .avatarUrl(bot.getAvatarUrl())
                .build();
    }
}
