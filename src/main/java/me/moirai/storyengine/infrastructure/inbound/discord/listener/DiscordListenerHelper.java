package me.moirai.storyengine.infrastructure.inbound.discord.listener;

import static java.util.concurrent.TimeUnit.SECONDS;

import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

@Component
public class DiscordListenerHelper {

    private static final int EPHEMERAL_MESSAGE_TTL = 10;

    public InteractionHook sendNotification(IReplyCallback event, String message) {
        return event.reply(message).setEphemeral(true).complete();
    }

    public Message updateNotification(InteractionHook interactionHook, String newContent) {

        return interactionHook.editOriginal(newContent)
                .onSuccess(msg -> msg.delete().completeAfter(EPHEMERAL_MESSAGE_TTL, SECONDS))
                .complete();
    }
}
