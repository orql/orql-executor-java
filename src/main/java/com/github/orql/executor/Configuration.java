package com.github.orql.executor;

import com.github.orql.core.mapper.OrqlResult;
import com.github.orql.core.mapper.ResultMapper;
import com.github.orql.core.orql.Parser;
import com.github.orql.core.schema.SchemaManager;
import com.github.orql.core.sql.OrqlToSql;
import com.github.orql.core.sql.SqlGenerator;

import javax.sql.DataSource;

public class Configuration {

    /**
     * sql执行器
     */
    private SqlExecutor sqlExecutor;

    /**
     * orql生成器
     */
    private OrqlToSql orqlToSql;

    /**
     * sql生成器
     */
    private SqlGenerator sqlGenerator;

    /**
     * 数据源工厂
     */
    private DataSource dataSource;

    /**
     * schema注册器
     */
    private SchemaManager schemaManager;

    /**
     * 映射器
     */
    private ResultMapper resultMapper;

    /**
     * orql映射
     */
    private OrqlResult orqlResult;

    public Configuration() {
        sqlExecutor = new SqlExecutor();
        orqlToSql = new OrqlToSql();
        sqlGenerator = new SqlGenerator();
        schemaManager = new SchemaManager();
        resultMapper = new ResultMapper();
        orqlResult = new OrqlResult();
    }

    public SchemaManager getSchemaManager() {
        return schemaManager;
    }

    public Parser getParser() {
        return new Parser(schemaManager);
    }

    public SqlExecutor getSqlExecutor() {
        return sqlExecutor;
    }

    public SqlGenerator getSqlGenerator() {
        return sqlGenerator;
    }

    public OrqlToSql getOrqlToSql() {
        return orqlToSql;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public ResultMapper getResultMapper() {
        return resultMapper;
    }

    public OrqlResult getOrqlResult() {
        return orqlResult;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}