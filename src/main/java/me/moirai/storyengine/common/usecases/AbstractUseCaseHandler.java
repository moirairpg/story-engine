package me.moirai.storyengine.common.usecases;

import static java.util.Objects.isNull;

import me.moirai.storyengine.common.annotation.UseCaseHandler;

@UseCaseHandler
public abstract class AbstractUseCaseHandler<A extends UseCase<T>, T> {

    public abstract T execute(A request);

    public void validate(A request) {

    }

    public T handle(A request) {

        if (isNull(request)) {
            throw new IllegalArgumentException("Use case request cannot be null");
        }

        validate(request);
        return execute(request);
    }
}
