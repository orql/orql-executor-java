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

    private User createUser(Session session) {
        User user = new User();
        user.setName(randomString(8));
        user.setPassword(randomString(8));
        session.buildUpdate().add(user);
        return user;
    }

    @Test
    public void testAdd() {
        ExecutorInstance.transaction(session -> {
            User user = createUser(session);
            Assert.assertNotNull(user.getId());
        });
    }

    @Test
    public void testQueryAllItem() {
        ExecutorInstance.transaction(session -> {
            User user = createUser(session);
            User result = session.buildQuery()
                    .orql("user(id = $id): {*}")
                    .param("id", user.getId())
                    .queryOne();
            Assert.assertEquals(result.getId(), user.getId());
            Assert.assertEquals(result.getName(), user.getName());
            Assert.assertEquals(result.getPassword(), user.getPassword());
        });
    }

    @Test
    public void testQueryAllItemIgnore() {
        ExecutorInstance.transaction(session -> {
            User user = createUser(session);
            User result = session.buildQuery()
                    .orql("user(id = $id): {*, !password}")
                    .param("id", user.getId())
                    .queryOne();
            Assert.assertEquals(result.getId(), user.getId());
            Assert.assertEquals(result.getName(), user.getName());
            Assert.assertNull(result.getPassword());
        });
    }

    @Test
    public void testAddAll() {
        ExecutorInstance.transaction(session -> {
            User user = new User();
            user.setName(randomString(8));
            user.setPassword(randomString(8));
            session.buildUpdate().add("user:{*}", user);
            User result = session.buildQuery()
                    .orql("user(id = $id): {*}")
                    .param("id", user.getId())
                    .queryOne();
            Assert.assertEquals(result.getId(), user.getId());
            Assert.assertEquals(result.getName(), user.getName());
            Assert.assertEquals(result.getPassword(), user.getPassword());
        });
    }

    @Test
    public void testUpdate() {
        ExecutorInstance.transaction(session -> {
            User user = createUser(session);
            String newName = randomString(8);
            user.setName(newName);
            session.buildUpdate().update(user);
            User result = session.buildQuery()
                    .orql("user(id = $id): {*}")
                    .param("id", user.getId())
                    .queryOne();
            Assert.assertEquals(result.getName(), newName);
        });
    }

    @Test
    public void testUpdateByOrql() {
        ExecutorInstance.transaction(session -> {
            User user = createUser(session);
            String newName = randomString(8);
            user.setName(newName);
            session.buildUpdate()
                    .update("user(id = $id) : {name}", user);
            User result = session.buildQuery()
                    .orql("user(id = $id): {*}")
                    .param("id", user.getId())
                    .queryOne();
            Assert.assertEquals(result.getName(), newName);
        });
    }

    @Test
    public void testUpdateIgnore() {
        ExecutorInstance.transaction(session -> {
            User user = createUser(session);
            String newName = randomString(8);
            String newPassword = randomString(8);
            user.setName(newName);
            user.setPassword(newPassword);
            session.buildUpdate()
                    .update("user(id = $id) : {*, !id}", user);
            User result = session.buildQuery()
                    .orql("user(id = $id): {*}")
                    .param("id", user.getId())
                    .queryOne();
            Assert.assertEquals(result.getName(), newName);
            Assert.assertEquals(result.getPassword(), newPassword);
        });
    }

    @Test
    public void testQueryOneById() {
        ExecutorInstance.transaction(session -> {
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
        ExecutorInstance.transaction(session -> {
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
        ExecutorInstance.transaction(session -> {
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
        ExecutorInstance.transaction(session -> {
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
        ExecutorInstance.transaction(session -> {
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
