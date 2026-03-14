package me.moirai.storyengine.core.application.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.list;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.application.usecase.discord.DiscordMessageData;
import me.moirai.storyengine.core.application.usecase.discord.DiscordUserDetails;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.adventure.AdventureLorebookEntry;
import me.moirai.storyengine.core.domain.adventure.AdventureService;
import me.moirai.storyengine.core.domain.port.TokenizerPort;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.ModelConfigurationRequest;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.ModelConfigurationRequestFixture;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
public class LorebookEnrichmentHelperImplTest {

    private static final String LOREBOOK_KEY = "lorebook";

    @Mock
    private TokenizerPort tokenizerPort;

    @Mock
    private AdventureService adventureService;

    @Mock
    private ChatMessageHelper chatMessageService;

    @InjectMocks
    private LorebookEnrichmentHelperImpl adapter;

    @Test
    void enrichContextWithLorebookForRpg_whenMessagesAreValid_andNormalMode_thenReturnContextWithProcessedPlayerEntries() {

        // Given
        String worldId = "WRLDID";
        ModelConfigurationRequest modelConfiguration = ModelConfigurationRequestFixture.gpt4Mini().build();
        List<DiscordMessageData> messageList = getMessageListForTesting();

        ArgumentCaptor<Map<String, Object>> contextCaptor = ArgumentCaptor.forClass(Map.class);

        stubLorebookEntriesByWords();

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
        String worldId = "WRLDID";
        ModelConfigurationRequest modelConfiguration = ModelConfigurationRequestFixture.gpt4Mini().build();
        List<DiscordMessageData> messageList = getMessageListForTesting();

        ArgumentCaptor<Map<String, Object>> contextCaptor = ArgumentCaptor.forClass(Map.class);

        stubLorebookEntriesByWords();
        stubLorebookEntriesByMention();
        stubLorebookEntriesByAuthor();

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

    private void stubLorebookEntriesByAuthor() {

        Adventure adventure = AdventureFixture.publicMultiplayerAdventure().build();
        AdventureLorebookEntry marcusCharacter = AdventureLorebookEntry.builder()
                .id("1")
                .name("Pyromancer")
                .regex("[Pp]iro[Mm]ancer")
                .description("The Pyromancer is a fire battlemage")
                .playerId("1")
                .isPlayerCharacter(true)
                .adventureId(adventure.getId())
                .build();

        AdventureLorebookEntry johnCharacter = AdventureLorebookEntry.builder()
                .id("2")
                .name("Lord of Doom")
                .regex("[Ll]ord [Oo] [Dd]oom")
                .description("The Lord of Doom is a very powerful ogre")
                .playerId("2")
                .isPlayerCharacter(true)
                .adventureId(adventure.getId())
                .build();

        when(adventureService.findLorebookEntryByPlayerDiscordId(anyString(), anyString()))
                .thenReturn(marcusCharacter)
                .thenReturn(johnCharacter)
                .thenReturn(marcusCharacter);
    }

    private void stubLorebookEntriesByMention() {

        Adventure adventure = AdventureFixture.publicMultiplayerAdventure().build();
        AdventureLorebookEntry marcusCharacter = AdventureLorebookEntry.builder()
                .id("1")
                .name("Pyromancer")
                .regex("[Pp]iro[Mm]ancer")
                .description("The Pyromancer is a fire battlemage")
                .playerId("1")
                .isPlayerCharacter(true)
                .adventureId(adventure.getId())
                .build();

        when(adventureService.findLorebookEntryByPlayerDiscordId(anyString(), anyString()))
                .thenReturn(marcusCharacter);

    }

    private void stubLorebookEntriesByWords() {

        Adventure adventure = AdventureFixture.publicMultiplayerAdventure().build();
        AdventureLorebookEntry swordOfFire = AdventureLorebookEntry.builder()
                .id("3")
                .name("Sword of Fire")
                .regex("[Ss]word [Oo]f [Ff]ire")
                .description("The Sword of Fire is a spectral sword that spits fire")
                .adventureId(adventure.getId())
                .build();

        AdventureLorebookEntry gloveOfArmageddon = AdventureLorebookEntry.builder()
                .id("4")
                .name("Glove of Armageddon")
                .regex("[Gg]love [Oo]f [Aa]rmageddon")
                .description("The Glove of Armageddon is a gauntlet that punches with the strength of three suns")
                .adventureId(adventure.getId())
                .build();

        AdventureLorebookEntry lordOfDoom = AdventureLorebookEntry.builder()
                .id("2")
                .name("Lord of Doom")
                .regex("[Ll]ord [Oo] [Dd]oom")
                .description("The Lord of Doom is a very powerful ogre")
                .playerId("2")
                .isPlayerCharacter(true)
                .adventureId(adventure.getId())
                .build();

        when(adventureService.findAllLorebookEntriesByRegex(anyString(), anyString()))
                .thenReturn(list(swordOfFire, gloveOfArmageddon, lordOfDoom));
    }
}
