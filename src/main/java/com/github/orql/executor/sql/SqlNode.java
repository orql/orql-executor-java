package com.github.orql.executor.sql;

import com.github.orql.executor.ExpOp;

import java.util.List;

public abstract class SqlNode {

    /**
     * 数据库结构操作
     */
    public static abstract class SqlDDL extends SqlNode {

    }

    public static class SqlCreateTable extends SqlDDL {

    }

    public static class SqlDropTable extends SqlDDL {

    }

    public static class SqlCreateColumn extends SqlDDL {

    }

    public static class SqlDropColumn extends SqlDDL {

    }

    /**
     * 数据库数据操作
     */
    public static abstract class SqlDML extends SqlNode {

    }

    public static class SqlInsert extends SqlDML {

        private String table;

        private List<SqlColumn> columns;

        private List<SqlParam> params;

        public String getTable() {
            return table;
        }

        public List<SqlColumn> getColumns() {
            return columns;
        }

        public List<SqlParam> getParams() {
            return params;
        }

        public SqlInsert(String table, List<SqlColumn> columns, List<SqlParam> params) {
            this.table = table;
            this.columns = columns;
            this.params = params;
        }
    }

    public static class SqlDelete extends SqlDML {

        private String table;

        private SqlExp where;

        public SqlDelete(String table, SqlExp where) {
            this.table = table;
            this.where = where;
        }

        public String getTable() {
            return table;
        }

        public SqlExp getWhere() {
            return where;
        }
    }

    public static class SqlUpdate extends SqlDML {

        private String table;

        private SqlExp where;

        private List<SqlColumn> sets;

        public SqlUpdate(String table, SqlExp where, List<SqlColumn> sets) {
            this.table = table;
            this.where = where;
            this.sets = sets;
        }

        public String getTable() {
            return table;
        }

        public SqlExp getWhere() {
            return where;
        }

        public List<SqlColumn> getSets() {
            return sets;
        }
    }

    public static class SqlQuery extends SqlDML {

        private List<SqlColumn> select;

        private SqlForm from;

        private List<SqlExp> where;

        private List<SqlJoin> joins;

        private SqlPage page;

        private List<SqlOrder> orders;

        public SqlQuery(List<SqlColumn> select, SqlForm from, List<SqlExp> where, List<SqlJoin> joins, List<SqlOrder> orders, SqlPage page) {
            this.select = select;
            this.from = from;
            this.where = where;
            this.joins = joins;
            this.orders = orders;
            this.page = page;
        }

        public List<SqlColumn> getSelect() {
            return select;
        }

        public SqlForm getFrom() {
            return from;
        }

        public List<SqlExp> getWhere() {
            return where;
        }

        public List<SqlJoin> getJoins() {
            return joins;
        }

        public SqlPage getPage() {
            return page;
        }

        public List<SqlOrder> getOrders() {
            return orders;
        }
    }

    public abstract static class SqlForm {
    }

    /**
     * from table as alias
     */
    public static class SqlTableForm extends SqlForm {

        private SqlTable table;

        public SqlTableForm(SqlTable table) {
            this.table = table;
        }

        public SqlTable getTable() {
            return table;
        }
    }

    /**
     * 嵌套查询
     * select * from (select * from table) as alias
     */
    public static class SqlInnerFrom extends SqlForm {

        private SqlQuery query;

        public SqlInnerFrom(SqlQuery query) {
            this.query = query;
        }

        public SqlQuery getQuery() {
            return query;
        }
    }

    public static class SqlTable {

        private String name;

        private String alias;

        public SqlTable(String name) {
            this.name = name;
        }

        public SqlTable(String name, String alias) {
            this.name = name;
            this.alias = alias;
        }

        public String getName() {
            return name;
        }

        public String getAlias() {
            return alias;
        }
    }

    public static class SqlJoin {

        private String table;

        private String alias;

        private SqlJoinType type;

        private SqlExp on;

        public SqlJoin(String table, String childPath, SqlJoinType type, SqlExp on) {
            this.table = table;
            this.alias = childPath;
            this.type = type;
            this.on = on;
        }

        public String getTable() {
            return table;
        }

        public String getAlias() {
            return alias;
        }

        public SqlJoinType getType() {
            return type;
        }

        public SqlExp getOn() {
            return on;
        }
    }

    public static class SqlPage {

        private Long offset;

        private Integer limit;

        public SqlPage(Long offset, Integer limit) {
            this.offset = offset;
            this.limit = limit;
        }

        public Integer getLimit() {
            return limit;
        }

        public Long getOffset() {
            return offset;
        }

    }

    public static class SqlOrder {

        private String sort;

        private List<SqlColumn> columns;

        public SqlOrder(List<SqlColumn> columns, String sort) {
            this.columns = columns;
            this.sort = sort;
        }

        public String getSort() {
            return sort;
        }

        public List<SqlColumn> getColumns() {
            return columns;
        }
    }

    public static class SqlColumn {

        private String name;

        private String table;

        public SqlColumn(String name) {
            this.name = name;
        }

        public SqlColumn(String name, String table) {
            this.name = name;
            this.table = table;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTable() {
            return table;
        }

        public void setTable(String table) {
            this.table = table;
        }
    }

    public static class SqlCountColumn extends SqlColumn {

        public SqlCountColumn(String name) {
            super(name);
        }

        public SqlCountColumn(String name, String table) {
            super(name, table);
        }
    }

    public static class SqlExp {

    }

    public static class SqlNestExp extends SqlExp {

        private SqlExp exp;

        public SqlNestExp(SqlExp exp) {
            this.exp = exp;
        }

        public SqlExp getExp() {
            return exp;
        }
    }

    public static class SqlAndExp extends SqlExp {

        private SqlExp left;

        private SqlExp right;

        public SqlAndExp(SqlExp left, SqlExp right) {
            this.left = left;
            this.right = right;
        }

        public SqlExp getLeft() {
            return left;
        }

        public SqlExp getRight() {
            return right;
        }
    }

    public static class SqlOrExp extends SqlExp {

        private SqlExp left;

        private SqlExp right;

        public SqlOrExp(SqlExp left, SqlExp right) {
            this.left = left;
            this.right = right;
        }

        public SqlExp getLeft() {
            return left;
        }

        public SqlExp getRight() {
            return right;
        }
    }

    public static class SqlColumnExp extends SqlExp {

        private SqlColumn left;

        private ExpOp op;

        private SqlColumn rightColumn;

        private SqlParam rightParam;

        private Object rightValue;

        public SqlColumnExp(SqlColumn left, ExpOp op, SqlColumn right) {
            this.left = left;
            this.op = op;
            this.rightColumn = right;
        }

        public SqlColumnExp(SqlColumn left, ExpOp op, SqlParam right) {
            this.left = left;
            this.op = op;
            this.rightParam = right;
        }

        public SqlColumnExp(SqlColumn left, ExpOp op, Object right) {
            this.left = left;
            this.op = op;
            this.rightValue = right;
        }

        public SqlColumn getLeft() {
            return left;
        }

        public ExpOp getOp() {
            return op;
        }

        public Object getRightValue() {
            return rightValue;
        }

        public SqlParam getRightParam() {
            return rightParam;
        }

        public SqlColumn getRightColumn() {
            return rightColumn;
        }
    }

    public static class SqlParam {

        private String name;

        public SqlParam(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public enum SqlJoinType {
        Left,
        Inner;
    }
}