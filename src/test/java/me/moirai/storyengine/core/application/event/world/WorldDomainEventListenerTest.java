package me.moirai.storyengine.core.application.event.world;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.core.domain.userdetails.UserDeletedEvent;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@ExtendWith(MockitoExtension.class)
public class WorldDomainEventListenerTest {

    private static final Long DELETED_USER_ID = UserFixture.NUMERIC_ID;
    private static final String FIRST_IMAGE_KEY = "worlds/first.png";
    private static final String SECOND_IMAGE_KEY = "worlds/second.png";
    private static final UUID FIRST_WORLD = UUID.fromString("857345aa-0000-0000-0000-000000000001");
    private static final UUID SECOND_WORLD = UUID.fromString("857345aa-0000-0000-0000-000000000002");

    @Mock
    private WorldRepository worldRepository;

    @Mock
    private StoragePort storagePort;

    @InjectMocks
    private WorldDomainEventListener listener;

    @Test
    void shouldDeleteEveryWorldOwnedByTheUserWhenTheUserIsDeleted() {

        // given
        when(worldRepository.findAllOwnedBy(DELETED_USER_ID))
                .thenReturn(List.of(worldWith(1L, FIRST_WORLD, null), worldWith(2L, SECOND_WORLD, null)));

        when(worldRepository.findAllInvolving(DELETED_USER_ID)).thenReturn(emptyList());

        // when
        listener.onUserDeleted(userDeletedEvent());

        // then
        verify(worldRepository).deleteByPublicId(FIRST_WORLD);
        verify(worldRepository).deleteByPublicId(SECOND_WORLD);
        verify(worldRepository, never()).save(any());
    }

    @Test
    void shouldDeleteTheImageOfEveryOwnedWorldWhenTheUserIsDeleted() {

        // given
        when(worldRepository.findAllOwnedBy(DELETED_USER_ID))
                .thenReturn(List.of(
                        worldWith(1L, FIRST_WORLD, FIRST_IMAGE_KEY),
                        worldWith(2L, SECOND_WORLD, SECOND_IMAGE_KEY)));

        when(worldRepository.findAllInvolving(DELETED_USER_ID)).thenReturn(emptyList());

        // when
        listener.onUserDeleted(userDeletedEvent());

        // then
        verify(storagePort).delete(FIRST_IMAGE_KEY);
        verify(storagePort).delete(SECOND_IMAGE_KEY);
    }

    @Test
    void shouldNotCallStorageWhenTheOwnedWorldHasNoImage() {

        // given
        when(worldRepository.findAllOwnedBy(DELETED_USER_ID))
                .thenReturn(List.of(worldWith(1L, FIRST_WORLD, null)));

        when(worldRepository.findAllInvolving(DELETED_USER_ID)).thenReturn(emptyList());

        // when
        listener.onUserDeleted(userDeletedEvent());

        // then
        verify(worldRepository).deleteByPublicId(FIRST_WORLD);
        verify(storagePort, never()).delete(any());
    }

    @Test
    void shouldPropagateTheFailureWhenAnImageDeleteFails() {

        // given
        when(worldRepository.findAllOwnedBy(DELETED_USER_ID))
                .thenReturn(List.of(worldWith(1L, FIRST_WORLD, FIRST_IMAGE_KEY)));

        doThrow(new RuntimeException("storage down")).when(storagePort).delete(FIRST_IMAGE_KEY);

        // then
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> listener.onUserDeleted(userDeletedEvent()))
                .withMessage("storage down");
    }

    @Test
    void shouldRevokePermissionsWhenTheUserOnlyHasAccess() {

        // given
        var world = worldWith(1L, FIRST_WORLD, null);
        world.grant(new Permission(DELETED_USER_ID, PermissionLevel.READ));

        when(worldRepository.findAllOwnedBy(DELETED_USER_ID)).thenReturn(emptyList());
        when(worldRepository.findAllInvolving(DELETED_USER_ID)).thenReturn(List.of(world));

        // when
        listener.onUserDeleted(userDeletedEvent());

        // then
        verify(worldRepository).save(world);
        verify(worldRepository, never()).deleteByPublicId(any());
        verify(storagePort, never()).delete(any());

        assertThat(world.getPermissions())
                .extracting(Permission::userId)
                .doesNotContain(DELETED_USER_ID);
    }

    @Test
    void shouldNotRevokeOnAWorldThatWasJustDeletedWhenTheUserOwnsIt() {

        // given
        var owned = worldWith(1L, FIRST_WORLD, null);

        when(worldRepository.findAllOwnedBy(DELETED_USER_ID)).thenReturn(List.of(owned));
        when(worldRepository.findAllInvolving(DELETED_USER_ID)).thenReturn(List.of(owned));

        // when
        listener.onUserDeleted(userDeletedEvent());

        // then
        verify(worldRepository).deleteByPublicId(FIRST_WORLD);
        verify(worldRepository, never()).save(any());
    }

    @Test
    void shouldDoNothingWhenTheUserHasNoWorlds() {

        // given
        when(worldRepository.findAllOwnedBy(DELETED_USER_ID)).thenReturn(emptyList());
        when(worldRepository.findAllInvolving(DELETED_USER_ID)).thenReturn(emptyList());

        // when
        listener.onUserDeleted(userDeletedEvent());

        // then
        verify(worldRepository, never()).deleteByPublicId(any());
        verify(worldRepository, never()).save(any());
        verify(storagePort, never()).delete(any());
    }

    private World worldWith(Long id, UUID publicId, String imageKey) {

        var world = WorldFixture.privateWorldWithId();
        ReflectionTestUtils.setField(world, "id", id);
        ReflectionTestUtils.setField(world, "publicId", publicId);
        ReflectionTestUtils.setField(world, "imageKey", imageKey);

        return world;
    }

    private UserDeletedEvent userDeletedEvent() {

        var user = UserFixture.playerWithId();
        user.communicateUserDeleted();

        return (UserDeletedEvent) user.drainEvents().getFirst();
    }
}
