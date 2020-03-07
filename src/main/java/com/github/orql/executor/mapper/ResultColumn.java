package com.github.orql.executor.mapper;

import com.github.orql.core.schema.DataType;

public class ResultColumn extends Result {

    /**
     * 数据库列
     */
    protected String field;

    protected DataType type;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

}
