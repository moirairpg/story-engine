package me.moirai.storyengine.common.security.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import me.moirai.storyengine.common.annotation.Authorize;
import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.common.exception.AccessDeniedException;
import me.moirai.storyengine.common.exception.AuthenticationFailedException;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authentication.MoiraiSecurityContext;

@SpringJUnitConfig
class AuthorizationAspectTest {

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
        MethodAnnotatedBean methodAnnotatedBean() {
            return new MethodAnnotatedBean();
        }

        @Bean
        TypeAnnotatedHandler typeAnnotatedHandler() {
            return new TypeAnnotatedHandler();
        }

        @Bean
        BothLevelsBean bothLevelsBean() {
            return new BothLevelsBean();
        }

        @Bean
        UnannotatedBean unannotatedBean() {
            return new UnannotatedBean();
        }
    }

    record TestCommand(UUID adventureId) {
    }

    static class MethodAnnotatedBean {

        @Authorize(operation = AuthorizationOperation.UPDATE_ADVENTURE, fields = "#adventureId")
        public String update(UUID adventureId) {
            return "updated";
        }
    }

    abstract static class AbstractHandler {

        public String handle(TestCommand request) {
            return "handled";
        }
    }

    @Authorize(operation = AuthorizationOperation.PLAY_ADVENTURE, fields = "#request.adventureId")
    static class TypeAnnotatedHandler extends AbstractHandler {
    }

    @Authorize(operation = AuthorizationOperation.PLAY_ADVENTURE, fields = "#adventureId")
    static class BothLevelsBean {

        @Authorize(operation = AuthorizationOperation.DELETE_ADVENTURE, fields = "#adventureId")
        public String remove(UUID adventureId) {
            return "removed";
        }
    }

    static class UnannotatedBean {

        public String doThing() {
            return "done";
        }
    }

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private MethodAnnotatedBean methodAnnotatedBean;

    @Autowired
    private TypeAnnotatedHandler typeAnnotatedHandler;

    @Autowired
    private BothLevelsBean bothLevelsBean;

    @Autowired
    private UnannotatedBean unannotatedBean;

    @BeforeEach
    void setUp() {
        reset(authorizationService);
        MoiraiSecurityContext.set(principal());
    }

    @AfterEach
    void tearDown() {
        MoiraiSecurityContext.clear();
    }

    @Test
    void shouldAuthorizeUsingTheMethodLevelAnnotation() {

        // when
        var result = methodAnnotatedBean.update(ADVENTURE_ID);

        // then
        assertThat(result).isEqualTo("updated");
        verify(authorizationService)
                .authorize(eq(AuthorizationOperation.UPDATE_ADVENTURE), anyMap(), any());
    }

    @Test
    void shouldResolveTheAnnotatedFieldFromTheMethodArguments() {

        // when
        methodAnnotatedBean.update(ADVENTURE_ID);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<java.util.Map<String, Object>> captor = ArgumentCaptor.forClass(java.util.Map.class);
        verify(authorizationService).authorize(any(), captor.capture(), any());

        assertThat(captor.getValue()).containsEntry("adventureId", ADVENTURE_ID);
    }

    @Test
    void shouldAuthorizeUsingTheClassLevelAnnotationOnAnInheritedMethod() {

        // when
        var result = typeAnnotatedHandler.handle(new TestCommand(ADVENTURE_ID));

        // then
        assertThat(result).isEqualTo("handled");
        verify(authorizationService)
                .authorize(eq(AuthorizationOperation.PLAY_ADVENTURE), anyMap(), any());
    }

    @Test
    void shouldResolveNestedFieldsFromTheCommandOnAClassLevelAnnotation() {

        // when
        typeAnnotatedHandler.handle(new TestCommand(ADVENTURE_ID));

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<java.util.Map<String, Object>> captor = ArgumentCaptor.forClass(java.util.Map.class);
        verify(authorizationService).authorize(any(), captor.capture(), any());

        assertThat(captor.getValue()).containsEntry("adventureId", ADVENTURE_ID);
    }

    @Test
    void shouldAuthorizeOnceUsingTheMethodAnnotationWhenBothLevelsArePresent() {

        // when
        var result = bothLevelsBean.remove(ADVENTURE_ID);

        // then
        assertThat(result).isEqualTo("removed");
        verify(authorizationService)
                .authorize(eq(AuthorizationOperation.DELETE_ADVENTURE), anyMap(), any());
        verify(authorizationService, never())
                .authorize(eq(AuthorizationOperation.PLAY_ADVENTURE), anyMap(), any());
    }

    @Test
    void shouldResolveThePrincipalFromMoiraiSecurityContext() {

        // when
        methodAnnotatedBean.update(ADVENTURE_ID);

        // then
        var principalCaptor = ArgumentCaptor.forClass(MoiraiPrincipal.class);
        verify(authorizationService).authorize(any(), anyMap(), principalCaptor.capture());

        assertThat(principalCaptor.getValue().username()).isEqualTo("caller");
    }

    @Test
    void shouldThrowAuthenticationFailedWhenThereIsNoPrincipal() {

        // given
        MoiraiSecurityContext.clear();

        // then
        assertThatExceptionOfType(AuthenticationFailedException.class)
                .isThrownBy(() -> methodAnnotatedBean.update(ADVENTURE_ID));

        verify(authorizationService, never()).authorize(any(), anyMap(), any());
    }

    @Test
    void shouldPropagateDenialFromTheAuthorizationService() {

        // given
        doThrow(new AccessDeniedException("denied"))
                .when(authorizationService).authorize(any(), anyMap(), any());

        // then
        assertThatExceptionOfType(AccessDeniedException.class)
                .isThrownBy(() -> methodAnnotatedBean.update(ADVENTURE_ID));
    }

    @Test
    void shouldNotAuthorizeBeansWithoutTheAnnotation() {

        // when
        var result = unannotatedBean.doThing();

        // then
        assertThat(result).isEqualTo("done");
        verify(authorizationService, never()).authorize(any(), anyMap(), any());
    }

    private MoiraiPrincipal principal() {
        return new MoiraiPrincipal(
                UUID.randomUUID(), 1L, "caller", "token", "refresh", Role.PLAYER, null);
    }
}
