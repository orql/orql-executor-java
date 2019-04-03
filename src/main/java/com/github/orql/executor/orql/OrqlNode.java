package com.github.orql.executor.orql;

import com.github.orql.executor.ExpOp;
import com.github.orql.executor.schema.Association;
import com.github.orql.executor.schema.Column;
import com.github.orql.executor.schema.Schema;

import java.util.List;

public class OrqlNode {

    private OrqlOp op;

    private OrqlRefItem root;

    public OrqlNode(OrqlOp op, OrqlRefItem root) {
        this.op = op;
        this.root = root;
    }

    public OrqlOp getOp() {
        return op;
    }

    public OrqlRefItem getRoot() {
        return root;
    }

    public enum OrqlOp {
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

        OrqlOp(String name) {
            this.name = name;
        }

        public static OrqlOp fromName(String name) {
            for (OrqlOp op : OrqlOp.values()) {
                if (op.name.equals(name)) return op;
            }
            return null;
        }
    }

    public static class OrqlItem {

        private String name;

        public String getName() {
            return name;
        }

        public OrqlItem(String name) {
            this.name = name;
        }
    }

    public static class OrqlRefItem extends OrqlItem {

        private Schema ref;

        private Association association;

        private OrqlWhere where;

        private List<OrqlItem> children;

        public OrqlRefItem(String name, Schema ref, Association association, List<OrqlItem> children, OrqlWhere where) {
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

        public OrqlWhere getWhere() {
            return where;
        }

        public List<OrqlItem> getChildren() {
            return children;
        }
    }

    public static class OrqlObjectItem extends OrqlRefItem {

        public OrqlObjectItem(String name, Schema ref, Association association, List<OrqlItem> children, OrqlWhere where) {
            super(name, ref, association, children, where);
        }
    }

    public static class OrqlArrayItem extends OrqlRefItem {

        public OrqlArrayItem(String name, Schema ref, Association association, List<OrqlItem> children, OrqlWhere where) {
            super(name, ref, association, children, where);
        }
    }

    public static class OrqlColumnItem extends OrqlItem {

        private Column column;

        public OrqlColumnItem(Column column) {
            super(column.getName());
            this.column = column;
        }

        public Column getColumn() {
            return column;
        }

    }

    public static class OrqlAllItem extends OrqlItem {

        public OrqlAllItem() {
            super("");
        }
    }

    public static class OrqlWhere {

        private OrqlExp exp;

        private List<OrqlOrder> orders;

        public OrqlWhere(OrqlExp exp, List<OrqlOrder> orders) {
            this.exp = exp;
            this.orders = orders;
        }

        public OrqlExp getExp() {
            return exp;
        }

        public List<OrqlOrder> getOrders() {
            return orders;
        }
    }

    public static class OrqlExp {

    }

    public static class OrqlNestExp extends OrqlExp {

        private OrqlExp exp;

        public OrqlNestExp(OrqlExp exp) {
            this.exp = exp;
        }

        public OrqlExp getExp() {
            return exp;
        }
    }

    public static class OrqlAndExp extends OrqlExp {

        private OrqlExp left;

        private OrqlExp right;

        public OrqlAndExp(OrqlExp left, OrqlExp right) {
            this.left = left;
            this.right = right;
        }

        public OrqlExp getLeft() {
            return left;
        }

        public OrqlExp getRight() {
            return right;
        }
    }

    public static class OrqlOrExp extends OrqlExp {

        private OrqlExp left;

        private OrqlExp right;

        public OrqlOrExp(OrqlExp left, OrqlExp right) {
            this.left = left;
            this.right = right;
        }

        public OrqlExp getLeft() {
            return left;
        }

        public OrqlExp getRight() {
            return right;
        }

    }

    public static class OrqlNotExp extends OrqlExp {

        private OrqlExp exp;

        public OrqlExp getExp() {
            return exp;
        }

        public void setExp(OrqlExp exp) {
            this.exp = exp;
        }
    }

    public static class OrqlColumnExp extends OrqlExp {

        private Column left;

        private ExpOp op;

        private Column rightColumn;

        private String rightParam;

        private Object rightValue;

        public OrqlColumnExp(Column left, ExpOp op, Column right) {
            this.left = left;
            this.op = op;
            this.rightColumn = right;
        }

        public OrqlColumnExp(Column left, ExpOp op, String right) {
            this.left = left;
            this.op = op;
            this.rightParam = right;
        }

        public OrqlColumnExp(Column left, ExpOp op, Object right) {
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

    public static class OrqlOrder {

        /**
         * asc
         * desc
         */
        private String sort;

        private List<Column> columns;

        public OrqlOrder(List<Column> columns, String sort) {
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