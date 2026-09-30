package me.moirai.storyengine.common.security.authentication;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import me.moirai.storyengine.common.cqs.query.QueryRunner;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.exception.AuthenticationFailedException;
import me.moirai.storyengine.core.port.inbound.userdetails.GetAuthenticatedUserDetails;

@Service
public class MoiraiUserDetailsService implements UserDetailsService {

    private static final String DEACTIVATED_USER = "Deactivated user requested authentication";
    private static final String INVALID_USER = "Invalid user requested authentication";

    private final QueryRunner queryRunner;

    public MoiraiUserDetailsService(QueryRunner queryRunner) {
        this.queryRunner = queryRunner;
    }

    @Override
    public UserDetails loadUserByUsername(String tokenCluster) throws UsernameNotFoundException {

        var authorizationToken = tokenCluster.split(" / ")[0];
        var refreshToken = tokenCluster.split(" / ")[1];

        return getUserDetails(authorizationToken, refreshToken);
    }

    private MoiraiPrincipal getUserDetails(String authorizationToken, String refreshToken) {

        try {
            var user = queryRunner.run(new GetAuthenticatedUserDetails(authorizationToken));

            if (!user.isActive()) {
                throw new AuthenticationFailedException(DEACTIVATED_USER);
            }

            return new MoiraiPrincipal(
                    user.publicId(),
                    user.id(),
                    user.username(),
                    authorizationToken,
                    refreshToken,
                    user.role(),
                    null);
        } catch (NotFoundException e) {
            throw new AuthenticationFailedException(INVALID_USER, e);
        }
    }
}
