package com.github.orql.executor.orql;

import com.github.orql.executor.ExpOp;
import com.github.orql.executor.schema.Association;
import com.github.orql.executor.schema.Column;
import com.github.orql.executor.schema.Schema;
import com.github.orql.executor.schema.SchemaManager;
import com.github.orql.executor.orql.OrqlNode.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {

    private static Map<String, OrqlNode> caches = new HashMap<>();

    private Lexer lexer;

    private SchemaManager schemaManager;

    private Token token;

    public Parser(SchemaManager schemaManager) {
        this.schemaManager = schemaManager;
    }

    private String matchToken(TokenType type) {
        if (type != token.getType()) throw new SyntaxException("expect " + type.name() + " actual " + token.getType());
        String string = token.getValue();
        walk();
        return string;
    }

    private boolean isToken(TokenType type) {
        return token.getType() == type;
    }

    private boolean isString(String string) {
        return token.getValue().equals(string);
    }

    private void walk() {
        token = lexer.nextToken();
    }

    public OrqlNode parse(String orql) {
        if (caches.containsKey(orql)) return caches.get(orql);
        this.lexer = new Lexer(orql);
        this.token = this.lexer.nextToken();
        OrqlNode node = visitOrql();
        caches.put(orql, node);
        return node;
    }

    private OrqlNode visitOrql() {
        return new OrqlNode(visitRoot());
    }

    private List<OrqlItem> visitItems(Schema schema) {
        List<OrqlItem> items = new ArrayList<>();
        while (true) {
            boolean ignore = false;
            if (isToken(TokenType.NOT)) {
                walk();
                ignore = true;
            }
            OrqlNode.OrqlItem item = visitItem(schema, ignore);
            items.add(item);
            // ,
            if (! isToken(TokenType.COMMA)) break;
            this.walk();
        }
        return items;
    }

    private OrqlNode.OrqlRefItem visitRoot() {
        String name = matchToken(TokenType.NAME);
        Schema schema = schemaManager.getSchema(name);
        OrqlNode.OrqlExp where = visitWhere(schema);
        List<OrqlNode.OrqlItem> items = new ArrayList<>();
        if (isToken(TokenType.COLON)) {
            // :
            walk();
            if (this.isToken(TokenType.OPEN_CURLY)) {
                // {
                walk();
                items = this.visitItems(schema);
                // }
                this.matchToken(TokenType.CLOSE_CURLY);
            } else {
                throw new SyntaxException("expect {");
            }
        }
        return new OrqlNode.OrqlRefItem(name, schema, null, items, where);
    }

    private OrqlNode.OrqlItem visitItem(Schema parent, boolean ignore) {
        if (this.isToken(TokenType.ALL)) {
            this.walk();
            return new OrqlNode.OrqlAllItem();
        }
        String name = matchToken(TokenType.NAME);
        if (parent.containsColumn(name)) {
            Column column = parent.getColumn(name);
            return ignore ? new OrqlNode.OrqlIgnoreItem(column) : new OrqlNode.OrqlColumnItem(column);
        }
        if (parent.containsAssociation(name)) {
            Association association = parent.getAssociation(name);
            Schema ref = association.getRef();
            OrqlNode.OrqlExp where = visitWhere(ref);
            List<OrqlNode.OrqlItem> items = new ArrayList<>();
            if (isToken(TokenType.COLON)) {
                // :
                walk();
                if (this.isToken(TokenType.OPEN_CURLY)) {
                    // {
                    walk();
                    items = this.visitItems(ref);
                    // }
                    this.matchToken(TokenType.CLOSE_CURLY);
                } else {
                    throw new SyntaxException("expect {");
                }
            }
            return new OrqlNode.OrqlRefItem(name, ref, association, items, where);
        }
        throw new SyntaxException("schema " + parent.getName() + " not exist column " + name);
    }

    private OrqlNode.OrqlExp visitWhere(Schema schema) {
        OrqlNode.OrqlExp exp = null;
        if (isToken(TokenType.OPEN_PAREN) || isToken(TokenType.NAME)) {
            // 表达式以(或name开头
            exp = visitExp(schema);
        }
        return exp;
    }

    private OrqlNode.OrqlExp visitExp(Schema schema) {
        OrqlNode.OrqlExp tmp = visitExpTerm(schema);
        while (isToken(TokenType.OR)) {
            // ||
            this.walk();
            OrqlNode.OrqlExp exp = visitExp(schema);
            tmp = new OrqlNode.OrqlOrExp(tmp, exp);
        }
        return tmp;
    }

    private OrqlNode.OrqlExp visitExpTerm(Schema schema) {
        OrqlNode.OrqlExp tmp = visitFactor(schema);
        while (isToken(TokenType.AND)) {
            // &&
            this.walk();
            OrqlNode.OrqlExp term = visitExpTerm(schema);
            tmp = new OrqlNode.OrqlAndExp(tmp, term);
        }
        return tmp;
    }

    private OrqlNode.OrqlExp visitFactor(Schema schema) {
        if (isToken(TokenType.OPEN_PAREN)) {
            // (
            walk();
            OrqlNode.OrqlExp exp = visitExp(schema);
            this.matchToken(TokenType.CLOSE_PAREN);
            return new OrqlNode.OrqlNestExp(exp);
        }
        Column column = visitColumn(schema);
        ExpOp op = visitOp();
        if (isToken(TokenType.NAME)) {
            Column right = visitColumn(schema);
            return new OrqlNode.OrqlColumnExp(column, op, right);
        }
        if (isToken(TokenType.PARAM)) {
            String param = matchToken(TokenType.PARAM);
            return new OrqlNode.OrqlColumnExp(column, op, param);
        }
        Object value = visitValue();
        return new OrqlNode.OrqlColumnExp(column, op, value);
    }

    private Object visitValue() {
        if (isToken(TokenType.INT)) {
            Integer value = Integer.valueOf(token.getValue());
            walk();
            return value;
        }
        if (isToken(TokenType.FLOAT)) {
            Float value = Float.valueOf(token.getValue());
            walk();
            return value;
        }
        if (isToken(TokenType.BOOLEAN)) {
            Boolean value = token.getValue().equals("true");
            this.walk();
            return value;
        }
        if (isToken(TokenType.STRING)) {
            String value = token.getValue();
            walk();
            return value;
        }
        if (isToken(TokenType.NULL)) {
            walk();
            return new OrqlNode.NullValue();
        }
        throw new SyntaxException("expect value");
    }

    private ExpOp visitOp() {
        if (isToken(TokenType.EQ)) {
            this.walk();
            return ExpOp.Eq;
        }
        if (isToken(TokenType.GT)) {
            this.walk();
            return ExpOp.Gt;
        }
        if (isToken(TokenType.GE)) {
            this.walk();
            return ExpOp.Ge;
        }
        if (isToken(TokenType.LT)) {
            this.walk();
            return ExpOp.Lt;
        }
        if (isToken(TokenType.LE)) {
            this.walk();
            return ExpOp.Le;
        }
        if (isToken(TokenType.LIKE)) {
            this.walk();
            return ExpOp.Like;
        }
        if (isToken(TokenType.NE)) {
            this.walk();
            return ExpOp.Ne;
        }
        throw new SyntaxException("expect op");
    }

    private Column visitColumn(Schema schema) {
        String name = matchToken(TokenType.NAME);
        Column column = schema.getColumn(name);
        if (column == null) throw new SyntaxException("schema " + schema.getName() + " not exist column " + name);
        return column;
    }

}
