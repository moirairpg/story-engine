package me.moirai.storyengine.infrastructure.inbound.discord.listener;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import me.moirai.storyengine.common.usecases.UseCaseRunner;
import me.moirai.storyengine.core.application.helper.AdventureHelper;
import me.moirai.storyengine.core.port.inbound.discord.messagereceived.AuthorModeRequest;
import me.moirai.storyengine.core.port.inbound.discord.messagereceived.ChatModeRequest;
import me.moirai.storyengine.core.port.inbound.discord.messagereceived.RpgModeRequest;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
public class MessageReceivedListener extends ListenerAdapter {

    private static final String AUTHOR_MODE = "AUTHOR";
    private static final String RPG_MODE = "RPG";
    private static final String CHAT_MODE = "CHAT";

    private final UseCaseRunner useCaseRunner;
    private final AdventureHelper adventureHelper;
    private final DiscordListenerErrorHandler errorHandler;

    public MessageReceivedListener(UseCaseRunner useCaseRunner,
            AdventureHelper adventureHelper,
            DiscordListenerErrorHandler errorHandler) {

        this.useCaseRunner = useCaseRunner;
        this.adventureHelper = adventureHelper;
        this.errorHandler = errorHandler;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        try {
            Member author = event.getMember();
            Member bot = event.getGuild().getMember(event.getJDA().getSelfUser());
            String channelId = event.getChannel().getId();
            String messageContent = event.getMessage().getContentRaw();
            String gameMode = adventureHelper.getGameModeByChannelId(channelId);

            if (StringUtils.isNoneBlank(messageContent, gameMode) && !author.getUser().isBot()) {
                String botUsername = bot.getUser().getName();
                String botNickname = isNotBlank(bot.getNickname()) ? bot.getNickname() : botUsername;

                event.getChannel().sendTyping().complete();

                switch (gameMode) {
                    case CHAT_MODE -> processChatMode(event, botUsername, botNickname);
                    case RPG_MODE -> processRpgMode(event, botUsername, botNickname);
                    case AUTHOR_MODE -> processAuthorMode(event, botUsername, botNickname);
                }
            }
        } catch (Exception e) {
            handleError(event, e);
        }
    }

    private void processAuthorMode(MessageReceivedEvent event, String botUsername, String botNickname) {

        Message message = event.getMessage();
        Member author = event.getMember();
        Member bot = event.getGuild().getMember(event.getJDA().getSelfUser());
        List<String> mentions = message.getMentions()
                .getMembers()
                .stream()
                .map(Member::getId)
                .toList();

        String guildId = event.getGuild().getId();
        String channelId = event.getChannel().getId();

        AuthorModeRequest request = AuthorModeRequest.builder()
                .authordDiscordId(author.getId())
                .channelId(channelId)
                .messageId(message.getId())
                .guildId(guildId)
                .isBotMentioned(mentions.contains(bot.getId()))
                .mentionedUsersIds(mentions)
                .botUsername(botUsername)
                .botNickname(botNickname)
                .build();

        useCaseRunner.run(request)
                .doOnError(error -> handleError(event, error))
                .subscribe();
    }

    private void processRpgMode(MessageReceivedEvent event, String botUsername, String botNickname) {

        Message message = event.getMessage();
        Member author = event.getMember();
        Member bot = event.getGuild().getMember(event.getJDA().getSelfUser());
        List<String> mentions = message.getMentions()
                .getMembers()
                .stream()
                .map(Member::getId)
                .toList();

        String guildId = event.getGuild().getId();
        String channelId = event.getChannel().getId();

        RpgModeRequest request = RpgModeRequest.builder()
                .authordDiscordId(author.getId())
                .channelId(channelId)
                .messageId(message.getId())
                .guildId(guildId)
                .isBotMentioned(mentions.contains(bot.getId()))
                .mentionedUsersIds(mentions)
                .botUsername(botUsername)
                .botNickname(botNickname)
                .build();

        useCaseRunner.run(request)
                .doOnError(error -> handleError(event, error))
                .subscribe();
    }

    private void processChatMode(MessageReceivedEvent event, String botUsername, String botNickname) {

        Message message = event.getMessage();
        Member author = event.getMember();
        Member bot = event.getGuild().getMember(event.getJDA().getSelfUser());
        List<String> mentions = message.getMentions()
                .getMembers()
                .stream()
                .map(Member::getId)
                .toList();

        String guildId = event.getGuild().getId();
        String channelId = event.getChannel().getId();

        ChatModeRequest request = ChatModeRequest.builder()
                .authordDiscordId(author.getId())
                .channelId(channelId)
                .messageId(message.getId())
                .guildId(guildId)
                .isBotMentioned(mentions.contains(bot.getId()))
                .mentionedUsersIds(mentions)
                .botUsername(botUsername)
                .botNickname(botNickname)
                .build();

        useCaseRunner.run(request)
                .doOnError(error -> handleError(event, error))
                .subscribe();
    }

    private void handleError(MessageReceivedEvent event, Throwable error) {

        Member member = event.getMember();
        MessageChannelUnion channel = event.getChannel();
        errorHandler.handleError(member, channel, error);
    }
}
