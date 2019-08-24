package com.github.orql.executor.schema;

import com.github.orql.executor.Cascade;

import java.util.ArrayList;
import java.util.List;

public class Schema {

    /**
     * schema名称
     */
    private String name;

    /**
     * 数据库表名
     */
    private String table;

    /**
     * 类
     */
    private Class clazz;

    /**
     * id 列
     */
    private Column idColumn;

    /**
     * 普通列list
     */
    private List<Column> columns = new ArrayList<>();

    /**
     * 列名list
     */
    private List<String> columnNames = new ArrayList<>();

    /**
     * 关联对象list
     */
    private List<Association> associations = new ArrayList<>();

    /**
     * 关联对象名list
     */
    private List<String> associationNames = new ArrayList<>();

    public String getName() {
        return name;
    }

    public Column getIdColumn() {
        return idColumn;
    }

    public String getTable() {
        return table;
    }

    public Class getClazz() {
        return clazz;
    }

    public Schema addColumn(Column column) {
        columnNames.add(column.getName());
        if (column.isPrivateKey()) {
            idColumn = column;
        }
        columns.add(column);
        return this;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public boolean containsColumn(String name) {
        return columnNames.contains(name);
    }

    public boolean containsAssociation(String name) {
        return associationNames.contains(name);
    }

    public Column getColumn(String name) {
        int index = columnNames.indexOf(name);
        if (index >= 0) {
            return columns.get(index);
        }
        return null;
    }

    public Association getAssociation(String name) {
        int index = associationNames.indexOf(name);
        if (index >= 0) {
            return associations.get(index);
        }
        return null;
    }

    public List<Association> getAssociations() {
        return associations;
    }

    public Association getAssociationByRefKey(String refKey) {
        for (Association association : associations) {
            if (association.getRefKey().equals(refKey)) return association;
        }
        return null;
    }

    /**
     * 插入关联键
     * @param field
     * @param refSchema
     * @return
     */
    public Schema addRefColumn(String field, Schema refSchema) {
        return addRefColumn(field, refSchema, null, null);
    }

    public Schema addRefColumn(String field, Schema refSchema, Cascade onDelete, Cascade onUpdate) {
        for (Column column : columns) {
            if (column.getName().equals(field)) {
                //重复不插入
                return this;
            }
        }
        Column idColumn = refSchema.getIdColumn();
        Column refColumn = new Column.Builder()
                .name(field)
                .field(field)
                .isRefKey()
                .dataType(idColumn.getDataType()).isRefKey()
                .ref(refSchema)
                .onDelete(onDelete)
                .onUpdate(onUpdate)
                .build();
        addColumn(refColumn);
        return this;
    }

    public Schema addAssociation(Association association) {
        associationNames.add(association.getName());
        this.associations.add(association);
        switch (association.getType()) {
            case HasOne:
            case HasMany:
                //插入外键到ref
                association.getRef().addRefColumn(
                        association.getRefKey(),
                        association.getRef(),
                        association.getOnDelete(),
                        association.getOnUpdate());
                break;
            case BelongsTo:
                // 插入外键到自己
                addRefColumn(association.getRefKey(), association.getRef());
                break;
        }
        return this;
    }

    public Association.Builder belongsTo(String name, Schema ref) {
        return new Association.Builder(name, this, ref, Association.Type.BelongsTo);
    }

    public Association.Builder hasOne(String name, Schema ref) {
        return new Association.Builder(name, this, ref, Association.Type.HasOne);
    }

    public Association.Builder hasMany(String name, Schema ref) {
        return new Association.Builder(name, this, ref, Association.Type.HasMany);
    }

    public Association.Builder belongsToMany(String name, Schema ref, String middle) {
        Association.Builder builder = new Association.Builder(name, this, ref, Association.Type.HasMany);
        builder.middle(middle);
        return builder;
    }

    public Column getColumn(int i) {
        return columns.get(i);
    }

    public String getIdName() {
        return idColumn.getName();
    }

    public String getIdField() {
        return idColumn.getField();
    }

    public static class Builder {

        private Schema schema = new Schema();

        public Builder name(String name) {
            schema.name = name;
            if (schema.table == null) {
                schema.table = name;
            }
            return this;
        }

        public Builder table(String table) {
            schema.table = table;
            return this;
        }

        public Builder clazz(Class clazz) {
            schema.clazz = clazz;
            return this;
        }

        public Schema build() {
            return schema;
        }
    }
}