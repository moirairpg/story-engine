package me.moirai.storyengine.core.application.command.user;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import me.moirai.storyengine.common.annotation.Authorize;
import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.port.inbound.userdetails.DeleteUsers;
import me.moirai.storyengine.core.port.inbound.userdetails.DeleteUsersResult;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@CommandHandler
@Authorize(operation = AuthorizationOperation.DELETE_USERS)
public class DeleteUsersHandler extends AbstractCommandHandler<DeleteUsers, DeleteUsersResult> {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteUsersHandler.class);

    private static final String CANNOT_DELETE_OWN_ACCOUNT = "An account cannot be deleted by its own holder from the users page";
    private static final String USER_NOT_REGISTERED = "The User with the requested username is not registered in MoirAI";
    private static final String DELETION_FAILED = "Deletion of user {} failed during a bulk deletion";

    private final UserRepository repository;
    private final ApplicationEventPublisher eventPublisher;
    private final TransactionTemplate transactionTemplate;

    public DeleteUsersHandler(
            UserRepository repository,
            ApplicationEventPublisher eventPublisher,
            PlatformTransactionManager transactionManager) {

        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public DeleteUsersResult execute(DeleteUsers command) {

        var isDeletingOwnAccount = command.usernames().stream()
                .anyMatch(username -> username.equalsIgnoreCase(command.requesterUsername()));

        if (isDeletingOwnAccount) {
            throw new BusinessRuleViolationException(CANNOT_DELETE_OWN_ACCOUNT);
        }

        var failed = new ArrayList<String>();

        command.usernames()
                .forEach(username -> {
                    try {
                        transactionTemplate.executeWithoutResult(status -> deleteUser(username));
                    } catch (Exception e) {
                        LOG.error(DELETION_FAILED, username, e);

                        failed.add(storedUsernameOf(username));
                    }
                });

        return new DeleteUsersResult(failed);
    }

    private void deleteUser(String username) {

        var user = repository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(USER_NOT_REGISTERED));

        user.communicateUserDeleted();
        user.drainEvents().forEach(eventPublisher::publishEvent);

        repository.delete(user);
    }

    private String storedUsernameOf(String username) {

        return repository.findByUsername(username)
                .map(User::getUsername)
                .orElse(username);
    }
}
