package me.moirai.storyengine.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import me.moirai.storyengine.common.security.authentication.MoiraiUserDetailsService;
import me.moirai.storyengine.common.security.authentication.filter.AuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class AuthenticationSecurityConfig {

    private final String[] unsecuredPaths;
    private final String authenticationFailedPath;
    private final String logoutPath;
    private final MoiraiUserDetailsService userDetailsService;

    public AuthenticationSecurityConfig(
            @Value("${moirai.security.ignored-paths}") String[] unsecuredPaths,
            @Value("${moirai.security.redirect-path.fail}") String authenticationFailedPath,
            @Value("${moirai.security.redirect-path.logout}") String logoutPath,
            MoiraiUserDetailsService userDetailsService) {

        this.unsecuredPaths = unsecuredPaths;
        this.authenticationFailedPath = authenticationFailedPath;
        this.logoutPath = logoutPath;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    SecurityFilterChain configure(HttpSecurity http) throws Exception {

        return http
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .addFilterBefore(new AuthenticationFilter(
                        unsecuredPaths, authenticationFailedPath, logoutPath, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(unsecuredPaths).permitAll()
                        .anyRequest().authenticated())
                // .oauth2Login(withDefaults())
                .anonymous(anonymous -> anonymous.disable())
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .exceptionHandling(handler -> handler
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .build();
    }
}
