package me.moirai.storyengine.core.application.query.world;

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
import me.moirai.storyengine.core.port.inbound.world.GetWorldMembers;
import me.moirai.storyengine.core.port.outbound.world.WorldPermissionReader;

@ExtendWith(MockitoExtension.class)
public class GetWorldMembersHandlerTest {

    private static final UUID WORLD_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Mock
    private WorldPermissionReader reader;

    @InjectMocks
    private GetWorldMembersHandler handler;

    @Test
    void shouldReturnTheMembersTheReaderProvides() {

        // given
        var members = List.of(new AssetMember(OWNER_ID, "owner", "The Owner", PermissionLevel.OWNER));
        when(reader.getAllByWorldPublicId(any())).thenReturn(members);

        // when
        var result = handler.execute(new GetWorldMembers(WORLD_ID));

        // then
        assertThat(result).isEqualTo(members);
    }

    @Test
    void shouldReturnAnEmptyListWhenTheReaderReturnsNothing() {

        // given
        when(reader.getAllByWorldPublicId(any())).thenReturn(List.of());

        // when
        var result = handler.execute(new GetWorldMembers(WORLD_ID));

        // then
        assertThat(result).isEmpty();
    }
}
