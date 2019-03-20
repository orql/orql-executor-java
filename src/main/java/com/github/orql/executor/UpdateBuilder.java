package com.github.orql.executor;

import com.github.orql.executor.schema.Schema;
import com.github.orql.executor.schema.SchemaManager;
import com.github.orql.executor.util.MapBean;
import com.github.orql.executor.util.OrqlUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UpdateBuilder {

    protected SchemaManager schemaManager;

    protected Session session;

    public UpdateBuilder(Session session, SchemaManager schemaManager) {
        this.schemaManager = schemaManager;
        this.session = session;
    }

    /**
     * 不插入自增id,插回自增id
     * @param instance
     */
    public void add(Object instance) {
        // FIXME 关联插入未实现
        Class clazz = instance.getClass();
        Schema schema = schemaManager.getSchema(clazz);
        Field[] fields = clazz.getDeclaredFields();
        String idName = schema.getIdName();
        List<String> items = new ArrayList<>();
        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();
            if (fieldName.equals(idName)) {
                continue;
            }
            if (schema.containsColumn(fieldName) || schema.containsAssociation(fieldName)) {
                try {
                    if (field.get(instance) != null) {
                        items.add(fieldName);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        if (items.isEmpty()) return;
        String reql = "add " + schema.getName() + " : {" + items.stream().collect(Collectors.joining(", ")) + "}";
        add(reql, instance);
    }

    public void add(String reql, Object instance) {
        Map<String, Object> params = MapBean.toMap(instance);
        Object id = session.add(reql, params);
        if (id != null) {
            String schemaName = OrqlUtil.getSchema(reql);
            Schema schema = schemaManager.getSchema(schemaName);
            Class clazz = schema.getClazz();
            try {
                Field idField = clazz.getDeclaredField(schema.getIdName());
                idField.setAccessible(true);
                // id插回
                idField.set(instance, id);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void delete(Schema schema, Object id) {
        String reql = "delete " + schema.getName() + "(" + schema.getIdName() + " = #" + schema.getIdName() + ")";
        Map<String, Object> params = new HashMap<>();
        params.put(schema.getIdName(), id);
        session.delete(reql, params);
    }

    public void delete(Class clazz, Object id) {
        Schema schema = schemaManager.getSchema(clazz);
        delete(schema, id);
    }

    public void delete(Object instance) {
        try {
            Class clazz = instance.getClass();
            Schema schema = schemaManager.getSchema(clazz);
            Field field = clazz.getDeclaredField(schema.getIdName());
            field.setAccessible(true);
            Object id = field.get(instance);
            delete(schema, id);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用reql和instance作为params更新
     * @param reql
     * @param instance
     */
    public void update(String reql, Object instance) {
        Map<String, Object> params = MapBean.toMap(instance);
        session.update(reql, params);
    }

    /**
     * 使用id作为条件，更新非null值
     * @param instance
     */
    public void update(Object instance) {
        Class clazz = instance.getClass();
        Schema schema = schemaManager.getSchema(clazz);
        Field[] fields = clazz.getDeclaredFields();
        String idName = schema.getIdName();
        List<String> items = new ArrayList<>();
        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();
            if (fieldName.equals(idName)) {
                continue;
            }
            if (schema.containsColumn(fieldName) || schema.containsAssociation(fieldName)) {
                try {
                    if (field.get(instance) != null) {
                        items.add(fieldName);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        if (items.isEmpty()) return;
        String reql = "update " + schema.getName() + "(" + schema.getIdName() + " = #" + schema.getIdName() + ") : {" + items.stream().collect(Collectors.joining(", ")) + "}";
        update(reql, instance);
    }

}
