package com.github.orql.executor.mapper;

import com.github.orql.executor.Constants;
import com.github.orql.executor.orql.OrqlNode.*;
import com.github.orql.executor.schema.Column;
import com.github.orql.executor.schema.Schema;

import java.util.ArrayList;
import java.util.List;

public class ReqlResult {

    public ResultRoot toResult(ReqlRefItem reqlRoot) {
        return toResult(reqlRoot, reqlRoot.getRef().getTable(), new ArrayList<>());
    }

    /**
     * 将reql转换为result
     * @param reqlRoot
     * @param path
     * @return
     */
    private ResultRoot toResult(ReqlRefItem reqlRoot, String path, List<ResultColumn> allColumns) {
        ResultRoot resultRoot = new ResultRoot();
        ResultId resultId = null;
        List<Result> columns = new ArrayList<>();
        for (ReqlItem item : reqlRoot.getChildren()) {
            if (item instanceof ReqlColumnItem) {
                Column column = ((ReqlColumnItem) item).getColumn();
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
            } else if (item instanceof ReqlObjectItem) {
                if (((ReqlObjectItem) item).getChildren().isEmpty()) continue;
                ResultObject resultObject = new ResultObject();
                resultObject.setColumn(item.getName());
                resultObject.setRoot(toResult((ReqlObjectItem) item, path + Constants.SqlSplit + item.getName(), allColumns));
                columns.add(resultObject);
            } else if (item instanceof ReqlArrayItem) {
                if (((ReqlArrayItem) item).getChildren().isEmpty()) continue;
                ResultArray resultArray = new ResultArray();
                resultArray.setColumn(item.getName());
                resultArray.setRoot(toResult((ReqlArrayItem) item, path + Constants.SqlSplit + item.getName(), allColumns));
                columns.add(resultArray);
            }
        }
        // 没有id插入id
        if (resultId == null) {
            Schema refSchema = reqlRoot.getRef();
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
