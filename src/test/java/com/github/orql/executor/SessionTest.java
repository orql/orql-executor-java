package com.github.orql.executor;

import com.github.orql.executor.migration.Migration;
import com.github.orql.executor.migration.MysqlMigration;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SessionTest {

    private static OrqlExecutor executor = ExecutorInstance.getInstance();

    @BeforeClass
    public static void setUp() throws SQLException {
//        Migration migration = new MysqlMigration(executor.configuration);
//        Session session = executor.newSession();
//        migration.drop(session);
//        migration.create(session);
    }

    @Test
    public void testAdd() throws SQLException {
//        int i = 0;
        Migration migration = new MysqlMigration(executor.configuration);
        Session session = executor.newSession();
        migration.update(session);
//        ExecutorInstance.autoRollback(session -> {
//            Map<String, Object> params = new HashMap<>();
//            session.add("add user : {name, phone}", params);
//        });
    }

}
