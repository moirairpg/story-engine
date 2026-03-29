package me.moirai.storyengine;

import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;
import org.testcontainers.postgresql.PostgreSQLContainer;

import io.qdrant.client.QdrantClient;
import me.moirai.storyengine.common.testutil.DbTestHelper;
import me.moirai.storyengine.core.port.outbound.discord.DiscordAuthenticationPort;
import me.moirai.storyengine.core.port.outbound.generation.EmbeddingPort;
import me.moirai.storyengine.core.port.outbound.generation.TextCompletionPort;
import me.moirai.storyengine.core.port.outbound.generation.TextModerationPort;
import me.moirai.storyengine.core.port.outbound.vectorsearch.VectorSearchPort;
import me.moirai.storyengine.infrastructure.config.DiscordApiConfig;
import me.moirai.storyengine.infrastructure.config.OpenAiApiConfig;
import me.moirai.storyengine.infrastructure.config.QdrantConfig;

@ActiveProfiles({ "test", "prompts" })
@SpringBootTest(classes = MoiraiApplication.class)
public abstract class AbstractIntegrationTest {

    @Autowired
    private DbTestHelper dbTestHelper;

    @Mock
    private DiscordAuthenticationPort discordAuthenticationPort;

    @MockitoBean
    private TextCompletionPort textCompletionPort;

    @MockitoBean
    private TextModerationPort textModerationPort;

    @MockitoBean
    private EmbeddingPort embeddingPort;

    @MockitoBean
    private VectorSearchPort vectorSearchPort;

    @MockitoBean
    private OpenAiApiConfig openAiApiConfig;

    @MockitoBean
    private DiscordApiConfig discordApiConfig;

    @MockitoBean
    private QdrantConfig qdrantConfig;

    @MockitoBean
    private QdrantClient qdrantClient;

    @MockitoBean
    private RestClient discordClient;

    @MockitoBean
    private RestClient openAiClient;

    private static final String POSTGRES_IMAGE_NAME = "postgres:18-alpine";

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

    protected <T> T insert(Object value, Class<T> type) {
        return dbTestHelper.insert(value, type);
    }

    protected <T> T clearAndInsert(Object value, Class<T> type) {
        return dbTestHelper.clearAndInsert(value, type);
    }

    protected <T> void update(Object value, Long primaryKeyValue, Class<T> type) {
        dbTestHelper.update(value, primaryKeyValue, type);
    }

    protected <T> void clear(Class<T> type) {
        dbTestHelper.clear(type);
    }

    protected void clearDatabase() {
        dbTestHelper.clearDatabase();
    }
}
