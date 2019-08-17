package com.github.orql.executor;

import com.github.orql.executor.schema.SchemaManager;
import com.github.orql.executor.util.MapBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryBuilder {

    private Integer page;

    private Integer size;

    private Long offset;

    private Integer limit;

    private Session session;

    private String orql;

    private Map<String, Object> params = new HashMap<>();

    private SchemaManager schemaManager;

    public QueryBuilder(Session session, SchemaManager schemaManager) {
        this.session = session;
        this.schemaManager = schemaManager;
    }

    public QueryBuilder page(Integer page) {
        this.page = page;
        return this;
    }

    public QueryBuilder size(Integer size) {
        this.size = size;
        return this;
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

    public <T> List<T> queryAll(Class<T> clazz) {
        if (page != null && size != null) {
            offset = (long) (page - 1) * size;
            limit = size;
        }
        Object result = session.query(orql, params, offset, limit);
        List<T> list = new ArrayList<>();
        for (Object child : (List) result) {
            list.add((T) MapBean.toBean((Map) child, clazz));
        }
        return list;
    }

    public <T> T queryOne(Class<T> clazz) {
        Object result = session.query(orql, params, null, null);
        if (result == null) return null;
        return (T) MapBean.toBean((Map) result, clazz);
    }

    public Long count() {
        return (Long) session.query(orql, params, null, null);
    }

}
