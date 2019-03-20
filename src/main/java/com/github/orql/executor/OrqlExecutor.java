package com.github.orql.executor;

import java.sql.Connection;
import java.sql.SQLException;

public class OrqlExecutor {

    protected Configuration configuration;

    public OrqlExecutor(Configuration configuration) {
        this.configuration = configuration;
    }

    public Session newSession() {
        try {
            return new DefaultSession(configuration, configuration.getDataSource().getConnection());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Session newSession(Connection conn) {
        return new DefaultSession(configuration, conn);
    }

}