package me.moirai.storyengine.common.usecases;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;

@SuppressWarnings("all")
public class UseCaseRunnerImpl implements UseCaseRunner {

    private static final Logger LOG = LoggerFactory.getLogger(UseCaseRunnerImpl.class);

    private static final String HANDLER_NOT_FOUND = "No use case handler found for %s";
    private static final String HANDLER_CANNOT_BE_NULL = "Cannot register null handlers";
    private static final String HANDLER_ALREADY_REGISTERED = "Cannot register use case handler for %s - there is a handler already registered";
    private static final String HANDLER_REGISTERED = "Handler {} registered for use case {}";

    private final Map<Class<? extends UseCase<?>>, AbstractUseCaseHandler<?, ?>> handlersByUseCase = new HashMap<>();

    @Override
    public <T> T run(UseCase<T> useCase) {

        AbstractUseCaseHandler<?, ?> handler = handlersByUseCase.get(useCase.getClass());

        if (handler == null) {
            String errorMessage = String.format(HANDLER_NOT_FOUND, useCase.getClass().getSimpleName());
            throw new IllegalArgumentException(errorMessage);
        }

        return ((AbstractUseCaseHandler<UseCase<T>, T>) handler).handle(useCase);
    }

    @Override
    public <A extends UseCase<T>, T> void registerHandler(AbstractUseCaseHandler<A, T> handler) {

        if (Objects.isNull(handler)) {
            throw new IllegalArgumentException(HANDLER_CANNOT_BE_NULL);
        }

        Class<A> useCaseType = extractUseCaseType(handler);

        boolean isHandlerAlreadyRegisteredForUseCase = handlersByUseCase.containsKey(useCaseType);
        if (isHandlerAlreadyRegisteredForUseCase) {
            throw new IllegalArgumentException(HANDLER_ALREADY_REGISTERED);
        }

        handlersByUseCase.putIfAbsent(useCaseType, handler);

        LOG.debug(HANDLER_REGISTERED, handler.getClass().getSimpleName(), useCaseType.getSimpleName());
    }

    private <A extends UseCase<T>, T> Class<A> extractUseCaseType(AbstractUseCaseHandler<A, T> handler) {

        Class<? extends AbstractUseCaseHandler<A, T>> unproxiedHandler = (Class<? extends AbstractUseCaseHandler<A, T>>) AopUtils
                .getTargetClass(handler);

        ParameterizedType parameterizedType = (ParameterizedType) unproxiedHandler.getGenericSuperclass();

        return (Class<A>) parameterizedType.getActualTypeArguments()[0];
    }
}
