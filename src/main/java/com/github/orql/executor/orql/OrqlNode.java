package com.github.orql.executor.orql;

import com.github.orql.executor.ExpOp;
import com.github.orql.executor.schema.Association;
import com.github.orql.executor.schema.Column;
import com.github.orql.executor.schema.Schema;

import java.util.List;

public class OrqlNode {

    private ReqlOp op;

    private ReqlRefItem root;

    public OrqlNode(ReqlOp op, ReqlRefItem root) {
        this.op = op;
        this.root = root;
    }

    public ReqlOp getOp() {
        return op;
    }

    public ReqlRefItem getRoot() {
        return root;
    }

    public enum ReqlOp {
        Add("add"),
        Delete("delete"),
        Update("update"),
        Query("query"),
        Count("count"),
        Max("max"),
        Min("min"),
        Avg("avg"),
        Group("group");

        private String name;

        ReqlOp(String name) {
            this.name = name;
        }

        public static ReqlOp fromName(String name) {
            for (ReqlOp op : ReqlOp.values()) {
                if (op.name.equals(name)) return op;
            }
            return null;
        }
    }

    public static class ReqlItem {

        private String name;

        public String getName() {
            return name;
        }

        public ReqlItem(String name) {
            this.name = name;
        }
    }

    public static class ReqlRefItem extends ReqlItem {

        private Schema ref;

        private Association association;

        private ReqlWhere where;

        private List<ReqlItem> children;

        public ReqlRefItem(String name, Schema ref, Association association, List<ReqlItem> children, ReqlWhere where) {
            super(name);
            this.ref = ref;
            this.association = association;
            this.children = children;
            this.where = where;
        }

        public Schema getRef() {
            return ref;
        }

        public Association getAssociation() {
            return association;
        }

        public ReqlWhere getWhere() {
            return where;
        }

        public List<ReqlItem> getChildren() {
            return children;
        }
    }

    public static class ReqlObjectItem extends ReqlRefItem {

        public ReqlObjectItem(String name, Schema ref, Association association, List<ReqlItem> children, ReqlWhere where) {
            super(name, ref, association, children, where);
        }
    }

    public static class ReqlArrayItem extends ReqlRefItem {

        public ReqlArrayItem(String name, Schema ref, Association association, List<ReqlItem> children, ReqlWhere where) {
            super(name, ref, association, children, where);
        }
    }

    public static class ReqlColumnItem extends ReqlItem {

        private Column column;

        public ReqlColumnItem(Column column) {
            super(column.getName());
            this.column = column;
        }

        public Column getColumn() {
            return column;
        }

    }

    public static class ReqlAllItem extends ReqlItem {

        public ReqlAllItem() {
            super("");
        }
    }

    public static class ReqlWhere {

        private ReqlExp exp;

        private List<ReqlOrder> orders;

        public ReqlWhere(ReqlExp exp, List<ReqlOrder> orders) {
            this.exp = exp;
            this.orders = orders;
        }

        public ReqlExp getExp() {
            return exp;
        }

        public List<ReqlOrder> getOrders() {
            return orders;
        }
    }

    public static class ReqlExp {

    }

    public static class ReqlNestExp extends ReqlExp {

        private ReqlExp exp;

        public ReqlNestExp(ReqlExp exp) {
            this.exp = exp;
        }

        public ReqlExp getExp() {
            return exp;
        }
    }

    public static class ReqlAndExp extends ReqlExp {

        private ReqlExp left;

        private ReqlExp right;

        public ReqlAndExp(ReqlExp left, ReqlExp right) {
            this.left = left;
            this.right = right;
        }

        public ReqlExp getLeft() {
            return left;
        }

        public ReqlExp getRight() {
            return right;
        }
    }

    public static class ReqlOrExp extends ReqlExp {

        private ReqlExp left;

        private ReqlExp right;

        public ReqlOrExp(ReqlExp left, ReqlExp right) {
            this.left = left;
            this.right = right;
        }

        public ReqlExp getLeft() {
            return left;
        }

        public ReqlExp getRight() {
            return right;
        }

    }

    public static class ReqlNotExp extends ReqlExp {

        private ReqlExp exp;

        public ReqlExp getExp() {
            return exp;
        }

        public void setExp(ReqlExp exp) {
            this.exp = exp;
        }
    }

    public static class ReqlColumnExp extends ReqlExp {

        private Column left;

        private ExpOp op;

        private Column rightColumn;

        private String rightParam;

        private Object rightValue;

        public ReqlColumnExp(Column left, ExpOp op, Column right) {
            this.left = left;
            this.op = op;
            this.rightColumn = right;
        }

        public ReqlColumnExp(Column left, ExpOp op, String right) {
            this.left = left;
            this.op = op;
            this.rightParam = right;
        }

        public ReqlColumnExp(Column left, ExpOp op, Object right) {
            this.left = left;
            this.op = op;
            this.rightValue = right;
        }

        public Column getLeft() {
            return left;
        }

        public ExpOp getOp() {
            return op;
        }

        public Column getRightColumn() {
            return rightColumn;
        }

        public Object getRightValue() {
            return rightValue;
        }

        public String getRightParam() {
            return rightParam;
        }
    }

    public static class ReqlOrder {

        /**
         * asc
         * desc
         */
        private String sort;

        private List<Column> columns;

        public ReqlOrder(List<Column> columns, String sort) {
            this.columns = columns;
            this.sort = sort;
        }

        public String getSort() {
            return sort;
        }

        public List<Column> getColumns() {
            return columns;
        }
    }

    public static class NullValue {

    }

}