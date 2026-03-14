package me.moirai.storyengine.common.cqs.command;

public interface CommandRunner {

    public <T> T run(Command<T> useCase);

    <A extends Command<T>, T> void registerHandler(AbstractCommandHandler<A, T> handler);
}
