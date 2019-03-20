package com.github.orql.executor;

import com.github.orql.executor.sql.NamedParamSql;

import java.sql.ResultSet;
import java.util.Map;

public interface Session {

    void beginTransaction();

    void close();

    void commit();

    void rollback();

    Object query(String reql, Map<String, Object> params, Long offset, Integer limit);

    /**
     * hasOne hasMany 先插入上级，然后把上级id赋值到下级外键，然后插入下一级
     * belongsTo 把id赋值到当前的外键上，插入
     * belongsToMany 先插入上级，然后插入middle表
     * @param reql
     * @param params
     */
    Object add(String reql, Map<String, Object> params);

    /**
     *
     * @param reql
     * @param params
     */
    void delete(String reql, Map<String, Object> params);

    void update(String reql, Map<String, Object> params);

    ResultSet nativeQuery(NamedParamSql namedParamSql);

    Object nativeAdd(NamedParamSql namedParamSql);

    int nativeUpdate(NamedParamSql namedParamSql);

    int nativeDelete(NamedParamSql namedParamSql);

    QueryBuilder buildQuery();

    UpdateBuilder buildUpdate();

    NativeBuilder buildNative();

}
