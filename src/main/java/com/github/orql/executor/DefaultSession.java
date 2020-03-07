package com.github.orql.executor;

import com.github.orql.core.QueryOrder;
import com.github.orql.core.orql.OrqlNode;
import com.github.orql.core.orql.OrqlParser;
import com.github.orql.core.schema.AssociationInfo;
import com.github.orql.core.schema.SchemaInfo;
import com.github.orql.core.schema.SchemaManager;
import com.github.orql.core.sql.NamedParamSql;
import com.github.orql.core.sql.OrqlToSql;
import com.github.orql.core.sql.SqlGenerator;
import com.github.orql.executor.mapper.OrqlResultGen;
import com.github.orql.executor.mapper.ResultMapper;
import com.github.orql.executor.mapper.ResultRoot;
import com.github.orql.executor.util.MapBean;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DefaultSession implements Session {

    protected Connection conn;

    protected SqlGenerator sqlGenerator;

    protected OrqlToSql orqlToSql;

    protected SqlExecutor sqlExecutor;

    protected OrqlParser orqlParser;

    protected ResultMapper resultMapper;

    protected OrqlResultGen orqlResultGen;

    protected SchemaManager schemaManager;

    public DefaultSession(Configuration configuration, Connection conn) {
        this.conn = conn;
        this.sqlGenerator = configuration.getSqlGenerator();
        this.orqlToSql = configuration.getOrqlToSql();
        this.sqlExecutor = configuration.getSqlExecutor();
        this.orqlParser = configuration.getOrqlParser();
        this.orqlResultGen = configuration.getOrqlResultGen();
        this.resultMapper = configuration.getResultMapper();
        this.schemaManager = configuration.getSchemaManager();
    }

    @Override
    public void beginTransaction() {
        try {
            if (conn.getAutoCommit()) {
                conn.setAutoCommit(false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void commit() {
        try {
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void rollback() {
        try {
            conn.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public <T> T queryOne(String orql, Map<String, Object> params, Long offset, List<QueryOrder> orders) {
        OrqlNode node = orqlParser.parse(orql);
        String sql = orqlToSql.toQuery("queryOne", node.getRoot(), false, orders);
        NamedParamSql namedParamSql = new NamedParamSql(sql, params);
        try {
            ResultSet resultSet = sqlExecutor.query(conn, namedParamSql);
            ResultRoot resultRoot = orqlResultGen.toResult(node.getRoot());
            List<Map<String, Object>> results = resultMapper.mappe(resultRoot, resultSet);
            Class clazz = node.getRoot().getRef().getClazz();
            return results.isEmpty() ? null : (T) MapBean.toBean(results.get(0), clazz);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T> List<T> queryAll(String orql, Map<String, Object> params, Integer limit, Long offset, List<QueryOrder> orders) {
        OrqlNode node = orqlParser.parse(orql);
        String sql = orqlToSql.toQuery("queryAll", node.getRoot(), true, orders);
        params.put("limit", limit);
        params.put("offset", offset);
        NamedParamSql namedParamSql = new NamedParamSql(sql, params);
        try {
            ResultSet resultSet = sqlExecutor.query(conn, namedParamSql);
            ResultRoot resultRoot = orqlResultGen.toResult(node.getRoot());
            List<Map<String, Object>> results = resultMapper.mappe(resultRoot, resultSet);
            Class clazz = node.getRoot().getRef().getClazz();
            List<T> list = new ArrayList<>();
            for (Map<String, Object> result : results) {
                list.add((T) MapBean.toBean(result, clazz));
            }
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public long count(String orql, Map<String, Object> params) {
        OrqlNode node = orqlParser.parse(orql);
        String sql = orqlToSql.toQuery("count", node.getRoot(), false, null);
        NamedParamSql namedParamSql = new NamedParamSql(sql, params);
        try {
            ResultSet resultSet = sqlExecutor.query(conn, namedParamSql);
            return resultSet.next() ? resultSet.getLong(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public Object add(String orql, Map<String, Object> params) {
        OrqlNode tree = orqlParser.parse(orql);
        return add(tree.getRoot(), params);
    }

    private Object add(OrqlNode.OrqlRefItem root, Map<String, Object> params) {
        try {
            SchemaInfo schema = root.getRef();
            for (AssociationInfo association : schema.getAssociations()) {
                String name = association.getName();
                if (params.containsKey(name) && params.get(name) != null) {
                    // 插入前先处理belongsTo,获取其id一起插入
                    if (association.getType() == AssociationInfo.Type.BelongsTo) {
                        Map<String, Object> childData = (Map<String, Object>) params.get(name);
                        Object childId = childData.get(association.getRefId().getName());
                        if (childId != null) {
                            params.put(association.getRefKey(), childId);
                        }
                    }
                }
            }
            NamedParamSql namedParamSql = new NamedParamSql(orqlToSql.toAdd(root), params);
            namedParamSql.setGeneratedKey(true);
            namedParamSql.idType(schema.getIdColumn().getDataType());
            return sqlExecutor.insert(conn, namedParamSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void delete(String orql, Map<String, Object> params) {
        try {
            OrqlNode tree = orqlParser.parse(orql);
            OrqlNode.OrqlRefItem root = tree.getRoot();
            NamedParamSql namedParamSql = new NamedParamSql(orqlToSql.toDelete(root), params);
            sqlExecutor.delete(conn, namedParamSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(String orql, Map<String, Object> params) {
        try {
            OrqlNode tree = orqlParser.parse(orql);
            OrqlNode.OrqlRefItem root = tree.getRoot();
            SchemaInfo schema = root.getRef();
            for (AssociationInfo association : schema.getAssociations()) {
                String name = association.getName();
                if (params.containsKey(name) && params.get(name) != null) {
                    // 更改前先处理belongsTo
                    if (association.getType() == AssociationInfo.Type.BelongsTo) {
                        Map<String, Object> childData = (Map<String, Object>) params.get(name);
                        params.put(association.getRefKey(), childData.get(association.getRefId().getName()));
                    }
                }
            }
            NamedParamSql namedParamSql = new NamedParamSql(orqlToSql.toUpdate(root), params);
            sqlExecutor.update(conn, namedParamSql);
        } catch ( SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ResultSet nativeQuery(NamedParamSql namedParamSql) {
        try {
            return sqlExecutor.query(conn, namedParamSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object nativeAdd(NamedParamSql namedParamSql) {
        try {
            return sqlExecutor.insert(conn, namedParamSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int nativeUpdate(NamedParamSql namedParamSql) {
        try {
            return sqlExecutor.update(this.conn, namedParamSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int nativeDelete(NamedParamSql namedParamSql) {
        try {
            return sqlExecutor.delete(this.conn, namedParamSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public QueryBuilder buildQuery() {
        return new QueryBuilder(this, schemaManager);
    }

    @Override
    public UpdateBuilder buildUpdate() {
        return new UpdateBuilder(this, schemaManager);
    }

    @Override
    public NativeBuilder buildNative() {
        return new NativeBuilder(this);
    }
}
