package me.moirai.discordbot.infrastructure.security.authentication.config;

import static org.springframework.security.config.web.server.SecurityWebFiltersOrder.AUTHENTICATION;

import org.mockito.Mock;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.web.server.SecurityWebFilterChain;

import me.moirai.discordbot.infrastructure.security.authentication.MoiraiUserDetailsService;
import me.moirai.discordbot.infrastructure.security.authentication.filter.AuthenticationFilter;

@TestConfiguration
@EnableWebFluxSecurity
public class AuthenticationSecurityConfigTest {

    private static final String[] IGNORED_PATHS = { "/auth/code" };
    private static final String FAIL_PATH = "/fail";
    private static final String LOGOUT_PATH = "/logout";

    @Mock
    private MoiraiUserDetailsService userDetailsService;

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        return http.authorizeExchange(exchange -> exchange.anyExchange().permitAll())
                .addFilterBefore(new AuthenticationFilter(
                        IGNORED_PATHS, FAIL_PATH, LOGOUT_PATH, userDetailsService), AUTHENTICATION)
                .authorizeExchange(exchanges -> exchanges.pathMatchers(IGNORED_PATHS).permitAll())
                .authorizeExchange(exchanges -> exchanges.anyExchange().authenticated())
                .csrf(CsrfSpec::disable)
                .build();
    }
}
