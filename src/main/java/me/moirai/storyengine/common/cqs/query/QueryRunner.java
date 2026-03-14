package me.moirai.storyengine.common.cqs.query;

public interface QueryRunner {

    public <T> T run(Query<T> useCase);

    <A extends Query<T>, T> void registerHandler(AbstractQueryHandler<A, T> handler);
}
