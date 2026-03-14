package me.moirai.storyengine.infrastructure.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import me.moirai.storyengine.infrastructure.inbound.discord.contextmenu.DiscordContextMenuCommand;
import me.moirai.storyengine.infrastructure.inbound.discord.slashcommands.DiscordSlashCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;

@Configuration
public class JdaConfig {

    private static final Logger LOG = LoggerFactory.getLogger(JdaConfig.class);

    private static final String REGISTERED_EVENT_LISTENERS = "{} Discord event listeners have been registered";
    private static final String REGISTERED_SLASH_COMMANDS = "{} Discord slash commands have been registered";
    private static final String REGISTERED_CONTEXT_MENU_COMMANDS = "{} Discord context menu commands have been registered";

    private final String discordApiToken;
    private final String discordBotStatus;

    public JdaConfig(
        @Value("${moirai.discord.api.token}") String discordApiToken,
        @Value("${moirai.discord.bot.status}") String discordBotStatus) {

        this.discordApiToken = discordApiToken;
        this.discordBotStatus = discordBotStatus;
    }

    @Bean
    <T extends ListenerAdapter> JDA jda(
            List<T> eventListeners,
            List<? extends DiscordSlashCommand> slashCommands,
            List<? extends DiscordContextMenuCommand> ctxMenuCommands) {

        JDABuilder jdaBuilder = JDABuilder.createDefault(discordApiToken);

        for (T listener : eventListeners) {
            LOG.debug("Registering event listener: {}", listener.getClass().getSimpleName());
            jdaBuilder.addEventListeners(listener);
        }

        JDA jda = jdaBuilder
                .setActivity(Activity.customStatus(discordBotStatus))
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .build();

        for (DiscordSlashCommand slashCommand : slashCommands) {
            LOG.debug("Registering slash command: {}", slashCommand.getClass().getSimpleName());
            SlashCommandData slashCommandToBeCreated = Commands
                    .slash(slashCommand.getName(), slashCommand.getDescription())
                    .addOptions(slashCommand.getOptions());

            jda.upsertCommand(slashCommandToBeCreated).complete();
        }

        for (DiscordContextMenuCommand ctxMenuCommand : ctxMenuCommands) {
            LOG.debug("Registering context menu command: {}", ctxMenuCommand.getClass().getSimpleName());
            CommandData command = Commands.context(ctxMenuCommand.getCommandType(), ctxMenuCommand.getName());
            jda.upsertCommand(command).complete();
        }

        LOG.info(REGISTERED_EVENT_LISTENERS, eventListeners.size());
        LOG.info(REGISTERED_SLASH_COMMANDS, slashCommands.size());
        LOG.info(REGISTERED_CONTEXT_MENU_COMMANDS, ctxMenuCommands.size());

        return jda;
    }
}
