package com.github.orql.executor;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class ExecutorInstance {

    private static OrqlExecutor orqlExecutor;

    public static OrqlExecutor getInstance() {
        if (orqlExecutor == null) {
            Configuration configuration = new Configuration();
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl("jdbc:mysql://localhost:3306/myorm");
            hikariConfig.setUsername("root");
            hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
            DataSource dataSource = new HikariDataSource(hikariConfig);
            configuration.setDataSource(dataSource);
            configuration.getSchemaManager().scanPackage("com.github.orql.executor.schema");
            orqlExecutor = new OrqlExecutor(configuration);
        }
        return orqlExecutor;
    }

    public static void autoRollback(SessionExecutor executor) {
        OrqlExecutor orqlExecutor = getInstance();
        Session session = orqlExecutor.newSession();
        session.beginTransaction();
        executor.execute(session);
        session.rollback();
        session.close();
    }

    public static void transaction(SessionExecutor executor) {
        OrqlExecutor orqlExecutor = getInstance();
        Session session = orqlExecutor.newSession();
        session.beginTransaction();
        executor.execute(session);
        session.commit();
    }

}
