package me.moirai.storyengine.infrastructure.inbound.discord.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.events.ExceptionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.SessionDisconnectEvent;
import net.dv8tion.jda.api.events.session.SessionInvalidateEvent;
import net.dv8tion.jda.api.events.session.SessionResumeEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
public class BotStatusListener extends ListenerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(BotStatusListener.class);

    @Override
    public void onReady(ReadyEvent event) {

        String botName = event.getJDA().getSelfUser().getName();
        LOG.info("{}'s Discord session has been established.", botName);
    }

    @Override
    public void onSessionDisconnect(SessionDisconnectEvent event) {

        String botName = event.getJDA().getSelfUser().getName();
        LOG.info("{}'s Discord session has been disconnected'.", botName);

    }

    @Override
    public void onSessionResume(SessionResumeEvent event) {

        String botName = event.getJDA().getSelfUser().getName();
        LOG.info("{}'s Discord session has been reconnected.", botName);
    }

    @Override
    public void onSessionInvalidate(SessionInvalidateEvent event) {

        String botName = event.getJDA().getSelfUser().getName();
        LOG.info("{}'s Discord session has been invalidated.", botName);
    }

    @Override
    public void onShutdown(ShutdownEvent event) {

        String botName = event.getJDA().getSelfUser().getName();
        LOG.info("{} has shutdown.", botName);
    }

    @Override
    public void onException(ExceptionEvent event) {

        String botName = event.getJDA().getSelfUser().getName();
        LOG.error("{} has thrown a JDA exception", botName, event.getCause());
    }
}
