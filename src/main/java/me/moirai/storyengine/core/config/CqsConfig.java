package me.moirai.storyengine.core.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.cqs.command.CommandRunner;
import me.moirai.storyengine.common.cqs.command.CommandRunnerImpl;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.cqs.query.QueryRunner;
import me.moirai.storyengine.common.cqs.query.QueryRunnerImpl;

@EnableAsync
@Configuration
public class CqsConfig {

    private static final Logger LOG = LoggerFactory.getLogger(CqsConfig.class);
    private static final String REGISTERED_QUERY_HANDLERS = "{} query handlers have been registered";
    private static final String REGISTERED_COMMAND_HANDLERS = "{} command handlers have been registered";

    @Bean
    QueryRunner queryRunner(List<AbstractQueryHandler<?, ?>> handlers) {

        var runner = new QueryRunnerImpl();
        handlers.forEach(runner::registerHandler);

        LOG.info(REGISTERED_QUERY_HANDLERS, handlers.size());

        return runner;
    }

    @Bean
    CommandRunner commandRunner(List<AbstractCommandHandler<?, ?>> handlers) {

        var runner = new CommandRunnerImpl();
        handlers.forEach(runner::registerHandler);

        LOG.info(REGISTERED_COMMAND_HANDLERS, handlers.size());

        return runner;
    }
}
