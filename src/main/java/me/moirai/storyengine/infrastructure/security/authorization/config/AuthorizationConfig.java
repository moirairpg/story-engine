package me.moirai.storyengine.infrastructure.security.authorization.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import me.moirai.storyengine.infrastructure.security.authorization.AuthorizationInterceptor;

@Configuration
public class AuthorizationConfig implements WebMvcConfigurer {

    private final AuthorizationInterceptor authorizationInterceptor;

    public AuthorizationConfig(AuthorizationInterceptor authorizationInterceptor) {
        this.authorizationInterceptor = authorizationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authorizationInterceptor);
    }
}
