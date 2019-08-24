package com.github.orql.executor.mapper;

import com.github.orql.executor.Constants;
import com.github.orql.executor.orql.OrqlNode.*;
import com.github.orql.executor.schema.Column;
import com.github.orql.executor.schema.Schema;

import java.util.ArrayList;
import java.util.List;

public class OrqlResult {

    public ResultRoot toResult(OrqlRefItem orqlRoot) {
        return toResult(orqlRoot, orqlRoot.getRef().getTable(), new ArrayList<>());
    }

    /**
     * 将orql转换为result
     * @param orqlRoot
     * @param path
     * @return
     */
    private ResultRoot toResult(OrqlRefItem orqlRoot, String path, List<ResultColumn> allColumns) {
        ResultRoot resultRoot = new ResultRoot();
        ResultId resultId = null;
        List<Result> columns = new ArrayList<>();
        for (OrqlItem item : orqlRoot.getChildren()) {
            if (item instanceof OrqlColumnItem) {
                Column column = ((OrqlColumnItem) item).getColumn();
                if (column.isPrivateKey()) {
                    resultId = new ResultId();
                    resultId.setColumn(column.getName());
                    resultId.setField(path + Constants.SqlSplit + column.getField());
                    resultId.setType(column.getDataType());
                } else {
                    ResultColumn resultColumn = new ResultColumn();
                    resultColumn.setColumn(column.getName());
                    resultColumn.setField(path + "_" + column.getField());
                    resultColumn.setType(column.getDataType());
                    columns.add(resultColumn);
                    allColumns.add(resultColumn);
                }
            } else if (item instanceof OrqlRefItem) {
                if (((OrqlRefItem) item).getChildren().isEmpty()) continue;
                if (((OrqlRefItem) item).isArray()) {
                    ResultArray resultArray = new ResultArray();
                    resultArray.setColumn(item.getName());
                    resultArray.setRoot(toResult((OrqlRefItem) item, path + Constants.SqlSplit + item.getName(), allColumns));
                    columns.add(resultArray);
                } else {
                    ResultObject resultObject = new ResultObject();
                    resultObject.setColumn(item.getName());
                    resultObject.setRoot(toResult((OrqlRefItem) item, path + Constants.SqlSplit + item.getName(), allColumns));
                    columns.add(resultObject);
                }
            }
        }
        // 没有id插入id
        if (resultId == null) {
            Schema refSchema = orqlRoot.getRef();
            Column idColumn = refSchema.getIdColumn();
            resultId = new ResultId();
            resultId.setColumn(idColumn.getName());
            resultId.setField(path + "_" + idColumn.getField());
            resultId.setType(idColumn.getDataType());
        }
        allColumns.add(resultId);
        resultRoot.setId(resultId);
        resultRoot.setColumns(columns);
        resultRoot.setAllColumns(allColumns);
        return resultRoot;
    }

}
