package me.moirai.storyengine.core.application.command.user;

import org.springframework.context.ApplicationEventPublisher;

import me.moirai.storyengine.common.annotation.Authorize;
import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.core.port.inbound.userdetails.DeleteUserByUsername;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@CommandHandler
@Authorize(operation = AuthorizationOperation.MANAGE_USER, fields = "#request.username")
public class DeleteUserByUsernameHandler extends AbstractCommandHandler<DeleteUserByUsername, Void> {

    private static final String USER_NOT_REGISTERED_IN_MOIRAI = "The User with the requested username is not registered in MoirAI";

    private final UserRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public DeleteUserByUsernameHandler(
            UserRepository repository,
            ApplicationEventPublisher eventPublisher) {

        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Void execute(DeleteUserByUsername useCase) {

        var user = repository.findByUsername(useCase.username())
                .orElseThrow(() -> new NotFoundException(USER_NOT_REGISTERED_IN_MOIRAI));

        user.communicateUserDeleted();
        user.drainEvents().forEach(eventPublisher::publishEvent);

        repository.delete(user);

        return null;
    }
}
