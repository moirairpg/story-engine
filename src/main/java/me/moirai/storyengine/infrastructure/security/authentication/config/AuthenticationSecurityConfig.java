package me.moirai.storyengine.infrastructure.security.authentication.config;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.config.web.server.SecurityWebFiltersOrder.AUTHENTICATION;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.CorsSpec;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.config.web.server.ServerHttpSecurity.FormLoginSpec;
import org.springframework.security.config.web.server.ServerHttpSecurity.HttpBasicSpec;
import org.springframework.security.config.web.server.ServerHttpSecurity.LogoutSpec;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;

import me.moirai.storyengine.infrastructure.security.authentication.MoiraiUserDetailsService;
import me.moirai.storyengine.infrastructure.security.authentication.filter.AuthenticationFilter;

@Configuration
@EnableWebFluxSecurity
public class AuthenticationSecurityConfig {

    private final String[] ignoredPaths;
    private final String authenticationFailedPath;
    private final String logoutPath;
    private final MoiraiUserDetailsService userDetailsService;

    public AuthenticationSecurityConfig(
            @Value("${moirai.security.ignored-paths}") String[] ignoredPaths,
            @Value("${moirai.security.redirect-path.fail}") String authenticationFailedPath,
            @Value("${moirai.security.redirect-path.logout}") String logoutPath,
            MoiraiUserDetailsService userDetailsService) {

        this.ignoredPaths = ignoredPaths;
        this.authenticationFailedPath = authenticationFailedPath;
        this.logoutPath = logoutPath;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    SecurityWebFilterChain configure(ServerHttpSecurity http) {

        HttpStatusServerEntryPoint unauthorizedEntryPoint = new HttpStatusServerEntryPoint(UNAUTHORIZED);
        return http.httpBasic(HttpBasicSpec::disable)
                .formLogin(FormLoginSpec::disable)
                .logout(LogoutSpec::disable)
                .addFilterBefore(new AuthenticationFilter(
                        ignoredPaths, authenticationFailedPath, logoutPath, userDetailsService), AUTHENTICATION)
                .authorizeExchange(exchanges -> exchanges.pathMatchers(ignoredPaths).permitAll())
                .authorizeExchange(exchanges -> exchanges.anyExchange().authenticated())
                .oauth2Login(withDefaults())
                .csrf(CsrfSpec::disable)
                .cors(CorsSpec::disable)
                .exceptionHandling(handler -> handler.authenticationEntryPoint(unauthorizedEntryPoint))
                .build();
    }
}
