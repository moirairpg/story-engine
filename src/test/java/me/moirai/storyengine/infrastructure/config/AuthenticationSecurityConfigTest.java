package me.moirai.storyengine.infrastructure.config;

import org.mockito.Mock;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import me.moirai.storyengine.common.security.authentication.MoiraiUserDetailsService;
import me.moirai.storyengine.common.security.authentication.filter.AuthenticationFilter;

@TestConfiguration
@EnableWebSecurity
public class AuthenticationSecurityConfigTest {

    private static final String[] IGNORED_PATHS = { "/auth/code" };
    private static final String FAIL_PATH = "/fail";
    private static final String LOGOUT_PATH = "/logout";

    @Mock
    private MoiraiUserDetailsService userDetailsService;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .addFilterBefore(new AuthenticationFilter(
                        IGNORED_PATHS, FAIL_PATH, LOGOUT_PATH, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(IGNORED_PATHS).permitAll()
                        .anyRequest().authenticated())
                .csrf(csrf -> csrf.disable())
                .anonymous(anonymous -> anonymous.disable())
                .build();
    }
}
