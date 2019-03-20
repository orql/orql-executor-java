package com.github.orql.executor.sql;

import com.github.orql.executor.*;
import com.github.orql.executor.exception.SqlGenException;
import com.github.orql.executor.orql.OrqlNode;
import com.github.orql.executor.orql.OrqlNode.*;
import com.github.orql.executor.schema.*;
import com.github.orql.executor.sql.SqlNode.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class OrqlToSql {

    private Logger logger = LoggerFactory.getLogger(OrqlToSql.class);

    private SqlGenerator sqlGenerator = new SqlGenerator();

    private static class QueryWrapper {
        OrqlNode.ReqlRefItem item;
        String path;
        QueryWrapper(OrqlNode.ReqlRefItem item, String path) {
            this.item = item;
            this.path = path;
        }
    }

    public String toAdd(ReqlRefItem root) {
        List<SqlColumn> columns = new ArrayList<>();
        List<SqlParam> params = new ArrayList<>();
        for (ReqlItem item : root.getChildren()) {
            if (item instanceof ReqlColumnItem) {
                Column columnItem = ((ReqlColumnItem) item).getColumn();
                columns.add(new SqlColumn(columnItem.getField()));
                params.add(new SqlParam(columnItem.getName()));
            } else if (item instanceof ReqlRefItem) {
                Association association = ((ReqlRefItem) item).getAssociation();
                switch (association.getType()) {
                    case BelongsTo:
                        columns.add(new SqlColumn(association.getRefKey()));
                        params.add(new SqlParam(association.getRefKey()));
                        break;
                }
            }
        }
        SqlInsert insert = new SqlInsert(root.getRef().getTable(), columns, params);
        return sqlGenerator.gen(insert);
    }

    public String toDelete(ReqlRefItem root) {
        SqlExp exp = genExp(root.getWhere().getExp(), root.getRef().getTable());
        SqlDelete delete = new SqlDelete(root.getRef().getTable(), exp);
        return sqlGenerator.gen(delete);
    }

    public String toUpdate(ReqlRefItem root) {
        SqlExp exp = genExp(root.getWhere().getExp(), root.getRef().getTable());
        List<SqlColumn> sets = new ArrayList<>();
        for (ReqlItem item : root.getChildren()) {
            if (item instanceof ReqlColumnItem) {
                sets.add(new SqlColumn(((ReqlColumnItem) item).getColumn().getField()));
            } else if (item instanceof ReqlRefItem) {
                Association association = ((ReqlRefItem) item).getAssociation();
                switch (association.getType()) {
                    case BelongsTo:
                        // user belongsTo role
                        // roleId = #role.id
                        sets.add(new SqlColumn(((ReqlRefItem) item).getAssociation().getRefKey()));
                        break;
                }
            }
        }
        SqlUpdate update = new SqlUpdate(root.getRef().getTable(), exp, sets);
        return sqlGenerator.gen(update);
    }

    public String toQuery(ReqlOp op, ReqlRefItem root, SqlPage sqlPage) {
        sqlPage = sqlPage == null ? new SqlPage(null, null) : sqlPage;
        Schema rootSchema = root.getRef();
        String table = rootSchema.getTable();
        List<SqlJoin> joins = new ArrayList<>();
        List<SqlExp> where = new ArrayList<>();
        //根节点exp
        SqlExp rootExp = null;
        List<SqlColumn> select = new ArrayList<>();
        // 排序
        List<SqlOrder> orders = new ArrayList<>();
        // 根节点排序
        List<SqlOrder> rootOrders = new ArrayList<>();
        Stack<QueryWrapper> queryStack = new Stack<>();
        queryStack.push(new QueryWrapper(root, table));
        //存在数组类型关联
        boolean hasArrayRef = false;

        while (! queryStack.isEmpty()) {
            QueryWrapper queryWrapper = queryStack.pop();
            ReqlRefItem currentItem = queryWrapper.item;
            String currentPath = queryWrapper.path;
            Schema currentSchema = currentItem.getRef();
            Column idColumn = currentSchema.getIdColumn();
            // 是否有主键
            boolean hasId = false;
            // 是否有select
            boolean hasSelect = false;
            if (currentItem.getWhere() != null) {
                if (currentItem.getWhere().getExp() != null) {
                    SqlExp exp = genExp(currentItem.getWhere().getExp(), currentPath);
                    if (currentPath.equals(table)) {
                        // root where
                        rootExp = exp;
                    } else {
                        where.add(exp);
                    }
                }
                if (currentItem.getWhere().getOrders() != null) {
                    // 添加排序
                    for (ReqlOrder reqlOrder : currentItem.getWhere().getOrders()) {
                        List<SqlColumn> columns = new ArrayList<>();
                        for (Column column : reqlOrder.getColumns()) {
                            columns.add(new SqlColumn(column.getField(), currentPath));
                        }
                        SqlOrder sqlOrder = new SqlOrder(columns, reqlOrder.getSort());
                        // 分开存
                        if (currentPath.equals(table)) {
                            rootOrders.add(sqlOrder);
                        }
                        // 嵌套内外都要order
                        orders.add(sqlOrder);
                    }
                }
            }
            for (ReqlItem child : currentItem.getChildren()) {
                hasSelect = true;
                if (child instanceof ReqlRefItem) {
                    Association association = currentSchema.getAssociation(child.getName());

                    if (association.getType() == Association.Type.HasMany || association.getType() == Association.Type.BelongsToMany) {
                       if (! ((ReqlRefItem) child).getChildren().isEmpty()) {
                           //存在数组类型关联
                           hasArrayRef = true;
                       }
                    }
                    Schema childSchema = ((ReqlRefItem) child).getRef();
                    Column childIdColumn = childSchema.getIdColumn();
                    String childPath = currentPath + "_" + child.getName();
                    //入栈
                    queryStack.push(new QueryWrapper((ReqlRefItem) child, childPath));
                    SqlJoinType joinType = association.isRequired() ? SqlJoinType.Inner : SqlJoinType.Left;
                    Association.Type type = association.getType();
                    if (type == Association.Type.HasMany) {
                        // role hasMany user
                        // user.roleId = role.id
                        SqlExp on = new SqlColumnExp(
                                new SqlColumn(association.getRefKey(), childPath),
                                ExpOp.Eq,
                                new SqlColumn(childIdColumn.getField(), currentPath));
                        joins.add(new SqlJoin(childSchema.getTable(), childPath, joinType, on));
                    } else if (type == Association.Type.HasOne) {
                        // user hasOne info
                        // info.userId = user.id
                        SqlExp on = new SqlColumnExp(
                                new SqlColumn(association.getRefKey(), childPath),
                                ExpOp.Eq,
                                new SqlColumn(childIdColumn.getField(), currentPath));
                        joins.add(new SqlJoin(childSchema.getTable(), childPath, joinType, on));
                    } else if (type == Association.Type.BelongsTo) {
                        // user belongsTo role
                        // role.id = user.roleId
                        SqlExp on = new SqlColumnExp(
                                new SqlColumn(association.getRefId().getField(), childPath),
                                ExpOp.Eq,
                                new SqlColumn(association.getRefKey(), currentPath));
                        joins.add(new SqlJoin(childSchema.getTable(), childPath, joinType, on));
                    } else if (type == Association.Type.BelongsToMany) {
                        // post belongsToMany tag, middle postTags
                        // postTags.postId = post.id
                        // postTags.tagId = tag.id
                        Schema targetSchema = association.getCurrent();
                        Schema foreign = association.getRef();
                        String middlePath = childPath + "_" + association.getMiddle();
                        SqlExp leftOn = new SqlColumnExp(
                                new SqlColumn(association.getMiddleKey(), middlePath),
                                ExpOp.Eq,
                                new SqlColumn(childIdColumn.getField(), currentPath));
                        joins.add(new SqlJoin(association.getMiddle(), middlePath, joinType, leftOn));
                        SqlExp rightOn = new SqlColumnExp(
                                new SqlColumn(targetSchema.getIdColumn().getField(), childPath),
                                ExpOp.Eq,
                                new SqlColumn(association.getRefMiddleKey(), middlePath));
                        joins.add(new SqlJoin(foreign.getTable(), childPath, joinType, rightOn));
                    }
                } else {
                    if (child.getName().equals(idColumn.getName())) {
                        hasId = true;
                    }
                    if (op != ReqlOp.Count) {
                        if (child instanceof ReqlColumnItem) {
                            ReqlColumnItem columnItem = (ReqlColumnItem) child;
                            select.add(new SqlColumn(columnItem.getColumn().getField(), currentPath));
                        } else {
                            select.add(new SqlColumn(child.getName(), currentPath));
                        }
                    }
                }
            }
            if (! hasId) {
                if (op != ReqlOp.Count && hasSelect) {
                    //插入id
                    select.add(new SqlColumn(idColumn.getField(), currentPath));
                }
            }
        }
        //FIXME 逻辑太乱，后续修复
        SqlQuery query;
        if (op == ReqlOp.Count) {
            //分页
            select.add(new SqlCountColumn(rootSchema.getIdField(), table));
            if (rootExp != null) where.add(0, rootExp);
            SqlForm from = new SqlTableForm(new SqlTable(table, table));
            query = new SqlQuery(select, from, where, joins, orders, sqlPage);
        } else if (hasArrayRef && sqlPage.getLimit() != null) {
            //嵌套分页查询
            List<SqlColumn> innerSelect = Collections.singletonList(new SqlColumn("*"));
            List<SqlExp> innerWhere = rootExp != null ? Collections.singletonList(rootExp) : new ArrayList<>();
            SqlTableForm innerFrom = new SqlTableForm(new SqlTable(table));
            SqlForm from = new SqlInnerFrom(new SqlQuery(innerSelect, innerFrom, innerWhere, new ArrayList<>(), rootOrders, sqlPage));
            query = new SqlQuery(select, from, where, joins, orders,  null);
        } else if (! hasArrayRef && sqlPage.getLimit() == null && root instanceof ReqlObjectItem) {
            //无分页，单个查询，而且没有数组类型关联查询
            if (rootExp != null) where.add(0, rootExp);
            SqlForm from = new SqlTableForm(new SqlTable(table, table));
            sqlPage = new SqlPage(null, 1);
            query = new SqlQuery(select, from, where, joins, orders, sqlPage);
        } else {
            if (rootExp != null) where.add(0, rootExp);
            SqlForm from = new SqlTableForm(new SqlTable(table, table));
            query = new SqlQuery(select, from, where, joins, orders, sqlPage);
        }
        return sqlGenerator.gen(query);
    }

    private SqlExp genExp(ReqlExp reqlExp, String path) {
        if (reqlExp instanceof ReqlAndExp) {
            return new SqlAndExp(
                    genExp(((ReqlAndExp) reqlExp).getLeft(), path),
                    genExp(((ReqlAndExp) reqlExp).getRight(), path));
        }
        if (reqlExp instanceof ReqlOrExp) {
            return new SqlOrExp(
                    genExp(((ReqlOrExp) reqlExp).getLeft(), path),
                    genExp(((ReqlOrExp) reqlExp).getRight(), path));
        }
        if (reqlExp instanceof ReqlNestExp) {
            return new SqlNestExp(genExp(((ReqlNestExp) reqlExp).getExp(), path));
        }
        if (reqlExp instanceof ReqlColumnExp) {
            return genExpColumn((ReqlColumnExp) reqlExp, path);
        }
        throw new SqlGenException();
    }

    private SqlExp genExpColumn(ReqlColumnExp reqlColumnExp, String path) {
        SqlColumn left = new SqlColumn(reqlColumnExp.getLeft().getField(), path);
        if (reqlColumnExp.getRightColumn() != null) {
            SqlColumn right = new SqlColumn(reqlColumnExp.getRightColumn().getField(), path);
            return new SqlColumnExp(left, reqlColumnExp.getOp(), right);
        }
        if (reqlColumnExp.getRightParam() != null) {
            SqlParam right = new SqlParam(reqlColumnExp.getRightParam());
            return new SqlColumnExp(left, reqlColumnExp.getOp(), right);
        }
        Object right = reqlColumnExp.getRightValue();
        return new SqlColumnExp(left, reqlColumnExp.getOp(), right);
    }
}