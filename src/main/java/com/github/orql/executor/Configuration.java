package com.github.orql.executor;

import com.github.orql.executor.mapper.ReqlResult;
import com.github.orql.executor.mapper.ResultMapper;
import com.github.orql.executor.orql.Parser;
import com.github.orql.executor.schema.SchemaManager;
import com.github.orql.executor.sql.OrqlToSql;
import com.github.orql.executor.sql.SqlGenerator;

import javax.sql.DataSource;

public class Configuration {

    /**
     * sql执行器
     */
    private SqlExecutor sqlExecutor;

    /**
     * reql生成器
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
     * reql映射
     */
    private ReqlResult reqlResult;

    public Configuration() {
        sqlExecutor = new SqlExecutor();
        orqlToSql = new OrqlToSql();
        sqlGenerator = new SqlGenerator();
        schemaManager = new SchemaManager();
        resultMapper = new ResultMapper();
        reqlResult = new ReqlResult();
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

    public ReqlResult getReqlResult() {
        return reqlResult;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}