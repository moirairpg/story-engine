package me.moirai.storyengine.core.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.cqs.command.CommandRunner;
import me.moirai.storyengine.common.cqs.command.CommandRunnerImpl;

@Configuration
public class CommandRunnerConfig {

    private static final Logger LOG = LoggerFactory.getLogger(CommandRunnerConfig.class);

    private static final String REGISTERED_COMMAND_HANDLERS = "{} command handlers have been registered";

    @Bean
    CommandRunner commandRunner(List<AbstractCommandHandler<?, ?>> handlers) {

        CommandRunner runner = new CommandRunnerImpl();
        handlers.forEach(runner::registerHandler);

        LOG.info(REGISTERED_COMMAND_HANDLERS, handlers.size());

        return runner;
    }
}
