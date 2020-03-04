package com.github.orql.executor;

import com.github.orql.core.QueryOrder;
import com.github.orql.core.schema.SchemaManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryBuilder {

    private Long offset;

    private Integer limit;

    private Session session;

    private String orql;

    private Map<String, Object> params = new HashMap<>();

    private List<QueryOrder> orders;

    private SchemaManager schemaManager;

    public QueryBuilder(Session session, SchemaManager schemaManager) {
        this.session = session;
        this.schemaManager = schemaManager;
    }

    public QueryBuilder offset(Long offset) {
        this.offset = offset;
        return this;
    }

    public QueryBuilder limit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public QueryBuilder orql(String orql) {
        this.orql = orql;
        return this;
    }

    public QueryBuilder param(String name, Object value) {
        this.params.put(name, value);
        return this;
    }

    public QueryBuilder params(Map<String, Object> params) {
        this.params = params;
        return this;
    }

    public QueryBuilder order(String[] columns, String sort) {
        if (this.orders == null) {
            this.orders = new ArrayList<>();
        }
        QueryOrder order = new QueryOrder();
        for (String column : columns) {
            order.addColumn(column);
        }
        order.setSort(sort);
        return this;
    }

    public QueryBuilder order(String[] columns) {
        return this.order(columns, "asc");
    }

    public <T> List<T> queryAll() {
//        if (page != null && size != null) {
//            offset = (long) (page - 1) * size;
//            limit = size;
//        }
//        Object result = session.query(orql, params, offset, limit, this.orders);
//        List<T> list = new ArrayList<>();
//        for (Object child : (List) result) {
//            list.add((T) MapBean.toBean((Map) child, clazz));
//        }
//        return list;
        return session.queryAll(this.orql, this.params, this.limit, this.offset, this.orders);
    }

    public <T> T queryOne() {
       return session.queryOne(this.orql, this.params, this.offset, this.orders);
    }

    public Long count() {
        return (Long) session.count(orql, params);
    }

}
