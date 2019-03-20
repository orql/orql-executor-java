package com.github.orql.executor.sql;

import com.github.orql.executor.schema.DataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 命名参数sql
 * select * from user where id = #id
 * select * from user where id = $id
 */
public class NamedParamSql {

    private Map<String, Object> params;

    private String sql;

    private boolean generatedKey = false;

    private List<String> paramNames;

    private DataType idType;

    private enum MatchStatus {
        State,
        Param
    }

    public NamedParamSql(String sql) {
        params = new HashMap<>();
        paramNames = new ArrayList<>();
        init(sql);
    }

    public NamedParamSql(String sql, Map<String, Object> params) {
        this.params = params;
        paramNames = new ArrayList<>();
        init(sql);
    }

    private void init(String sql) {
        StringBuilder sqlBuilder = new StringBuilder();
        StringBuilder paramBuilder = new StringBuilder();
        MatchStatus status = MatchStatus.State;
        for (char c : sql.toCharArray()) {
            switch (status) {
                case State:
                    if (c == '#' || c == '$') {
                        status = MatchStatus.Param;
                        sqlBuilder.append('?');
                    } else {
                        sqlBuilder.append(c);
                    }
                    break;
                case Param:
                    if (c == ' ' || c == ')' || c == ',') {
                        status = MatchStatus.State;
                        paramNames.add(paramBuilder.toString());
                        paramBuilder.setLength(0);
                        sqlBuilder.append(c);
                    } else {
                        paramBuilder.append(c);
                    }
                    break;
            }
        }
        String last = paramBuilder.toString();
        if (! last.equals("")) paramNames.add(last);
        this.sql = sqlBuilder.toString();
    }

    public NamedParamSql idType(DataType idType) {
        this.idType = idType;
        return this;
    }

    public DataType getIdType() {
        return idType;
    }

    public NamedParamSql param(String name, Object value) {
        params.put(name, value);
        return this;
    }

    public void setGeneratedKey(boolean generatedKey) {
        this.generatedKey = generatedKey;
    }

    public boolean isGeneratedKey() {
        return generatedKey;
    }

    public String getSql() {
        return sql;
    }

    public Object[] getParams() {
        return paramNames.stream().map(name -> params.get(name)).toArray();
    }

    private String getParamString(String name) {
        Object param = params.get(name);
        if (param == null) return name + ": null";
        return name + ": " + param.toString();
    }

    @Override
    public String toString() {
        if (paramNames.size() == 0) return "sql: " + sql;
        return "sql: " + sql + " params: " + paramNames.stream().map(this::getParamString).collect(Collectors.joining(", "));
    }
}
