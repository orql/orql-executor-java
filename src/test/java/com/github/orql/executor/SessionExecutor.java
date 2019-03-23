package com.github.orql.executor;

@FunctionalInterface
public interface SessionExecutor {

    void execute(Session session);

}
