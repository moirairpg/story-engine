package me.moirai.storyengine.common.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.cqs.query.QueryRunner;
import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.common.exception.AuthenticationFailedException;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authentication.MoiraiUserDetailsService;
import me.moirai.storyengine.core.port.inbound.userdetails.GetAuthenticatedUserDetails;
import me.moirai.storyengine.core.port.inbound.userdetails.UserDetailsResult;

@ExtendWith(MockitoExtension.class)
public class MoiraiUserDetailsServiceTest {

    @Mock
    private QueryRunner queryRunner;

    @InjectMocks
    private MoiraiUserDetailsService service;

    @Test
    public void authenticateUser_whenUserExists_thenReturnPrincipalCarryingTheStoredHandle() {

        // Given
        var token = "AUTH_TOKEN / REFRESH_TOKEN";
        var publicId = UUID.randomUUID();
        var user = userDetails(publicId, true);

        when(queryRunner.run(any(GetAuthenticatedUserDetails.class))).thenReturn(user);

        // When
        var userDetails = service.loadUserByUsername(token);

        // Then
        var principal = (MoiraiPrincipal) userDetails;
        assertThat(principal).isNotNull();
        assertThat(principal.publicId()).isEqualTo(publicId);
        assertThat(principal.id()).isEqualTo(1L);
        assertThat(principal.getUsername()).isEqualTo("john.doe");
        assertThat(principal.role()).isEqualTo(Role.PLAYER);
        assertThat(principal.authorizationToken()).isEqualTo("AUTH_TOKEN");
        assertThat(principal.refreshToken()).isEqualTo("REFRESH_TOKEN");
    }

    @Test
    public void authenticateUser_whenUserIsDeactivated_thenThrowAuthenticationFailed() {

        // Given
        var token = "AUTH_TOKEN / REFRESH_TOKEN";
        var user = userDetails(UUID.randomUUID(), false);

        when(queryRunner.run(any(GetAuthenticatedUserDetails.class))).thenReturn(user);

        // Then
        assertThatThrownBy(() -> service.loadUserByUsername(token))
                .isInstanceOf(AuthenticationFailedException.class);
    }

    @Test
    public void authenticateUser_whenNoMoiraiUserExists_thenThrowAuthenticationFailed() {

        // Given
        var token = "AUTH_TOKEN / REFRESH_TOKEN";

        when(queryRunner.run(any(GetAuthenticatedUserDetails.class)))
                .thenThrow(new NotFoundException("The User with the requested ID is not registered in MoirAI"));

        // Then
        assertThatThrownBy(() -> service.loadUserByUsername(token))
                .isInstanceOf(AuthenticationFailedException.class);
    }

    private UserDetailsResult userDetails(UUID publicId, boolean isActive) {

        return new UserDetailsResult(
                publicId,
                1L,
                "john_discord",
                "john.doe",
                "John Doe",
                null,
                Role.PLAYER,
                isActive,
                null,
                Instant.now());
    }
}
