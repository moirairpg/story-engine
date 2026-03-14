package me.moirai.storyengine.infrastructure.security.authorization.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import me.moirai.storyengine.infrastructure.security.authorization.MoiraiMethodSecurityExpressionHandler;
import me.moirai.storyengine.infrastructure.security.authorization.MoiraiSecurityExpressions;

@Configuration
@EnableMethodSecurity
public class AuthorizationConfig {

    private final MoiraiSecurityExpressions assetFactory;

    public AuthorizationConfig(MoiraiSecurityExpressions assetFactory) {
        this.assetFactory = assetFactory;
    }

    @Bean
    MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        return new MoiraiMethodSecurityExpressionHandler(() -> assetFactory);
    }
}
