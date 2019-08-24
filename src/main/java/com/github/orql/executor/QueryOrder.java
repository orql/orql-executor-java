package com.github.orql.executor;

import java.util.ArrayList;
import java.util.List;

public class QueryOrder {

    private List<String> columns = new ArrayList<>();

    private String sort = "asc";

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getSort() {
        return sort;
    }

    public void addColumn(String column) {
        this.columns.add(column);
    }

    public List<String> getColumns() {
        return columns;
    }
}
