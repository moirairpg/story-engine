package me.moirai.storyengine.infrastructure.config;

import org.mockito.Mock;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import me.moirai.storyengine.common.security.authentication.MoiraiUserDetailsService;
import me.moirai.storyengine.common.security.authentication.SessionRenewalService;
import me.moirai.storyengine.common.security.authentication.filter.AuthenticationFilter;
import me.moirai.storyengine.common.security.authentication.filter.CrossSiteRequestFilter;

@TestConfiguration
@EnableWebSecurity
public class AuthenticationSecurityConfigTest {

    private static final String[] IGNORED_PATHS = { "/auth/code" };
    private static final String[] ALLOWED_ORIGINS = { "http://localhost" };
    private static final String FAIL_PATH = "/fail";
    private static final String LOGOUT_PATH = "/logout";

    @Mock
    private MoiraiUserDetailsService userDetailsService;

    @Mock
    private SessionRenewalService sessionRenewalService;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(csrf -> csrf.disable())
                .addFilterBefore(new CrossSiteRequestFilter(ALLOWED_ORIGINS), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new AuthenticationFilter(
                        IGNORED_PATHS, FAIL_PATH, LOGOUT_PATH,
                        userDetailsService, sessionRenewalService),
                        CrossSiteRequestFilter.class)
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(IGNORED_PATHS).permitAll()
                        .anyRequest().authenticated())
                .anonymous(anonymous -> anonymous.disable())
                .build();
    }
}
