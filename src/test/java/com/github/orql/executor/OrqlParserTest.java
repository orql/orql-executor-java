package com.github.orql.executor;

import com.github.orql.executor.orql.OrqlNode;
import com.github.orql.executor.orql.Parser;
import org.junit.Assert;
import org.junit.Test;

public class OrqlParserTest {

    private OrqlNode.OrqlRefItem parse(String orql) {
        Parser parser = new Parser(ExecutorInstance.getInstance().configuration.getSchemaManager());
        return parser.parse(orql).getRoot();
    }

    @Test
    public void testQuerySimple() {
        OrqlNode.OrqlRefItem item = parse("user: {id, name}");
        Assert.assertEquals("user", item.getName());
    }

}
