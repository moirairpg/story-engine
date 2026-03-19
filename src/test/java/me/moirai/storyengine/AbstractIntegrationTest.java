package me.moirai.storyengine;

import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;
import org.testcontainers.postgresql.PostgreSQLContainer;

import me.moirai.storyengine.core.port.outbound.discord.DiscordAuthenticationPort;
import me.moirai.storyengine.core.port.outbound.discord.DiscordChannelPort;
import me.moirai.storyengine.core.port.outbound.generation.PersonaEnrichmentPort;
import me.moirai.storyengine.core.port.outbound.generation.StorySummarizationPort;
import me.moirai.storyengine.core.port.outbound.generation.TextCompletionPort;
import me.moirai.storyengine.core.port.outbound.generation.TextModerationPort;
import me.moirai.storyengine.infrastructure.config.DiscordApiConfig;
import me.moirai.storyengine.infrastructure.config.JdaConfig;
import me.moirai.storyengine.infrastructure.config.OpenAiApiConfig;
import net.dv8tion.jda.api.JDA;

@ActiveProfiles({ "test", "prompts" })
@SpringBootTest(classes = MoiraiApplication.class)
public abstract class AbstractIntegrationTest {

    @Mock
    private DiscordAuthenticationPort discordAuthenticationPort;

    @MockitoBean
    private DiscordChannelPort discordChannelOperationsPort;

    @MockitoBean
    private PersonaEnrichmentPort inputEnrichmentService;

    @MockitoBean
    private StorySummarizationPort contextSummarizationService;

    @MockitoBean
    private JDA jda;

    @MockitoBean
    private TextCompletionPort textCompletionPort;

    @MockitoBean
    private TextModerationPort textModerationPort;

    @MockitoBean
    private OpenAiApiConfig openAiApiConfig;

    @MockitoBean
    private DiscordApiConfig discordApiConfig;

    @MockitoBean
    private RestClient discordClient;

    @MockitoBean
    private RestClient openAiClient;

    @MockitoBean
    private JdaConfig jdaConfig;

    private static final String POSTGRES_IMAGE_NAME = "postgres:15-alpine";

    @SuppressWarnings("resource")
    static PostgreSQLContainer container = new PostgreSQLContainer(POSTGRES_IMAGE_NAME)
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
