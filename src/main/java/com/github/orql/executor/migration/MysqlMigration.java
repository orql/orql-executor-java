package com.github.orql.executor.migration;

import com.github.orql.executor.Configuration;
import com.github.orql.executor.Session;
import com.github.orql.executor.schema.Column;
import com.github.orql.executor.schema.DataType;
import com.github.orql.executor.schema.Schema;
import com.github.orql.executor.schema.SchemaManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MysqlMigration implements Migration {

    private static class FK {
        String name;
        String ref;
    }

    private static class Field {
        String name;
        String type;
        Integer length;
        String nullable;
    }

    private Configuration configuration;

    private String database;

    private static final String typePatternString = "(.+?)\\((.+?)\\)";

    private static final Pattern typePattern = Pattern.compile(typePatternString);

    public MysqlMigration(Configuration configuration, String database) {
        this.configuration = configuration;
        this.database = database;
    }

    @Override
    public void create(Session session) throws SQLException {
        SchemaManager schemaManager = configuration.getSchemaManager();
        for (Map.Entry<String, Schema> entry : schemaManager.getSchemas().entrySet()) {
            createTable(session, entry.getValue());
        }
        for (Map.Entry<String, Schema> entry : schemaManager.getSchemas().entrySet()) {
            updateFks(session, entry.getValue(), database);
        }
    }

    @Override
    public void update(Session session) throws SQLException {
        SchemaManager schemaManager = configuration.getSchemaManager();
        for (Map.Entry<String, Schema> entry: schemaManager.getSchemas().entrySet()) {
            Schema schema = entry.getValue();
            Boolean exist = existTable(session, schema);
            if (!exist) {
                createTable(session, schema);
                continue;
            }
            String sql = "select COLUMN_NAME as name, IS_NULLABLE as nullable, COLUMN_TYPE as type " +
                    "from information_schema.columns " +
                    "where table_schema = '" + database + "' && table_name = '" + schema.getTable() + "'";
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
            for (Column column: schema.getColumns()) {
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
        for (Map.Entry<String, Schema> entry : schemaManager.getSchemas().entrySet()) {
            updateFks(session, entry.getValue(), database);
        }
    }

    @Override
    public void drop(Session session) throws SQLException {
        SchemaManager schemaManager = configuration.getSchemaManager();
        session.buildNative().sql("set foreign_key_checks = 0").update();
        for (Map.Entry<String, Schema> entry: schemaManager.getSchemas().entrySet()) {
            boolean exist = existTable(session, entry.getValue());
            if (exist) {
                String dropSql = "drop table " + entry.getValue().getTable();
                session.buildNative().sql(dropSql).update();
            }
        }
        session.buildNative().sql("set foreign_key_checks = 1").update();
    }

    private void createTable(Session session, Schema schema) {
        String sql = "create table if not exists " +
                schema.getTable() + " (" +
                schema.getColumns().stream().map(this::genCreateColumn).collect(Collectors.joining(", ")) +
                ")";
        session.buildNative().sql(sql).update();
    }

    private String genCreateColumn(Column column) {
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

    private String genColumnType(Column column) {
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
        }
        return column.getLength() != null && column.getLength() > 0 ? (type + "(" + column.getLength() + ")") : type;
    }
    private void updateFks(Session session, Schema schema, String database) throws SQLException {
        String queryFKSql = "select COLUMN_NAME as name, REFERENCED_TABLE_NAME as ref, REFERENCED_COLUMN_NAME as refKey " +
                "from INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
                "where CONSTRAINT_SCHEMA = '" + database + "' && TABLE_NAME = '" + schema.getTable() + "' && CONSTRAINT_NAME <> 'PRIMARY'";
        ResultSet fkResultSet = session.buildNative().sql(queryFKSql).query();
        List<String> fkNames = new ArrayList<>();
        List<FK> fks = new ArrayList<>();
        while (fkResultSet.next()) {
            String name = fkResultSet.getString("name");
            String ref = fkResultSet.getString("ref");
            fkNames.add(name);
            FK fk = new FK();
            fk.name = name;
            fk.ref = ref;
            fks.add(fk);
        }
        List<Column> refColumns = schema.getColumns().stream().filter(Column::isRefKey).collect(Collectors.toList());
        for (Column column: refColumns) {
            int index = fkNames.indexOf(column.getField());
            if (index >= 0) {
                // 外键存在
                FK fk = fks.get(index);
                if (!column.getRef().getTable().equals(fk.ref)) {
                    // 外键已指向其他表
                    // 删除当前外键
                    session.buildNative().sql("alter table " + schema.getTable() + " drop foreign key " + genFK(schema, column)).update();
                    // 新建外键
                    createFK(session, schema, column);
                }
            } else {
                // 外键不存在
                createFK(session, schema, column);
            }
        }
    }

    private void createFK(Session session, Schema schema, Column column) {
        String sql = "alter table " + schema.getTable() + " add constraint " + genFK(schema, column) + " foreign key(" + column.getField() + ") REFERENCES " + column.getRef().getTable() + " (" + column.getRef().getIdField() + ")";
        session.buildNative().sql(sql).update();
    }

    private String genFK(Schema schema, Column column) {
        return "fk_" + schema.getTable() + "_" + column.getField();
    }

    private Boolean existTable(Session session, Schema schema) throws SQLException {
        String sql = "show tables like '" + schema.getTable() + "'";
        ResultSet resultSet = session.buildNative().sql(sql).query();
        while (resultSet.next()) {
            String tableName = resultSet.getString(1);
            if (tableName != null && tableName.equals(schema.getTable())) return true;
        }
        return false;
    }
}
