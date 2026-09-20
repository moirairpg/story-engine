package me.moirai.storyengine.core.application.command.user;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.userdetails.DeleteUsers;
import me.moirai.storyengine.core.port.inbound.userdetails.DeleteUsersResult;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@CommandHandler
public class DeleteUsersHandler extends AbstractCommandHandler<DeleteUsers, DeleteUsersResult> {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteUsersHandler.class);

    private static final String CANNOT_DELETE_OWN_ACCOUNT = "An account cannot be deleted by its own holder from the users page";
    private static final String USER_NOT_REGISTERED = "The User with the requested ID is not registered in MoirAI";
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

        if (command.userIds().contains(command.requesterId())) {
            throw new BusinessRuleViolationException(CANNOT_DELETE_OWN_ACCOUNT);
        }

        var failed = new ArrayList<UUID>();

        new LinkedHashSet<>(command.userIds())
                .forEach(userId -> {
                    try {
                        transactionTemplate.executeWithoutResult(status -> deleteUser(userId));
                    } catch (RuntimeException e) {
                        LOG.error(DELETION_FAILED, userId, e);

                        failed.add(userId);
                    }
                });

        return new DeleteUsersResult(failed);
    }

    private void deleteUser(UUID userId) {

        var user = repository.findByPublicId(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_REGISTERED));

        user.communicateUserDeleted();
        user.drainEvents().forEach(eventPublisher::publishEvent);

        repository.delete(user);
    }
}
