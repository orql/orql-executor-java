package com.github.orql.executor;

import com.github.orql.executor.mapper.ReqlResult;
import com.github.orql.executor.mapper.ResultMapper;
import com.github.orql.executor.mapper.ResultRoot;
import com.github.orql.executor.orql.OrqlNode;
import com.github.orql.executor.orql.Parser;
import com.github.orql.executor.schema.Association;
import com.github.orql.executor.schema.Schema;
import com.github.orql.executor.schema.SchemaManager;
import com.github.orql.executor.sql.NamedParamSql;
import com.github.orql.executor.sql.OrqlToSql;
import com.github.orql.executor.sql.SqlGenerator;
import com.github.orql.executor.sql.SqlNode;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class DefaultSession implements Session {

    protected Connection conn;

    protected SqlGenerator sqlGenerator;

    protected OrqlToSql orqlToSql;

    protected SqlExecutor sqlExecutor;

    protected Parser parser;

    protected ResultMapper resultMapper;

    protected ReqlResult reqlResult;

    protected SchemaManager schemaManager;

    public DefaultSession(Configuration configuration, Connection conn) {
        this.conn = conn;
        this.sqlGenerator = configuration.getSqlGenerator();
        this.orqlToSql = configuration.getOrqlToSql();
        this.sqlExecutor = configuration.getSqlExecutor();
        this.parser = configuration.getParser();
        this.reqlResult = configuration.getReqlResult();
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
    public Object query(String reql, Map<String, Object> params, Long offset, Integer limit) {
        try {
            OrqlNode tree = parser.parse(reql);
            SqlNode.SqlPage sqlPage = new SqlNode.SqlPage(offset, limit);
            NamedParamSql namedParamSql = new NamedParamSql(orqlToSql.toQuery(tree.getOp(), tree.getRoot(), sqlPage), params);
            ResultSet resultSet = sqlExecutor.query(conn, namedParamSql);
            if (tree.getOp() == OrqlNode.ReqlOp.Count) {
                return resultSet.next() ? resultSet.getLong(1) : 0L;
            }
            ResultRoot resultRoot = reqlResult.toResult(tree.getRoot());
            List<Map<String, Object>> results = resultMapper.mappe(resultRoot, resultSet);
            if (tree.getRoot() instanceof OrqlNode.ReqlArrayItem) {
                return results;
            }
            return results.isEmpty() ? null : results.get(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object add(String reql, Map<String, Object> params) {
        OrqlNode tree = parser.parse(reql);
        return add(tree.getRoot(), params);
    }

    private Object add(OrqlNode.ReqlRefItem root, Map<String, Object> params) {
        try {
            Schema schema = root.getRef();
            for (Association association : schema.getAssociations()) {
                String name = association.getName();
                if (params.containsKey(name) && params.get(name) != null) {
                    // 插入前先处理belongsTo,获取其id一起插入
                    if (association.getType() == Association.Type.BelongsTo) {
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
    public void delete(String reql, Map<String, Object> params) {
        try {
            OrqlNode tree = parser.parse(reql);
            OrqlNode.ReqlRefItem root = tree.getRoot();
            NamedParamSql namedParamSql = new NamedParamSql(orqlToSql.toDelete(root), params);
            sqlExecutor.delete(conn, namedParamSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(String reql, Map<String, Object> params) {
        try {
            OrqlNode tree = parser.parse(reql);
            OrqlNode.ReqlRefItem root = tree.getRoot();
            Schema schema = root.getRef();
            for (Association association : schema.getAssociations()) {
                String name = association.getName();
                if (params.containsKey(name) && params.get(name) != null) {
                    // 更改前先处理belongsTo
                    if (association.getType() == Association.Type.BelongsTo) {
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
