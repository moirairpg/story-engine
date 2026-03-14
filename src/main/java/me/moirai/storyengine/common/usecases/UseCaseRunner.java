package me.moirai.storyengine.common.usecases;

public interface UseCaseRunner {

    public <T> T run(UseCase<T> useCase);

    <A extends UseCase<T>, T> void registerHandler(AbstractUseCaseHandler<A, T> handler);
}
