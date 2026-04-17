package me.moirai.storyengine.common.cqs.command;

import static java.util.Objects.isNull;

import me.moirai.storyengine.common.annotation.CommandHandler;

@CommandHandler
public abstract class AbstractCommandHandler<A extends Command<T>, T> {

    public abstract T execute(A request);

    public void validate(A request) {

    }

    public T handle(A request) {

        if (isNull(request)) {
            throw new IllegalArgumentException("Command request cannot be null");
        }

        validate(request);
        return execute(request);
    }
}
