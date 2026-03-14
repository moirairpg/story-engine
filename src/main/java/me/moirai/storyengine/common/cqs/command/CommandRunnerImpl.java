package me.moirai.storyengine.common.cqs.command;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;

@SuppressWarnings("all")
public class CommandRunnerImpl implements CommandRunner {

    private static final Logger LOG = LoggerFactory.getLogger(CommandRunnerImpl.class);

    private static final String HANDLER_NOT_FOUND = "No command handler found for %s";
    private static final String HANDLER_CANNOT_BE_NULL = "Cannot register null handlers";
    private static final String HANDLER_ALREADY_REGISTERED = "Cannot register command handler for %s - there is a handler already registered";
    private static final String HANDLER_REGISTERED = "Handler {} registered for command {}";

    private final Map<Class<? extends Command<?>>, AbstractCommandHandler<?, ?>> handlersByCommand = new HashMap<>();

    @Override
    public <T> T run(Command<T> command) {

        AbstractCommandHandler<?, ?> handler = handlersByCommand.get(command.getClass());

        if (handler == null) {
            String errorMessage = String.format(HANDLER_NOT_FOUND, command.getClass().getSimpleName());
            throw new IllegalArgumentException(errorMessage);
        }

        return ((AbstractCommandHandler<Command<T>, T>) handler).handle(command);
    }

    @Override
    public <A extends Command<T>, T> void registerHandler(AbstractCommandHandler<A, T> handler) {

        if (Objects.isNull(handler)) {
            throw new IllegalArgumentException(HANDLER_CANNOT_BE_NULL);
        }

        Class<A> commandType = extractCommandType(handler);

        boolean isHandlerAlreadyRegisteredForCommand = handlersByCommand.containsKey(commandType);
        if (isHandlerAlreadyRegisteredForCommand) {
            throw new IllegalArgumentException(HANDLER_ALREADY_REGISTERED);
        }

        handlersByCommand.putIfAbsent(commandType, handler);

        LOG.debug(HANDLER_REGISTERED, handler.getClass().getSimpleName(), commandType.getSimpleName());
    }

    private <A extends Command<T>, T> Class<A> extractCommandType(AbstractCommandHandler<A, T> handler) {

        Class<? extends AbstractCommandHandler<A, T>> unproxiedHandler = (Class<? extends AbstractCommandHandler<A, T>>) AopUtils
                .getTargetClass(handler);

        ParameterizedType parameterizedType = (ParameterizedType) unproxiedHandler.getGenericSuperclass();

        return (Class<A>) parameterizedType.getActualTypeArguments()[0];
    }
}
