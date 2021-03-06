package com.github.orql.executor;

import com.github.orql.executor.orql.OrqlNode.*;
import com.github.orql.executor.orql.Parser;
import org.junit.Assert;
import org.junit.Test;

public class OrqlParserTest {

    private OrqlRefItem parse(String orql) {
        Parser parser = new Parser(ExecutorInstance.getInstance().configuration.getSchemaManager());
        return parser.parse(orql).getRoot();
    }

    @Test
    public void testSimple() {
        OrqlRefItem item = parse("user: {id, name}");
        Assert.assertEquals("user", item.getName());
    }

    @Test
    public void testAllItem() {
        OrqlRefItem item = parse("user: {*}");
        Assert.assertTrue(item.getChildren().get(0) instanceof OrqlAllItem);
    }

    @Test
    public void testAllItemIgnore() {
        OrqlRefItem item = parse("user: {*, !password}");
        Assert.assertTrue(item.getChildren().get(0) instanceof OrqlAllItem);
        Assert.assertTrue(item.getChildren().get(1) instanceof OrqlIgnoreItem);
        Assert.assertEquals(item.getChildren().get(1).getName(), "password");
    }
}
