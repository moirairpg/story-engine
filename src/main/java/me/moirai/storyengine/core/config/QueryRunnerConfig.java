package me.moirai.storyengine.core.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.cqs.query.QueryRunner;
import me.moirai.storyengine.common.cqs.query.QueryRunnerImpl;

@Configuration
public class QueryRunnerConfig {

    private static final Logger LOG = LoggerFactory.getLogger(QueryRunnerConfig.class);

    private static final String REGISTERED_COMMAND_HANDLERS = "{} query handlers have been registered";

    @Bean
    QueryRunner queryRunner(List<AbstractQueryHandler<?, ?>> handlers) {

        QueryRunner runner = new QueryRunnerImpl();
        handlers.forEach(runner::registerHandler);

        LOG.info(REGISTERED_COMMAND_HANDLERS, handlers.size());

        return runner;
    }
}
