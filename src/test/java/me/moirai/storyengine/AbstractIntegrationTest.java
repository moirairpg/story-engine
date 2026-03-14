package me.moirai.storyengine;

import org.mockito.Mock;
import org.springframework.boot.rsocket.context.RSocketServerBootstrap;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import me.moirai.storyengine.core.application.port.PersonaEnrichmentPort;
import me.moirai.storyengine.core.port.DiscordAuthenticationPort;
import me.moirai.storyengine.core.port.DiscordChannelPort;
import me.moirai.storyengine.core.port.StorySummarizationPort;
import me.moirai.storyengine.core.port.TextCompletionPort;
import me.moirai.storyengine.core.port.TextModerationPort;
import me.moirai.storyengine.infrastructure.config.JdaConfig;
import net.dv8tion.jda.api.JDA;

@ActiveProfiles({ "test", "prompts" })
@SpringBootTest(classes = MoiraiApplication.class)
public abstract class AbstractIntegrationTest {

    @Mock
    private DiscordAuthenticationPort discordAuthenticationPort;

    @MockBean
    private TextCompletionPort openAiPort;

    @MockBean
    private DiscordChannelPort discordChannelOperationsPort;

    @MockBean
    private PersonaEnrichmentPort inputEnrichmentService;

    @MockBean
    private StorySummarizationPort contextSummarizationService;

    @MockBean
    private JDA jda;

    @MockBean
    private TextModerationPort textModerationPort;

    @MockBean
    private JdaConfig jdaConfig;

    @MockBean
    private RSocketServerBootstrap rSocketServerBootstrap;

    private static final String POSTGRES_IMAGE_NAME = "postgres:15-alpine";

    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> container = new PostgreSQLContainer<>(POSTGRES_IMAGE_NAME)
            .withDatabaseName("moirai")
            .withUsername("moirai")
            .withPassword("moirai");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {

        container.start();

        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
    }
}
