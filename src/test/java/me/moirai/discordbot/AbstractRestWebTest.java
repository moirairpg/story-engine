package me.moirai.discordbot;

import static me.moirai.discordbot.infrastructure.security.authentication.MoiraiCookie.REFRESH_COOKIE;
import static me.moirai.discordbot.infrastructure.security.authentication.MoiraiCookie.SESSION_COOKIE;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.security.test.context.support.ReactorContextTestExecutionListener;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.web.reactive.server.WebTestClient;

import me.moirai.discordbot.common.usecases.UseCaseRunner;
import me.moirai.discordbot.infrastructure.security.authentication.MoiraiPrincipal;
import me.moirai.discordbot.infrastructure.security.authentication.MoiraiUserDetailsService;
import net.dv8tion.jda.api.JDA;
import reactor.core.publisher.Mono;

@WebFluxTest
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public abstract class AbstractRestWebTest {

    private static final TestContext CLEAR_TEST_CONTEXT = null;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @MockBean
    private JDA jda;

    @MockBean
    protected UseCaseRunner useCaseRunner;

    @MockBean
    protected ServerHttpSecurity serverHttpSecurity;

    @MockBean
    protected MoiraiUserDetailsService discordUserDetailsService;

    @Autowired
    protected WebTestClient webTestClient;

    private TestExecutionListener testExecutionListener = new ReactorContextTestExecutionListener();

    @BeforeEach
    public void before() throws Exception {

        UserDetails userDetails = MoiraiPrincipal.builder()
                .discordId("USRID")
                .email("user@email.com")
                .username("username")
                .build();

        webTestClient = webTestClient.mutate()
                .responseTimeout(Duration.ofMillis(3000000))
                .defaultCookie(SESSION_COOKIE.getName(), "COOKIE")
                .defaultCookie(REFRESH_COOKIE.getName(), "COOKIE")
                .build();

        when(discordUserDetailsService.findByUsername(anyString())).thenReturn(Mono.just(userDetails));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        TestSecurityContextHolder.setContext(securityContext);
        TestSecurityContextHolder.setAuthentication(authentication);
        // ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext));
        // ReactiveSecurityContextHolder.withAuthentication(authentication);

        testExecutionListener.beforeTestMethod(CLEAR_TEST_CONTEXT);
    }

    @AfterEach
    public void after() throws Exception {

        testExecutionListener.afterTestMethod(CLEAR_TEST_CONTEXT);
    }
}
