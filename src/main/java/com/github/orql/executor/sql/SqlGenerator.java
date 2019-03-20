package com.github.orql.executor.sql;

import com.github.orql.executor.ExpOp;
import com.github.orql.executor.exception.SqlGenException;
import com.github.orql.executor.orql.OrqlNode;
import com.github.orql.executor.sql.SqlNode.*;
import com.github.orql.executor.util.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SqlGenerator {

    public String gen(SqlNode tree) {
        if (tree instanceof SqlQuery) return genQuery((SqlQuery) tree);
        if (tree instanceof SqlInsert) return genAdd((SqlInsert) tree);
        if (tree instanceof SqlDelete) return genDelete((SqlDelete) tree);
        if (tree instanceof SqlUpdate) return genUpdate((SqlUpdate) tree);
        throw new SqlGenException();
    }

    private String genQuery(SqlQuery sqlQuery) {
        return "select " +
                genSelect(sqlQuery.getSelect()) +
                genFrom(sqlQuery.getFrom()) +
                genJoins(sqlQuery.getJoins()) +
                genWhere(sqlQuery.getWhere()) +
                genOrders(sqlQuery.getOrders()) +
                genPage(sqlQuery.getPage());
    }

    private String genSelect(List<SqlColumn> select) {
        return select.stream()
                .map(this::genSelectColumn)
                .collect(Collectors.joining(", "));
    }

    private String genSelectColumn(SqlColumn sqlColumn) {
        if (sqlColumn instanceof SqlCountColumn) {
            return "count(" + sqlColumn.getTable() + "." + sqlColumn.getName() + ")";
        }
        if (sqlColumn.getTable() != null) {
            return sqlColumn.getTable() + "." + sqlColumn.getName() + " as " + sqlColumn.getTable() + "_" + sqlColumn.getName();
        }
        return sqlColumn.getName();
    }

    private String genFrom(SqlForm sqlForm) {
        if (sqlForm instanceof SqlTableForm) {
            return genFromSqlTable(((SqlTableForm) sqlForm).getTable());
        }
        SqlQuery innerQuery = ((SqlInnerFrom) sqlForm).getQuery();
        // 只支持一层嵌套
        return " from (" + genQuery(innerQuery) + ") as " + ((SqlTableForm) innerQuery.getFrom()).getTable().getName();
    }

    private String genFromSqlTable(SqlTable sqlTable) {
        if (sqlTable.getAlias() == null) {
            return " from " + sqlTable.getName();
        }
        return " from " + sqlTable.getName() + " as " + sqlTable.getAlias();
    }

    private String genJoins(List<SqlJoin> joins) {
        if (joins.isEmpty()) {
            return "";
        }
        List<String> joinSqlList = new ArrayList<>(joins.size());
        for (SqlJoin join : joins) {
            joinSqlList.add(genJoin(join));
        }
        return Strings.join(joinSqlList, " ");
    }

    private String genJoin(SqlJoin join) {
        String joinSql = join.getType() == SqlJoinType.Inner ? " inner join " : " left join ";
        String expSql = genExp(join.getOn());
        return joinSql + join.getTable() + " as "  + join.getAlias() + " on " + expSql;
    }

    private String genPage(SqlPage page) {
        if (page == null || page.getLimit() == null) return "";
        return page.getOffset() != null ? " limit " + page.getOffset() + ", " + page.getLimit() : " limit " + page.getLimit();
    }

    /**
     * 生成排序
     * @param orders
     * @return
     */
    private String genOrders(List<SqlOrder> orders) {
        if (orders == null || orders.size() == 0) return "";
        StringBuilder builder = new StringBuilder();
        builder.append(" order by ");
        for (SqlOrder order : orders) {
            for (SqlColumn column : order.getColumns()) {
                builder.append(genColumn(column)).append(", ");
            }
            // 删掉最后一个,
            builder.setCharAt(builder.length() - 2, ' ');
            builder.append(order.getSort());
        }
        return builder.toString();
    }

    private String genWhere(List<SqlExp> where) {
        if (where.isEmpty()) {
            return "";
        }
        List<String> expSqlList = new ArrayList<>(where.size());
        for (SqlExp exp : where) {
            expSqlList.add(genExp(exp));
        }
        return " where " + Strings.join(expSqlList, " and ");
    }

    private String genAdd(SqlInsert insert) {
        return "insert into " + insert.getTable() +
                "(" + insert.getColumns().stream().map(SqlColumn::getName).collect(Collectors.joining(", ")) + ")" +
                " values " +
                "(" + insert.getParams().stream().map(param -> "$" + param.getName()).collect(Collectors.joining(", ")) + ")";
    }

    private String genDelete(SqlDelete delete) {
        return "delete from " +
                delete.getTable() +
                " where " + genExp(delete.getWhere());
    }

    private String genUpdate(SqlUpdate update) {
        return "update " + update.getTable() +
                " set " +
                update.getSets().stream().map(set -> set.getName() + " = $" + set.getName()).collect(Collectors.joining(", ")) +
                " where " + genExp(update.getWhere());
    }

    private String genExp(SqlExp exp) {
        if (exp instanceof SqlAndExp) {
            return genExp(((SqlAndExp) exp).getLeft()) + " and " + genExp(((SqlAndExp) exp).getRight());
        }
        if (exp instanceof SqlOrExp) {
            return genExp(((SqlOrExp) exp).getLeft()) + " or " + genExp(((SqlOrExp) exp).getRight());
        }
        if (exp instanceof SqlNestExp) {
            return "(" + genExp(((SqlNestExp) exp).getExp()) + ")";
        }
        if (exp instanceof SqlColumnExp) {
            return genColumnExp((SqlColumnExp) exp);
        }
        throw new SqlGenException();
    }

    private String genColumnExp(SqlColumnExp columnExp)  {
        String leftSql = genColumn(columnExp.getLeft());
        String opStr = genExpOp(columnExp.getOp());
        if (columnExp.getRightColumn() != null) {
            return leftSql + " " + opStr + " " + genColumn(columnExp.getRightColumn());
        }
        if (columnExp.getRightParam() != null) {
            return leftSql + " " + opStr + " $" + columnExp.getRightParam().getName();
        }
        if (columnExp.getRightValue() instanceof OrqlNode.NullValue) {

        }
        if (columnExp.getRightValue() != null) {
            if (columnExp.getRightValue() instanceof OrqlNode.NullValue) {
                return leftSql + (columnExp.getOp() == ExpOp.Eq ? " is " : " is not ") + "null";
            }
            return leftSql + " " + opStr + " " + genSqlValue(columnExp.getRightValue());
        }
        throw new SqlGenException();
    }

    private String genColumn(SqlColumn column) {
        return column.getTable() != null
                ? column.getTable() + "." + column.getName()
                : column.getName();
    }

    private String genExpOp(ExpOp op) {
        return op.text();
    }

    private String genSqlValue(Object value) {
        if (value instanceof String) return "'" + value + "'";
        return value.toString();
    }
}