package com.github.orql.executor.mapper;

import com.github.orql.executor.schema.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ResultMapper {

    private Logger logger = LoggerFactory.getLogger(ResultMapper.class);

    public Object getValue(ResultSet resultSet, String field, DataType type) throws SQLException {
        switch (type) {
            case Int:
                return resultSet.getObject(field) != null ? resultSet.getInt(field) : null;
            case Long:
                return resultSet.getObject(field) != null ? resultSet.getLong(field) : null;
            case Float:
                return resultSet.getObject(field) != null ? resultSet.getFloat(field) : null;
            case Bool:
                return resultSet.getObject(field) != null ? resultSet.getBoolean(field) : null;
            case Date:
                return resultSet.getTimestamp(field);
            case String:
                return resultSet.getString(field);
            case Enum:
                return resultSet.getObject(field);
        }
        return null;
    }

    public List<Map<String, Object>> mappe(ResultRoot resultRoot, ResultSet resultSet) throws SQLException {
        // 原始结果集合
        List<Map<String, Object>> resultMapList = new ArrayList<>();
        // 获取全部结果
        while(resultSet.next()) {
            Map<String, Object> resultMap = new LinkedHashMap<>();
            // 遍历全部列
            for (ResultColumn resultColumn : resultRoot.getAllColumns()) {
                String field = resultColumn.getField();
                Object value = getValue(resultSet, field, resultColumn.getType());
//                logger.info("field: " + field + ", value: " + value);
                resultMap.put(field, value);
            }
            resultMapList.add(resultMap);
        }
        return mappe(resultRoot, resultMapList);
    }

    private List<Map<String, Object>> mappe(ResultRoot resultRoot, List<Map<String, Object>> resultMapList) {
        List<Map<String, Object>> data = new ArrayList<>();
        ResultId resultId = resultRoot.getId();
        // 按id切割, {id : resultSetList}
        Map<Object, List<Map<String, Object>>> idListMap = new LinkedHashMap<>();
        for (Map<String, Object> resultMap : resultMapList) {
            Object idValue = resultMap.get(resultId.getField());
            // 避免null id列被映射
            if (idValue == null) continue;
            if (! idListMap.containsKey(idValue)) {
                idListMap.put(idValue, new ArrayList<>());
            }
            idListMap.get(idValue).add(resultMap);
        }
        for (Map.Entry<Object, List<Map<String, Object>>> entry : idListMap.entrySet()) {
            Object idValue = entry.getKey();
            List<Map<String, Object>> idList = entry.getValue();
            Map<String, Object> childData = new LinkedHashMap<>();
            // 从第一列获取数据
            Map<String, Object> rowRecord = idList.get(0);
            // 加入id
            childData.put(resultId.getColumn(), idValue);
            for (Result result : resultRoot.getColumns()) {
                if (result instanceof ResultColumn) {
                    childData.put(result.getColumn(), rowRecord.get(((ResultColumn) result).getField()));
                } else if (result instanceof ResultRef) {
                    List<Map<String, Object>> nestRecord = mappe(((ResultRef) result).getRoot(), idList);
                    if (result instanceof ResultObject) {
                        // 只填入一个值
                        childData.put(result.getColumn(), nestRecord.isEmpty() ? null : nestRecord.get(0));
                    } else if (result instanceof ResultArray) {
                        childData.put(result.getColumn(), nestRecord);
                    }
                }
            }
            data.add(childData);
        }
        return data;
    }
}
