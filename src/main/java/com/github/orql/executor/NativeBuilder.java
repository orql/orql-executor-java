package com.github.orql.executor;

import com.github.orql.core.sql.NamedParamSql;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class NativeBuilder {

    private String sql;

    private Map<String, Object> params = new HashMap<>();

    private Session session;

    public NativeBuilder(Session session) {
        this.session = session;
    }

    public NativeBuilder sql(String sql) {
        this.sql = sql;
        return this;
    }

    public NativeBuilder param(String key, Object value) {
        this.params.put(key, value);
        return this;
    }

    public ResultSet query() {
        NamedParamSql namedParamSql = new NamedParamSql(sql, params);
        return session.nativeQuery(namedParamSql);
    }

    public int update() {
        NamedParamSql namedParamSql = new NamedParamSql(sql, params);
        return session.nativeUpdate(namedParamSql);
    }

}
