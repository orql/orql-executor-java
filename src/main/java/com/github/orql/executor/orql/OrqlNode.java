package com.github.orql.executor.orql;

import com.github.orql.executor.ExpOp;
import com.github.orql.executor.schema.Association;
import com.github.orql.executor.schema.Column;
import com.github.orql.executor.schema.Schema;

import java.util.List;

public class OrqlNode {

    private OrqlRefItem root;

    public OrqlNode(OrqlRefItem root) {
        this.root = root;
    }

    public OrqlRefItem getRoot() {
        return root;
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

        private OrqlExp where;

        private List<OrqlItem> children;

        public OrqlRefItem(String name, Schema ref, Association association, List<OrqlItem> children, OrqlExp where) {
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

        public OrqlExp getWhere() {
            return where;
        }

        public List<OrqlItem> getChildren() {
            return children;
        }

        public Boolean isArray() {
            return association.getType() == Association.Type.HasMany
                    || association.getType() == Association.Type.BelongsToMany;
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

    public static class NullValue {

    }

}