package me.moirai.storyengine.infrastructure.outbound.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.list;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntry;
import me.moirai.storyengine.core.port.inbound.discord.DiscordMessageData;
import me.moirai.storyengine.core.port.inbound.discord.DiscordUserDetails;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.generation.ChatMessagePort;
import me.moirai.storyengine.core.port.outbound.generation.ModelConfigurationRequest;
import me.moirai.storyengine.core.port.outbound.generation.TokenizerPort;
import me.moirai.storyengine.infrastructure.outbound.adapter.generation.LorebookEnrichmentAdapter;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.ModelConfigurationRequestFixture;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
public class LorebookEnrichmentAdapterTest {

    private static final String LOREBOOK_KEY = "lorebook";

    @Mock
    private TokenizerPort tokenizerPort;

    @Mock
    private AdventureRepository adventureRepository;

    @Mock
    private ChatMessagePort chatMessageService;

    @InjectMocks
    private LorebookEnrichmentAdapter adapter;

    @Test
    void enrichContextWithLorebookForRpg_whenMessagesAreValid_andNormalMode_thenReturnContextWithProcessedPlayerEntries() {

        // Given
        UUID worldId = AdventureFixture.PUBLIC_ID;
        ModelConfigurationRequest modelConfiguration = ModelConfigurationRequestFixture.gpt4Mini().build();
        List<DiscordMessageData> messageList = getMessageListForTesting();

        ArgumentCaptor<Map<String, Object>> contextCaptor = ArgumentCaptor.forClass(Map.class);

        Adventure baseAdventure = AdventureFixture.publicMultiplayerAdventure().build();
        Adventure adventure = spy(baseAdventure);

        List<AdventureLorebookEntry> lorebookEntries = buildLorebookEntriesForWords(baseAdventure);

        doReturn(lorebookEntries).when(adventure).getLorebookEntriesByRegex(anyString());

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));

        when(tokenizerPort.getTokenCountFrom(anyString()))
                .thenReturn(5)
                .thenReturn(10)
                .thenReturn(5)
                .thenReturn(10)
                .thenReturn(5)
                .thenReturn(10)
                .thenReturn(5)
                .thenReturn(10)
                .thenReturn(5)
                .thenReturn(10);

        when(chatMessageService.addMessagesToContext(contextCaptor.capture(), anyInt()))
                .thenReturn(new HashMap<>());

        // When
        adapter.enrichContextWithLorebook(messageList, worldId, modelConfiguration);

        // Then
        Map<String, Object> enrichedContext = contextCaptor.getValue();
        assertThat(enrichedContext).isNotNull()
                .isNotEmpty()
                .containsKey(LOREBOOK_KEY);

        String[] lorebook = ((String) enrichedContext.get(LOREBOOK_KEY)).split("\n");
        assertThat(lorebook).isNotNull()
                .isNotEmpty()
                .hasSize(3);
    }

    @Test
    void enrichContextWithLorebookForRpg_whenMessagesAreValid_andRpgMode_thenReturnContextWithProcessedPlayerEntries() {

        // Given
        UUID worldId = AdventureFixture.PUBLIC_ID;
        ModelConfigurationRequest modelConfiguration = ModelConfigurationRequestFixture.gpt4Mini().build();
        List<DiscordMessageData> messageList = getMessageListForTesting();

        ArgumentCaptor<Map<String, Object>> contextCaptor = ArgumentCaptor.forClass(Map.class);

        Adventure baseAdventure = AdventureFixture.publicMultiplayerAdventure().build();
        Adventure adventure = spy(baseAdventure);

        AdventureLorebookEntry marcusCharacter = buildMarcusCharacter(baseAdventure);
        AdventureLorebookEntry johnCharacter = buildJohnCharacter(baseAdventure);
        List<AdventureLorebookEntry> lorebookEntries = buildLorebookEntriesForWords(baseAdventure);

        doReturn(lorebookEntries).when(adventure).getLorebookEntriesByRegex(anyString());
        doReturn(Optional.of(marcusCharacter))
                .doReturn(Optional.of(johnCharacter))
                .doReturn(Optional.of(marcusCharacter))
                .when(adventure).getLorebookEntryByPlayerId(anyString());

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));

        when(tokenizerPort.getTokenCountFrom(anyString()))
                .thenReturn(5)
                .thenReturn(10)
                .thenReturn(5)
                .thenReturn(10)
                .thenReturn(5)
                .thenReturn(10)
                .thenReturn(5)
                .thenReturn(10)
                .thenReturn(5)
                .thenReturn(10);

        when(chatMessageService.addMessagesToContext(contextCaptor.capture(), anyInt()))
                .thenReturn(new HashMap<>());

        // When
        adapter.enrichContextWithLorebookForRpg(messageList, worldId, modelConfiguration);

        // Then
        Map<String, Object> enrichedContext = contextCaptor.getValue();
        assertThat(enrichedContext).isNotNull()
                .isNotEmpty()
                .containsKey(LOREBOOK_KEY);

        String[] lorebook = ((String) enrichedContext.get(LOREBOOK_KEY)).split("\n");
        assertThat(lorebook).isNotNull()
                .isNotEmpty()
                .hasSize(4);
    }

    private List<DiscordMessageData> getMessageListForTesting() {

        DiscordUserDetails marcus = DiscordUserDetails.builder()
                .id("1")
                .mention("<@1>")
                .nickname("Little Marcus")
                .username("Marcus")
                .build();

        DiscordUserDetails john = DiscordUserDetails.builder()
                .id("2")
                .mention("<@2>")
                .nickname("JoeJoe")
                .username("John")
                .build();

        DiscordMessageData firstMessage = DiscordMessageData.builder()
                .id("1")
                .content("Little Marcus says: I pull the Sword of Fire and charge against the Lord of Doom.")
                .author(marcus)
                .build();

        DiscordMessageData secondMessage = DiscordMessageData.builder()
                .id("2")
                .content("JoeJoe says: I deflect Little Marcus's attack and attack back with my Glove of Armageddon.")
                .author(john)
                .mentionedUsers(list(marcus))
                .build();

        DiscordMessageData thirdMessage = DiscordMessageData.builder()
                .id("3")
                .content("Little Marcus says: I cast a ball of fire and deal fire damage.")
                .author(marcus)
                .build();

        return list(firstMessage, secondMessage, thirdMessage);
    }

    private AdventureLorebookEntry buildMarcusCharacter(Adventure adventure) {

        AdventureLorebookEntry entry = AdventureLorebookEntry.builder()
                .name("Pyromancer")
                .regex("[Pp]iro[Mm]ancer")
                .description("The Pyromancer is a fire battlemage")
                .playerId("1")
                .isPlayerCharacter(true)
                .build();

        ReflectionTestUtils.setField(entry, "publicId", UUID.fromString("e01b12a5-578e-4eb6-951f-0cc81cafd4fb"));
        ReflectionTestUtils.setField(entry, "id", 4L);

        return entry;
    }

    private AdventureLorebookEntry buildJohnCharacter(Adventure adventure) {

        var lordOfDoomId = UUID.fromString("9af8053c-b65d-468d-a1ad-5348d9b64105");
        AdventureLorebookEntry lordOfDoom = AdventureLorebookEntry.builder()
                .name("Lord of Doom")
                .regex("[Ll]ord [Oo] [Dd]oom")
                .description("The Lord of Doom is a very powerful ogre")
                .playerId("2")
                .isPlayerCharacter(true)
                .build();

        ReflectionTestUtils.setField(lordOfDoom, "id", 3L);
        ReflectionTestUtils.setField(lordOfDoom, "publicId", lordOfDoomId);

        return lordOfDoom;
    }

    private List<AdventureLorebookEntry> buildLorebookEntriesForWords(Adventure adventure) {

        var swordOfFireId = UUID.fromString("a78d57ee-ec5e-4f54-ace7-e4d7959d01e9");
        AdventureLorebookEntry swordOfFire = AdventureLorebookEntry.builder()
                .name("Sword of Fire")
                .regex("[Ss]word [Oo]f [Ff]ire")
                .description("The Sword of Fire is a spectral sword that spits fire`")
                .build();

        ReflectionTestUtils.setField(swordOfFire, "id", 1L);
        ReflectionTestUtils.setField(swordOfFire, "publicId", swordOfFireId);

        var gloveOfArmageddonId = UUID.fromString("a7628cc2-a98e-486f-ac29-9bfbe4ce36fc");
        AdventureLorebookEntry gloveOfArmageddon = AdventureLorebookEntry.builder()
                .name("Glove of Armageddon")
                .regex("[Gg]love [Oo]f [Aa]rmageddon")
                .description("The Glove of Armageddon is a gauntlet that punches with the strength of three suns")
                .build();

        ReflectionTestUtils.setField(gloveOfArmageddon, "id", 2L);
        ReflectionTestUtils.setField(gloveOfArmageddon, "publicId", gloveOfArmageddonId);

        var lordOfDoomId = UUID.fromString("9af8053c-b65d-468d-a1ad-5348d9b64105");
        AdventureLorebookEntry lordOfDoom = AdventureLorebookEntry.builder()
                .name("Lord of Doom")
                .regex("[Ll]ord [Oo] [Dd]oom")
                .description("The Lord of Doom is a very powerful ogre")
                .playerId("2")
                .isPlayerCharacter(true)
                .build();

        ReflectionTestUtils.setField(lordOfDoom, "id", 3L);
        ReflectionTestUtils.setField(lordOfDoom, "publicId", lordOfDoomId);

        return list(swordOfFire, gloveOfArmageddon, lordOfDoom);
    }
}
