package com.github.orql.executor.migration;

import com.github.orql.executor.Session;

import java.sql.SQLException;

public interface Migration {

    void create(Session session) throws SQLException;

    void update(Session session) throws SQLException;

    void drop(Session session) throws SQLException;

}
