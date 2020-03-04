package com.github.orql.executor;

import com.github.orql.core.schema.Association;
import com.github.orql.core.schema.SchemaInfo;
import com.github.orql.core.schema.SchemaManager;
import com.github.orql.executor.util.MapBean;
import com.github.orql.executor.util.OrqlUtil;
import com.github.orql.executor.util.ReflectUtil;

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

    public List<String> getOrqlItems(Object instance) {
        Class clazz = instance.getClass();
        SchemaInfo schema = schemaManager.getSchema(clazz);
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
        return items;
    }

    /**
     * 不插入自增id,插回自增id
     * 关联类型
     * hasOne hasMany -> 关联插入
     * belongsTo -> 关联插入父对象id
     * @param instance
     */
    public void add(Object instance) {
        // FIXME belongsToMany插入未实现
        SchemaInfo schema = schemaManager.getSchema(instance.getClass());
        Field[] fields = instance.getClass().getDeclaredFields();
        // 后续执行的
        List<Object> postAddList = new ArrayList<>();
        List<String> items = new ArrayList<>();
        for (Field field : fields) {
            field.setAccessible(true);
            Object value = null;
            try {
                value = field.get(instance);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (value == null) continue;
            if (schema.containsColumn(field.getName())) {
                items.add(field.getName());
            }
            if (schema.containsAssociation(field.getName())) {
                Association association = schema.getAssociation(field.getName());
                if (association.getType() == Association.Type.BelongsTo) {
                    items.add(field.getName());
                } else if (association.getType() == Association.Type.HasOne) {
                    // 根据当前外键获取对方外键
                    Association refAssociation = association.getRef().getAssociationByRefKey(association.getRefKey());
                    if (refAssociation != null && ReflectUtil.hasField(value, refAssociation.getName())) {
                        // 插入值
                        ReflectUtil.setValue(value, refAssociation.getName(), instance);
                        // 等当前插入后再插入
                        postAddList.add(value);
                    }
                } else if (association.getType() == Association.Type.HasMany) {
                    Association refAssociation = association.getRef().getAssociationByRefKey(association.getRefKey());
                    if (refAssociation != null) {
                        for (Object child : (List) value) {
                            // 插入值
                            ReflectUtil.setValue(child, refAssociation.getName(), instance);
                            // 等当前插入后再插入
                            postAddList.add(child);
                        }
                    }
                }
            }
        }
        if (items.isEmpty()) return;
        String orql = schema.getName() + " : {" + String.join(", ", items) + "}";
        add(orql, instance);
        for(Object item : postAddList) {
            add(item);
        }
    }

    public void add(String orql, Object instance) {
        Map<String, Object> params = MapBean.toMap(instance);
        Object id = session.add(orql, params);
        if (id != null) {
            String schemaName = OrqlUtil.getSchema(orql);
            SchemaInfo schema = schemaManager.getSchema(schemaName);
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

    public void delete(SchemaInfo schema, Object id) {
        String orql = "delete " + schema.getName() + "(" + schema.getIdName() + " = #" + schema.getIdName() + ")";
        Map<String, Object> params = new HashMap<>();
        params.put(schema.getIdName(), id);
        session.delete(orql, params);
    }

    public void delete(Class clazz, Object id) {
        SchemaInfo schema = schemaManager.getSchema(clazz);
        delete(schema, id);
    }

    public void delete(Object instance) {
        try {
            Class clazz = instance.getClass();
            SchemaInfo schema = schemaManager.getSchema(clazz);
            Field field = clazz.getDeclaredField(schema.getIdName());
            field.setAccessible(true);
            Object id = field.get(instance);
            delete(schema, id);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用orql和instance作为params更新
     * @param orql
     * @param instance
     */
    public void update(String orql, Object instance) {
        Map<String, Object> params = MapBean.toMap(instance);
        session.update(orql, params);
    }

    /**
     * 使用id作为条件，更新非null值
     * @param instance
     */
    public void update(Object instance) {
        SchemaInfo schema = schemaManager.getSchema(instance);
        List<String> items = getOrqlItems(instance);
        if (items.isEmpty()) return;
        String orql = schema.getName() + "(" + schema.getIdName() + " = #" + schema.getIdName() + ") : {" + items.stream().collect(Collectors.joining(", ")) + "}";
        update(orql, instance);
    }

}
