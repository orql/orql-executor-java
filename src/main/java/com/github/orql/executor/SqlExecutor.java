package com.github.orql.executor;

import com.github.orql.executor.sql.NamedParamSql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

class SqlExecutor {

    private static final Logger logger = LoggerFactory.getLogger(SqlExecutor.class);

    private void setParams(PreparedStatement statement, Object[] values) throws SQLException {
        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            if (value != null && value.getClass().isEnum()) {
                statement.setObject(i + 1, ((Enum) value).name());
                continue;
            }
            statement.setObject(i + 1, values[i]);
        }
    }

    public Object insert(Connection conn, NamedParamSql namedParamSql) throws SQLException {
        logger.info(namedParamSql.toString());
        PreparedStatement statement = namedParamSql.isGeneratedKey() ? conn.prepareStatement(namedParamSql.getSql(), Statement.RETURN_GENERATED_KEYS) : conn.prepareStatement(namedParamSql.getSql());
        setParams(statement, namedParamSql.getParams());
        int row = statement.executeUpdate();
        if (row == 0) {
            throw new SQLException();
        }
        if (namedParamSql.isGeneratedKey()) {
            ResultSet resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                switch (namedParamSql.getIdType()) {
                    case Int:
                        return resultSet.getInt(1);
                    case Long:
                        return resultSet.getLong(1);
                }
                return resultSet.getObject(1);
            } else {
                throw new SQLException();
            }
        }
        return null;
    }

    private int mutation(Connection conn, NamedParamSql namedParamSql) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(namedParamSql.getSql());
        setParams(statement, namedParamSql.getParams());
        return statement.executeUpdate();
    }

    public int delete(Connection conn, NamedParamSql namedParamSql) throws SQLException {
        logger.info(namedParamSql.toString());
        return mutation(conn, namedParamSql);
    }

    public int update(Connection conn, NamedParamSql namedParamSql) throws SQLException {
        logger.info(namedParamSql.toString());
        return mutation(conn, namedParamSql);
    }

    public ResultSet query(Connection conn, NamedParamSql namedParamSql) throws SQLException {
        logger.info(namedParamSql.toString());
        PreparedStatement statement = conn.prepareStatement(namedParamSql.getSql());
        setParams(statement, namedParamSql.getParams());
        return statement.executeQuery();
    }
}