package me.moirai.storyengine.infrastructure.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import liquibase.integration.spring.SpringLiquibase;

@Configuration
public class LiquibaseConfig {

    private final DataSource dataSource;
    private final String defaultSchema;
    private final String changeLogPath;

    public LiquibaseConfig(
            @Value("${spring.liquibase.defaultSchema}") String defaultSchema,
            @Value("${spring.liquibase.change-log}") String changeLogPath,
            DataSource dataSource) {

        this.defaultSchema = defaultSchema;
        this.changeLogPath = changeLogPath;
        this.dataSource = dataSource;
    }

    @Bean
    public SpringLiquibase liquibase() {

        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(changeLogPath);
        liquibase.setDefaultSchema(defaultSchema);

        return liquibase;
    }
}
