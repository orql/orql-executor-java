package com.github.orql.executor;

import com.github.orql.core.QueryOrder;
import com.github.orql.core.sql.NamedParamSql;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

public interface Session {

    void beginTransaction();

    void close();

    void commit();

    void rollback();

    <T> T queryOne(String orql, Map<String, Object> params, Long offset, List<QueryOrder> orders);

    <T> List<T> queryAll(String orql, Map<String, Object> params, Integer limit, Long offset, List<QueryOrder> orders);

    long count(String orql, Map<String, Object> params);

    /**
     * hasOne hasMany 先插入上级，然后把上级id赋值到下级外键，然后插入下一级
     * belongsTo 把id赋值到当前的外键上，插入
     * belongsToMany 先插入上级，然后插入middle表
     * @param orql
     * @param params
     */
    Object add(String orql, Map<String, Object> params);

    /**
     *
     * @param orql
     * @param params
     */
    void delete(String orql, Map<String, Object> params);

    void update(String orql, Map<String, Object> params);

    ResultSet nativeQuery(NamedParamSql namedParamSql);

    Object nativeAdd(NamedParamSql namedParamSql);

    int nativeUpdate(NamedParamSql namedParamSql);

    int nativeDelete(NamedParamSql namedParamSql);

    QueryBuilder buildQuery();

    UpdateBuilder buildUpdate();

    NativeBuilder buildNative();

}
