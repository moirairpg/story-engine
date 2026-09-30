package me.moirai.storyengine.core.application.query.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.core.port.inbound.AssetMember;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureMembers;
import me.moirai.storyengine.core.port.outbound.adventure.AdventurePermissionReader;

@ExtendWith(MockitoExtension.class)
public class GetAdventureMembersHandlerTest {

    private static final UUID ADVENTURE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Mock
    private AdventurePermissionReader reader;

    @InjectMocks
    private GetAdventureMembersHandler handler;

    @Test
    void shouldReturnTheMembersTheReaderProvides() {

        // given
        var members = List.of(new AssetMember(OWNER_ID, "owner", "The Owner", PermissionLevel.OWNER));
        when(reader.getAllByAdventurePublicId(any())).thenReturn(members);

        // when
        var result = handler.execute(new GetAdventureMembers(ADVENTURE_ID));

        // then
        assertThat(result).isEqualTo(members);
    }

    @Test
    void shouldReturnAnEmptyListWhenTheReaderReturnsNothing() {

        // given
        when(reader.getAllByAdventurePublicId(any())).thenReturn(List.of());

        // when
        var result = handler.execute(new GetAdventureMembers(ADVENTURE_ID));

        // then
        assertThat(result).isEmpty();
    }
}
