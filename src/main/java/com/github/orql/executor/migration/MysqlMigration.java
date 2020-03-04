package com.github.orql.executor.migration;

import com.github.orql.core.Cascade;
import com.github.orql.core.schema.ColumnInfo;
import com.github.orql.core.schema.DataType;
import com.github.orql.core.schema.SchemaInfo;
import com.github.orql.core.schema.SchemaManager;
import com.github.orql.executor.Configuration;
import com.github.orql.executor.Session;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.github.orql.core.schema.DataType.Int;

public class MysqlMigration implements Migration {

    private static class FK {
        String name;
        String ref;
        String onUpdate;
        String onDelete;
    }

    private static class Field {
        String name;
        String type;
        Integer length;
        String nullable;
    }

    private Configuration configuration;

    private static final String typePatternString = "(.+?)\\((.+?)\\)";

    private static final Pattern typePattern = Pattern.compile(typePatternString);

    public MysqlMigration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void create(Session session) throws SQLException {
        SchemaManager schemaManager = configuration.getSchemaManager();
        for (Map.Entry<String, SchemaInfo> entry : schemaManager.getSchemas().entrySet()) {
            createTable(session, entry.getValue());
        }
        for (Map.Entry<String, SchemaInfo> entry : schemaManager.getSchemas().entrySet()) {
            updateFks(session, entry.getValue());
        }
    }

    @Override
    public void update(Session session) throws SQLException {
        SchemaManager schemaManager = configuration.getSchemaManager();
        for (Map.Entry<String, SchemaInfo> entry: schemaManager.getSchemas().entrySet()) {
            SchemaInfo schema = entry.getValue();
            Boolean exist = existTable(session, schema);
            if (!exist) {
                createTable(session, schema);
                continue;
            }
            String sql = "select COLUMN_NAME as name, IS_NULLABLE as nullable, COLUMN_TYPE as type " +
                    "from information_schema.columns " +
                    "where table_schema = database() and table_name = '" + schema.getTable() + "'";
            List<String> fieldNames = new ArrayList<>();
            List<Field> fields = new ArrayList<>();
            ResultSet fieldResultSet = session.buildNative().sql(sql).query();
            while (fieldResultSet.next()) {
                Field field = new Field();
                field.name = fieldResultSet.getString("name");
                field.nullable = fieldResultSet.getString("nullable");
                String type = fieldResultSet.getString("type");
                Matcher m = typePattern.matcher(type);
                if (m.find()) {
                    field.type = m.group(1);
                    field.length = Integer.parseInt(m.group(2));
                } else {
                    field.type = type;
                }
                fields.add(field);
                fieldNames.add(field.name);
            }
            for (ColumnInfo column: schema.getColumns()) {
                int index = fieldNames.indexOf(column.getField());
                if (index >= 0) {
                    boolean change = false;
                    Field field = fields.get(index);
                    // 类型修改
                    if (!genColumnType(column).equals(field.type)) change = true;
                    // 长度修改
                    if (column.getLength() != null && !Objects.equals(column.getLength(), field.length)) change = true;
                    if (!column.isPrivateKey()) {
                        if (column.isRequired() && field.nullable.equals("NO")) change = true;
                        if (!column.isRequired() && field.nullable.equals("YES")) change = true;
                    }
                    if (change) {
                        // 修改字段
                        String updateSql = "alter table " + schema.getTable() + " change " + column.getField() + " " + genCreateColumn(column);
                        session.buildNative().sql(updateSql).update();
                    }
                } else {
                    // 字段不存在
                    String addSql = "alter table " + schema.getTable() +" add " + genCreateColumn(column);
                    session.buildNative().sql(addSql).update();
                }
            }
        }
        for (Map.Entry<String, SchemaInfo> entry : schemaManager.getSchemas().entrySet()) {
            updateFks(session, entry.getValue());
        }
    }

    @Override
    public void drop(Session session) throws SQLException {
        SchemaManager schemaManager = configuration.getSchemaManager();
        session.buildNative().sql("set foreign_key_checks = 0").update();
        for (Map.Entry<String, SchemaInfo> entry: schemaManager.getSchemas().entrySet()) {
            boolean exist = existTable(session, entry.getValue());
            if (exist) {
                String dropSql = "drop table " + entry.getValue().getTable();
                session.buildNative().sql(dropSql).update();
            }
        }
        session.buildNative().sql("set foreign_key_checks = 1").update();
    }

    private void createTable(Session session, SchemaInfo schema) {
        String sql = "create table if not exists " +
                schema.getTable() + " (" +
                schema.getColumns().stream().map(this::genCreateColumn).collect(Collectors.joining(", ")) +
                ")";
        session.buildNative().sql(sql).update();
    }

    private String genCreateColumn(ColumnInfo column) {
        String sql = column.getField() + " " + genColumnType(column);
        if (column.getDataType() == DataType.String) {
            if (column.getLength() == null) {
                sql += "(256)";
            }
        }
        if (column.isPrivateKey()) sql += " primary key";
        if (column.isGeneratedKey()) sql += " auto_increment";
        if (!column.isPrivateKey() && column.isRequired()) sql += " not null";
        return sql;
    }

    private String genColumnType(ColumnInfo column) {
        String type = "";
        if (column.getDataType() == null) {
            type = "";
        }
        switch (column.getDataType()) {
            case Int:
                type = "int";
                break;
            case Long:
                type = "bigint";
                break;
            case Float:
                type = "float";
                break;
            case Bool:
                type = "boolean";
                break;
            case Date:
                type = "datetime";
                break;
            case String:
                type = "varchar";
                break;
            case Enum:
                type = "enum";
                break;
            case Double:
                type = "double";
                break;
        }
        return column.getLength() != null && column.getLength() > 0 ? (type + "(" + column.getLength() + ")") : type;
    }
    private void updateFks(Session session, SchemaInfo schema) throws SQLException {
        String queryFKSql = "select " +
                "u.COLUMN_NAME as name, " +
                "u.REFERENCED_TABLE_NAME as ref, " +
                "u.REFERENCED_COLUMN_NAME as refKey, " +
                "r.UPDATE_RULE AS onUpdate, " +
                "r.DELETE_RULE AS onDelete " +
                "FROM  INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS AS r " +
                "INNER JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE AS u ON u.CONSTRAINT_NAME = r.CONSTRAINT_NAME and u.CONSTRAINT_NAME <> 'PRIMARY' AND u.table_schema = r.constraint_schema AND u.table_name = r.table_name " +
                "WHERE u.constraint_schema = database() AND u.table_name = '" + schema.getTable() + "'";
        ResultSet fkResultSet = session.buildNative().sql(queryFKSql).query();
        List<String> fkNames = new ArrayList<>();
        List<FK> fks = new ArrayList<>();
        while (fkResultSet.next()) {
            String name = fkResultSet.getString("name");
            String ref = fkResultSet.getString("ref");
            String onUpdate = fkResultSet.getString("onUpdate");
            String onDelete = fkResultSet.getString("onDelete");
            fkNames.add(name);
            FK fk = new FK();
            fk.name = name;
            fk.ref = ref;
            fk.onUpdate = onUpdate;
            fk.onDelete = onDelete;
            fks.add(fk);
        }
        List<ColumnInfo> refColumns = schema.getColumns().stream().filter(ColumnInfo::isRefKey).collect(Collectors.toList());
        for (ColumnInfo column: refColumns) {
            int index = fkNames.indexOf(column.getField());
            if (index >= 0) {
                // 外键存在
                FK fk = fks.get(index);
                boolean change = false;
                // 外键已指向其他表
                if (!column.getRef().getTable().equals(fk.ref)) change = true;
                // onDelete变化
                if (!equalsCascade(column.getOnDelete(), fk.onDelete)) change = true;
                // onUpdate变化
                if (!equalsCascade(column.getOnUpdate(), fk.onUpdate)) change = true;
                if (change) {
                    updateFK(session, schema, column);
                }
            } else {
                // 外键不存在
                createFK(session, schema, column);
            }
        }
    }

    /**
     * 比较级联操作
     * @param cascade
     * @param string
     * @return
     */
    private boolean equalsCascade(Cascade cascade, String string) {
        return cascade == null || cascadeToString(cascade).equals(string);
    }

    private String cascadeToString(Cascade cascade) {
        switch (cascade) {
            case Cascade:
                return "CASCADE";
            case SetNull:
                return "Set NULL";
            case NoAction:
                return "NOT ACTION";
            case Restrict:
                return "RESTRICT";
            default:
                return "RESTRICT";
        }
    }

    private void updateFK(Session session, SchemaInfo schema, ColumnInfo column) {
        // 先删除
        session.buildNative().sql("alter table " + schema.getTable() + " drop foreign key " + genFK(schema, column)).update();
        // 再新建
        createFK(session, schema, column);
    }

    private void createFK(Session session, SchemaInfo schema, ColumnInfo column) {
        String sql = "alter table " + schema.getTable() + " add constraint " + genFK(schema, column) + " foreign key(" + column.getField() + ") REFERENCES " + column.getRef().getTable() + " (" + column.getRef().getIdField() + ")";
        if (column.getOnDelete() != null) {
            sql += " on delete " + cascadeToString(column.getOnDelete());
        }
        if (column.getOnUpdate() != null) {
            sql += " on update " + cascadeToString(column.getOnUpdate());
        }
        session.buildNative().sql(sql).update();
    }

    private String genFK(SchemaInfo schema, ColumnInfo column) {
        return "fk_" + schema.getTable() + "_" + column.getField();
    }

    private Boolean existTable(Session session, SchemaInfo schema) throws SQLException {
        String sql = "show tables like '" + schema.getTable() + "'";
        ResultSet resultSet = session.buildNative().sql(sql).query();
        while (resultSet.next()) {
            String tableName = resultSet.getString(1);
            if (tableName != null && tableName.equals(schema.getTable())) return true;
        }
        return false;
    }
}
