package com.github.orql.executor.schema;

import com.github.orql.executor.Cascade;

public class Column {

    private String name;

    private DataType dataType;

    private String field;

    private Integer length;

    private boolean required = true;

    private boolean isPrivateKey = false;

    private boolean generatedKey = false;

    /**
     * 关联键
     */
    private boolean isRefKey = false;

    private Cascade onDelete;

    private Cascade onUpdate;

    private Schema ref;

    public String getName() {
        return name;
    }

    public DataType getDataType() {
        return dataType;
    }

    public String getField() {
        return field;
    }

    public Integer getLength() {
        return length;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isGeneratedKey() {
        return generatedKey;
    }

    public Schema getRef() {
        return ref;
    }

    public boolean isPrivateKey() {
        return isPrivateKey;
    }

    public boolean isRefKey() {
        return isRefKey;
    }

    public Cascade getOnDelete() {
        return onDelete;
    }

    public void setOnDelete(Cascade onDelete) {
        this.onDelete = onDelete;
    }

    public Cascade getOnUpdate() {
        return onUpdate;
    }

    public void setOnUpdate(Cascade onUpdate) {
        this.onUpdate = onUpdate;
    }

    public static class Builder {

        private Column column = new Column();

        public Builder name(String name) {
            column.name = name;
            return this;
        }

        public Builder dataType(DataType dataType) {
            column.dataType = dataType;
            return this;
        }

        public Builder isInt() {
            column.dataType = DataType.Int;
            return this;
        }

        public Builder isFloat() {
            column.dataType = DataType.Float;
            return this;
        }

        public Builder isString() {
            column.dataType = DataType.String;
            return this;
        }

        public Builder isBool() {
            column.dataType = DataType.Bool;
            return this;
        }

        public Builder isEnum() {
            column.dataType = DataType.Enum;
            return this;
        }

        public Builder length(int length) {
            column.length = length;
            return this;
        }

        public Builder field(String field) {
            column.field = field;
            return this;
        }

        public Builder isPrivateKey() {
            column.isPrivateKey = true;
            return this;
        }

        public Builder isGeneratedKey() {
            column.generatedKey = true;
            return this;
        }

        public Builder notRequired() {
            column.required = false;
            return this;
        }

        public Builder isRefKey() {
            column.isRefKey = true;
            return this;
        }

        public Builder ref(Schema ref) {
            column.ref = ref;
            return this;
        }

        public Builder onDelete(Cascade onDelete) {
            column.onDelete = onDelete;
            return this;
        }

        public Builder onUpdate(Cascade onUpdate) {
            column.onUpdate = onUpdate;
            return this;
        }

        public Column build() {
            if (column.field == null) {
                column.field = column.name;
            }
            return column;
        }

    }
}