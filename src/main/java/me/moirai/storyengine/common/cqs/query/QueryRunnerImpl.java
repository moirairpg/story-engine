package me.moirai.storyengine.common.cqs.query;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;

@SuppressWarnings("all")
public class QueryRunnerImpl implements QueryRunner {

    private static final Logger LOG = LoggerFactory.getLogger(QueryRunnerImpl.class);

    private static final String HANDLER_NOT_FOUND = "No query handler found for %s";
    private static final String HANDLER_CANNOT_BE_NULL = "Cannot register null handlers";
    private static final String HANDLER_ALREADY_REGISTERED = "Cannot register query handler for %s - there is a handler already registered";
    private static final String HANDLER_REGISTERED = "Handler {} registered for query {}";

    private final Map<Class<? extends Query<?>>, AbstractQueryHandler<?, ?>> handlersByQuery = new HashMap<>();

    @Override
    public <T> T run(Query<T> query) {

        AbstractQueryHandler<?, ?> handler = handlersByQuery.get(query.getClass());

        if (handler == null) {
            String errorMessage = String.format(HANDLER_NOT_FOUND, query.getClass().getSimpleName());
            throw new IllegalArgumentException(errorMessage);
        }

        return ((AbstractQueryHandler<Query<T>, T>) handler).handle(query);
    }

    @Override
    public <A extends Query<T>, T> void registerHandler(AbstractQueryHandler<A, T> handler) {

        if (Objects.isNull(handler)) {
            throw new IllegalArgumentException(HANDLER_CANNOT_BE_NULL);
        }

        Class<A> queryType = extractQueryType(handler);

        boolean isHandlerAlreadyRegisteredForQuery = handlersByQuery.containsKey(queryType);
        if (isHandlerAlreadyRegisteredForQuery) {
            throw new IllegalArgumentException(HANDLER_ALREADY_REGISTERED);
        }

        handlersByQuery.putIfAbsent(queryType, handler);

        LOG.debug(HANDLER_REGISTERED, handler.getClass().getSimpleName(), queryType.getSimpleName());
    }

    private <A extends Query<T>, T> Class<A> extractQueryType(AbstractQueryHandler<A, T> handler) {

        Class<? extends AbstractQueryHandler<A, T>> unproxiedHandler = (Class<? extends AbstractQueryHandler<A, T>>) AopUtils
                .getTargetClass(handler);

        ParameterizedType parameterizedType = (ParameterizedType) unproxiedHandler.getGenericSuperclass();

        return (Class<A>) parameterizedType.getActualTypeArguments()[0];
    }
}
