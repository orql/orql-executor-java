package com.github.orql.executor;

import com.github.orql.executor.migration.Migration;
import com.github.orql.executor.migration.MysqlMigration;
import com.github.orql.executor.schema.Post;
import com.github.orql.executor.schema.User;
import com.github.orql.executor.schema.UserInfo;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.util.*;

public class SessionTest {

    private static OrqlExecutor executor = ExecutorInstance.getInstance();

    public static String randomString(int length){
        String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random=new Random();
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<length; i++){
            int number=random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }


    @BeforeClass
    public static void setUp() throws SQLException {
        Migration migration = new MysqlMigration(executor.configuration);
        Session session = executor.newSession();
        migration.drop(session);
        migration.create(session);
        session.close();
    }

    @Test
    public void testAdd() {
        ExecutorInstance.autoRollback(session -> {
            User user = new User();
            user.setName(randomString(8));
            user.setPassword(randomString(8));
            session.buildUpdate().add(user);
            Assert.assertNotNull(user.getId());
        });
    }

    @Test
    public void testQueryOneById() {
        ExecutorInstance.autoRollback(session -> {
            User user = new User();
            user.setName(randomString(8));
            user.setPassword(randomString(8));
            session.buildUpdate().add(user);

            User result = session.buildQuery()
                    .orql("user(id = $id): {name, password}")
                    .param("id", user.getId())
                    .queryOne();

            Assert.assertEquals(result.getName(), user.getName());
            Assert.assertEquals(result.getPassword(), user.getPassword());
        });
    }

    @Test
    public void testQueryOneByAnd() {
        ExecutorInstance.autoRollback(session -> {
            User user = new User();
            user.setName(randomString(8));
            user.setPassword(randomString(8));
            session.buildUpdate().add(user);

            User result = session.buildQuery()
                    .orql("user(name = $name && password = $password): {id}")
                    .param("name", user.getName())
                    .param("password", user.getPassword())
                    .queryOne();

            Assert.assertEquals(result.getId(), user.getId());
        });
    }

    @Test
    public void testAddBelongsTo() {
        ExecutorInstance.autoRollback(session -> {
            User user = new User();
            user.setName(randomString(8));
            user.setPassword(randomString(8));
            session.buildUpdate().add(user);

            UserInfo info = new UserInfo();
            info.setNo(randomString(8));
            info.setUser(user);

            session.buildUpdate().add(info);

            UserInfo result = session.buildQuery()
                    .orql("userInfo(id = $id): {user: {name}}")
                    .param("id", info.getId())
                    .queryOne();
            Assert.assertEquals(user.getName(), result.getUser().getName());
        });
    }

    @Test
    public void testAddHasOne() {
        ExecutorInstance.autoRollback(session -> {
            User user = new User();
            user.setName(randomString(8));
            user.setPassword(randomString(8));

            UserInfo info = new UserInfo();
            info.setNo(randomString(8));

            user.setInfo(info);

            session.buildUpdate().add(user);

            User result = session.buildQuery()
                    .orql("user(id = $id):{info: {no}}")
                    .param("id", user.getId())
                    .queryOne();

            Assert.assertEquals(user.getInfo().getNo(), result.getInfo().getNo());
        });
    }

    @Test
    public void testAddHasMany() {
        ExecutorInstance.autoRollback(session -> {
            User user = new User();
            user.setName(randomString(8));
            user.setPassword(randomString(8));

            List<Post> posts = new ArrayList<>();
            for (int i = 0; i < 2; i ++) {
                Post post = new Post();
                post.setTitle(randomString(8));
                posts.add(post);
            }

            user.setPosts(posts);

            session.buildUpdate().add(user);

            List<Post> result = session.buildQuery()
                    .orql("post: {author(id = $userId): {name}}")
                    .param("userId", user.getId())
                    .queryAll();
            for (Post post : result) {
                Assert.assertEquals(user.getName(), post.getAuthor().getName());
            }
        });
    }

}
