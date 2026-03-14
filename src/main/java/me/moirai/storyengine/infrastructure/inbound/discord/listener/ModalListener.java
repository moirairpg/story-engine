package me.moirai.storyengine.infrastructure.inbound.discord.listener;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import me.moirai.storyengine.common.usecases.UseCaseRunner;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureAuthorsNoteByChannelId;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureBumpByChannelId;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureNudgeByChannelId;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureRememberByChannelId;
import me.moirai.storyengine.core.port.inbound.discord.contextmenu.EditMessage;
import me.moirai.storyengine.core.port.inbound.discord.slashcommands.SayCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;

@Component
public class ModalListener extends ListenerAdapter {

    private static final String BUMP_MODAL = "bump";
    private static final String NUDGE_MODAL = "nudge";
    private static final String AUTHORS_NOTE_MODAL = "authorsNote";
    private static final String REMEMBER_MODAL = "remember";
    private static final String EDIT_MESSAGE_MODAL = "editMessage";
    private static final String SAY_AS_BOT_MODAL = "sayAsBot";

    private static final String UPDATED_ADVENTURE_CONTEXT_MODIFIER = "Updated adventure's context modifier";
    private static final String MESSAGE_EDITED = "Message edited.";
    private static final String WAITING_FOR_INPUT = "Waiting for input...";
    private static final String MESSAGE_ID = "messageId";
    private static final String MESSAGE_CONTENT = "content";
    private static final String INPUT_SENT = "Input sent.";

    private final UseCaseRunner useCaseRunner;
    private final DiscordListenerHelper discordListenerHelper;

    public ModalListener(
            UseCaseRunner useCaseRunner,
            DiscordListenerHelper discordListenerHelper) {

        this.useCaseRunner = useCaseRunner;
        this.discordListenerHelper = discordListenerHelper;
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {

        String modalId = event.getModalId();
        Guild guild = event.getGuild();
        TextChannel textChannel = event.getChannel().asTextChannel();
        User author = event.getMember().getUser();
        Member bot = guild.retrieveMember(event.getJDA().getSelfUser()).complete();

        if (!author.isBot()) {
            switch (modalId) {
                case SAY_AS_BOT_MODAL -> processSayAsBotModal(event, textChannel);
                case EDIT_MESSAGE_MODAL -> processEditMessageModal(event, bot, textChannel);
                case REMEMBER_MODAL -> processRememberModal(event, textChannel);
                case AUTHORS_NOTE_MODAL -> processAuthorsModeModal(event, textChannel);
                case NUDGE_MODAL -> processNudgeModal(event, textChannel);
                case BUMP_MODAL -> processBumpModal(event, textChannel);
            }
        }
    }

    private void processEditMessageModal(ModalInteractionEvent event, Member bot, TextChannel textChannel) {
        InteractionHook interactionHook = discordListenerHelper.sendNotification(event, WAITING_FOR_INPUT);
        String messageContent = event.getValue(MESSAGE_CONTENT).getAsString();
        String messageId = event.getValue(MESSAGE_ID).getAsString();
        Message message = textChannel.retrieveMessageById(messageId).complete();

        if (!message.getAuthor().getId().equals(bot.getId())) {
            discordListenerHelper.updateNotification(interactionHook,
                    "It's only possible to edit messages sent by " + getBotNickname(bot));
            return;
        }

        EditMessage useCase = EditMessage.build(textChannel.getId(), messageId, messageContent);

        useCaseRunner.run(useCase);

        discordListenerHelper.updateNotification(interactionHook, MESSAGE_EDITED);
    }

    private void processBumpModal(ModalInteractionEvent event, TextChannel textChannel) {
        InteractionHook interactionHook = discordListenerHelper.sendNotification(event, WAITING_FOR_INPUT);
        String bumpContent = event.getValue("bumpContent").getAsString();
        int bumpFrequency = Integer.valueOf(event.getValue("bumpFrequency").getAsString());

        useCaseRunner.run(UpdateAdventureBumpByChannelId.builder()
                .bump(bumpContent)
                .bumpFrequency(bumpFrequency)
                .channelId(textChannel.getId())
                .build());

        discordListenerHelper.updateNotification(interactionHook, UPDATED_ADVENTURE_CONTEXT_MODIFIER);
    }

    private void processNudgeModal(ModalInteractionEvent event, TextChannel textChannel) {
        InteractionHook interactionHook = discordListenerHelper.sendNotification(event, WAITING_FOR_INPUT);
        String nudgeContent = event.getValue("nudgeContent").getAsString();

        useCaseRunner.run(UpdateAdventureNudgeByChannelId.builder()
                .nudge(nudgeContent)
                .channelId(textChannel.getId())
                .requesterId(event.getMember().getId())
                .build());

        discordListenerHelper.updateNotification(interactionHook, UPDATED_ADVENTURE_CONTEXT_MODIFIER);
    }

    private void processAuthorsModeModal(ModalInteractionEvent event, TextChannel textChannel) {
        InteractionHook interactionHook = discordListenerHelper.sendNotification(event, WAITING_FOR_INPUT);
        String authorsNoteContent = event.getValue("authorsNoteContent").getAsString();

        useCaseRunner
                .run(UpdateAdventureAuthorsNoteByChannelId.builder()
                        .authorsNote(authorsNoteContent)
                        .channelId(textChannel.getId())
                        .requesterId(event.getMember().getId())
                        .build());

        discordListenerHelper.updateNotification(interactionHook, UPDATED_ADVENTURE_CONTEXT_MODIFIER);
    }

    private void processRememberModal(ModalInteractionEvent event, TextChannel textChannel) {
        InteractionHook interactionHook = discordListenerHelper.sendNotification(event, WAITING_FOR_INPUT);
        String rememberContent = event.getValue("rememberContent").getAsString();

        useCaseRunner.run(UpdateAdventureRememberByChannelId.builder()
                .remember(rememberContent)
                .channelId(textChannel.getId())
                .requesterId(event.getMember().getId())
                .build());

        discordListenerHelper.updateNotification(interactionHook, UPDATED_ADVENTURE_CONTEXT_MODIFIER);
    }

    private void processSayAsBotModal(ModalInteractionEvent event, TextChannel textChannel) {
        InteractionHook interactionHook = discordListenerHelper.sendNotification(event, WAITING_FOR_INPUT);
        String messageContent = event.getValue(MESSAGE_CONTENT).getAsString();

        SayCommand useCase = SayCommand.build(textChannel.getId(), messageContent);

        useCaseRunner.run(useCase);

        discordListenerHelper.updateNotification(interactionHook, INPUT_SENT);
    }

    private String getBotNickname(Member bot) {

        return StringUtils.isNotBlank(bot.getNickname()) ? bot.getNickname()
                : bot.getUser().getName();
    }
}
