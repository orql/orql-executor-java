package com.github.orql.executor.schema;

import com.github.orql.executor.annotation.BelongsTo;
import com.github.orql.executor.annotation.BelongsToMany;
import com.github.orql.executor.annotation.HasMany;
import com.github.orql.executor.annotation.HasOne;
import com.github.orql.executor.exception.TypeNotSupportException;
import com.github.orql.executor.util.Strings;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class SchemaManager {

    private static final Logger logger = LoggerFactory.getLogger(SchemaManager.class);

    private Map<String, Schema> schemas = new HashMap<>();

    private static class ReflectWrapper {
        public Schema schema;
        public Field[] fields;
    }

    public SchemaManager addSchema(Schema schema) {
        schemas.put(schema.getName(), schema);
        return this;
    }

    public boolean containsSchema(String name) {
        return schemas.containsKey(name);
    }

    public Schema getSchema(String name) {
        if (schemas.containsKey(name)) {
            return schemas.get(name);
        }
        return null;
    }

    public Schema getSchema(Class clazz) {
        for (Map.Entry<String, Schema> entry : schemas.entrySet()) {
            if (entry.getValue().getClazz() == clazz) {
                return entry.getValue();
            }
        }
        return null;
    }

    public void scanPackage(String path) {
        Reflections reflections = new Reflections(path);
        List<ReflectWrapper> reflectWrappers = new ArrayList<>();
        Set<Class<?>> schemas = reflections.getTypesAnnotatedWith(com.github.orql.executor.annotation.Schema.class);
        for (Class<?> clazz : schemas) {
            logger.info("scan class " + clazz);
            Schema schema = initSchema(clazz);
            Field[] fields = clazz.getDeclaredFields();
            ReflectWrapper wrapper = new ReflectWrapper();
            wrapper.schema = schema;
            wrapper.fields = fields;
            reflectWrappers.add(wrapper);
            addSchema(schema);
            initColumns(schema, fields);
        }
        for (ReflectWrapper wrapper : reflectWrappers) {
            initAssociations(wrapper.schema, wrapper.fields);
        }
    }

    private Schema initSchema(Class<?> clazz) {
        com.github.orql.executor.annotation.Schema schemaAnnotation = clazz.getAnnotation(com.github.orql.executor.annotation.Schema.class);
        Schema.Builder schemaBuilder = new Schema.Builder();
        if (!schemaAnnotation.value().equals("")) {
            schemaBuilder.name(schemaAnnotation.value());
        } else if (!schemaAnnotation.name().equals("")) {
            schemaBuilder.name(schemaAnnotation.name());
        } else {
            schemaBuilder.name(Strings.toLowerCaseFirst(clazz.getSimpleName()));
        }
        if (!schemaAnnotation.table().equals("")) {
            schemaBuilder.table(schemaAnnotation.table());
        }
        // clazz
        schemaBuilder.clazz(clazz);
        return schemaBuilder.build();
    }

    private void initColumns(Schema schema, Field[] fields) {
        for (Field field : fields) {
            Column column = initColumn(field);
            if (column != null) {
                schema.addColumn(column);
            }
        }
    }

    private Column initColumn(Field field) {
        com.github.orql.executor.annotation.Column columnAnnotation = field.getAnnotation(com.github.orql.executor.annotation.Column.class);
        if (columnAnnotation == null) {
            return null;
        }
        Column.Builder columnBuilder = new Column.Builder();
        // name
        columnBuilder.name(field.getName());
        // field
        if (!columnAnnotation.field().equals("")) {
            columnBuilder.field(columnAnnotation.field());
        }
        // data type
        Class<?> type = field.getType();
        if (type == String.class) {
            columnBuilder.dataType(DataType.String);
        } else if (type == Integer.class) {
            columnBuilder.dataType(DataType.Int);
        } else if (type == Float.class) {
            columnBuilder.dataType(DataType.Float);
        } else if (type == Boolean.class) {
            columnBuilder.dataType(DataType.Bool);
        } else if (type == Long.class) {
            columnBuilder.dataType(DataType.Long);
        } else if (type == Date.class) {
            columnBuilder.dataType(DataType.Date);
        } else if (type.isEnum()) {
            columnBuilder.dataType(DataType.Enum);
        } else if (type == Double.class) {
            columnBuilder.dataType(DataType.Double);
        } else {
            try {
                throw new TypeNotSupportException(field);
            } catch (TypeNotSupportException e) {
                e.printStackTrace();
            }
        }
        // length
        if (columnAnnotation.length() > 0) {
            columnBuilder.length(columnAnnotation.length());
        }
        // primary key
        if (columnAnnotation.primaryKey()) {
            columnBuilder.isPrivateKey();
        }
        // generated key
        if (columnAnnotation.generatedKey()) {
            columnBuilder.isGeneratedKey();
        }
        return columnBuilder.build();
    }

    private void initAssociations(Schema schema, Field[] fields) {
        for (Field field : fields) {
            if (field.getAnnotation(com.github.orql.executor.annotation.Column.class) != null) {
                continue;
            }
            BelongsTo belongsToAnnotation = field.getAnnotation(BelongsTo.class);
            if (belongsToAnnotation != null) {
                Association.Builder builder = new Association.Builder(
                        field.getName(),
                        schema,
                        getSchema(field),
                        Association.Type.BelongsTo);
                // ref key
                if (! belongsToAnnotation.refKey().equals("")) {
                    builder.refKey(belongsToAnnotation.refKey());
                }
                // required
                builder.required(belongsToAnnotation.required());
                builder.build();
                continue;
            }
            HasOne hasOneAnnotation = field.getAnnotation(HasOne.class);
            if (hasOneAnnotation != null) {
                Association.Builder builder = new Association.Builder(
                        field.getName(),
                        schema,
                        getSchema(field),
                        Association.Type.HasOne);
                // ref key
                if (! hasOneAnnotation.refKey().equals("")) {
                    builder.refKey(hasOneAnnotation.refKey());
                }
                // required
                builder.required(hasOneAnnotation.required());
                // cascade
                builder.onDelete(hasOneAnnotation.onDelete());
                builder.onUpdate(hasOneAnnotation.onUpdate());
                builder.build();
                continue;
            }
            HasMany hasManyAnnotation = field.getAnnotation(HasMany.class);
            if (hasManyAnnotation != null) {
                Association.Builder builder = new Association.Builder(
                        field.getName(),
                        schema,
                        getSchema(field),
                        Association.Type.HasMany);
                // ref key
                if (! hasManyAnnotation.refKey().equals("")) {
                    builder.refKey(hasManyAnnotation.refKey());
                }
                // required
                builder.required(hasManyAnnotation.required());
                // cascade
                builder.onDelete(hasManyAnnotation.onDelete());
                builder.onUpdate(hasManyAnnotation.onUpdate());
                builder.build();
                continue;
            }
            BelongsToMany belongsToManyAnnotation = field.getAnnotation(BelongsToMany.class);
            if (belongsToManyAnnotation != null) {
                Association.Builder builder = new Association.Builder(
                        field.getName(),
                        schema,
                        getSchema(field),
                        Association.Type.BelongsToMany);
                // middle
                Class<?> middleClass = belongsToManyAnnotation.middle();
                com.github.orql.executor.annotation.Schema middleSchema = middleClass.getAnnotation(com.github.orql.executor.annotation.Schema.class);
                builder.middle(middleSchema.name());
                // middle key
                if (! belongsToManyAnnotation.middleKey().equals("")) {
                    builder.middleKey(belongsToManyAnnotation.middleKey());
                }
                // ref middle key
                if (! belongsToManyAnnotation.refMiddleKey().equals("")) {
                    builder.refKey(belongsToManyAnnotation.refMiddleKey());
                }
                builder.build();
                continue;
            }
        }
    }

    private Schema getSchema(Field field) {
        if (field.getType() == List.class) {
            Type genericType = field.getGenericType();
            ParameterizedType pt = (ParameterizedType) genericType;
            // T class type
            Class<?> genericClazz = (Class<?>)pt.getActualTypeArguments()[0];
            return getSchema(Strings.toLowerCaseFirst(genericClazz.getSimpleName()));
        }
        return getSchema(Strings.toLowerCaseFirst(field.getType().getSimpleName()));
    }

    public Map<String, Schema> getSchemas() {
        return schemas;
    }
}
