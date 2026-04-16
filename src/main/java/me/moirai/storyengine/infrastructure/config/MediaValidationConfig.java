package me.moirai.storyengine.infrastructure.config;

import org.apache.tika.Tika;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MediaValidationConfig {

    @Bean
    Tika tika() {
        return new Tika();
    }
}
