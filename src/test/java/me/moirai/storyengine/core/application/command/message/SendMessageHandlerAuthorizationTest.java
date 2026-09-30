package me.moirai.storyengine.core.application.command.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.common.exception.AccessDeniedException;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authentication.MoiraiSecurityContext;
import me.moirai.storyengine.common.security.authorization.AuthorizationAspect;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.security.authorization.AuthorizationService;
import me.moirai.storyengine.core.port.inbound.message.SendMessage;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.message.MessageRepository;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@SpringJUnitConfig
class SendMessageHandlerAuthorizationTest {

    private static final UUID ADVENTURE_ID = UUID.fromString("00000000-0000-0000-0000-0000000000aa");

    @Configuration
    @EnableAspectJAutoProxy
    static class TestConfig {

        @Bean
        AuthorizationService authorizationService() {
            return mock(AuthorizationService.class);
        }

        @Bean
        AuthorizationAspect authorizationAspect(AuthorizationService authorizationService) {
            return new AuthorizationAspect(authorizationService);
        }

        @Bean
        AdventureRepository adventureRepository() {
            return mock(AdventureRepository.class);
        }

        @Bean
        SendMessageHandler sendMessageHandler(AdventureRepository adventureRepository) {
            return new SendMessageHandler(
                    adventureRepository,
                    mock(MessageRepository.class),
                    mock(UserRepository.class),
                    mock(ApplicationEventPublisher.class));
        }
    }

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private AdventureRepository adventureRepository;

    @Autowired
    private SendMessageHandler sendMessageHandler;

    @BeforeEach
    void setUp() {
        reset(authorizationService, adventureRepository);
        MoiraiSecurityContext.set(principal());
    }

    @AfterEach
    void tearDown() {
        MoiraiSecurityContext.clear();
    }

    @Test
    void shouldAuthorizePlayAdventureUsingTheAdventureIdFromTheCommand() {

        // given
        when(adventureRepository.findByPublicId(ADVENTURE_ID)).thenReturn(Optional.empty());

        // when
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> sendMessageHandler.handle(command()));

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(authorizationService)
                .authorize(eq(AuthorizationOperation.PLAY_ADVENTURE), captor.capture(), any());

        assertThat(captor.getValue()).containsEntry("adventureId", ADVENTURE_ID);
    }

    @Test
    void shouldNotRunTheHandlerBodyWhenAuthorizationIsDenied() {

        // given
        doThrow(new AccessDeniedException("denied"))
                .when(authorizationService).authorize(any(), anyMap(), any());

        // when
        assertThatExceptionOfType(AccessDeniedException.class)
                .isThrownBy(() -> sendMessageHandler.handle(command()));

        // then
        verifyNoInteractions(adventureRepository);
    }

    @Test
    void shouldResolveThePrincipalFromMoiraiSecurityContext() {

        // given
        when(adventureRepository.findByPublicId(ADVENTURE_ID)).thenReturn(Optional.empty());

        // when
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> sendMessageHandler.handle(command()));

        // then
        var principalCaptor = ArgumentCaptor.forClass(MoiraiPrincipal.class);
        verify(authorizationService).authorize(any(), anyMap(), principalCaptor.capture());

        assertThat(principalCaptor.getValue().username()).isEqualTo("caller");
    }

    private SendMessage command() {
        return new SendMessage(ADVENTURE_ID, "content", "caller", true);
    }

    private MoiraiPrincipal principal() {
        return new MoiraiPrincipal(
                UUID.randomUUID(), 1L, "caller", "token", "refresh", Role.PLAYER, null);
    }
}
