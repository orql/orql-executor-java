package com.github.orql.executor.mapper;

import java.util.List;

/**
 * mapper跟节点
 */
public class ResultRoot {

    /**
     * id键
     */
    private ResultId id;

    private List<Result> columns;

    /**
     * 当前全部列, 为后续遍历提高性能
     */
    private List<ResultColumn> allColumns;

    public ResultId getId() {
        return id;
    }

    public void setId(ResultId id) {
        this.id = id;
    }

    public List<Result> getColumns() {
        return columns;
    }

    public void setColumns(List<Result> columns) {
        this.columns = columns;
    }

    public List<ResultColumn> getAllColumns() {
        return allColumns;
    }

    public void setAllColumns(List<ResultColumn> allColumns) {
        this.allColumns = allColumns;
    }
}
