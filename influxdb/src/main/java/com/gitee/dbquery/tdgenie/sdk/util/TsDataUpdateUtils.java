package com.gitee.dbquery.tdgenie.sdk.util;

import com.gitee.dbquery.tdgenie.util.DateTimeUtils;
import com.gitee.dbquery.tdgenie.sdk.dto.ConnectionDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 时序数据更新工具类
 *
 * @author 风一样的码农
 * @since 2023/8/24
 **/
public class TsDataUpdateUtils {


    /**
     * 批量插入数据，自动建子表
     *
     * @param connection   连接
     * @param db           库
     * @param tb           表
     * @param stbDb        超级表所在库
     * @param stb          超级表
     * @param tagValueList TAG值
     * @param dataList     插入数据列表
     */
    public static void batchInsertAutoCreateTable(ConnectionDTO connection, String db, String tb,
                                                  String stbDb, String stb,
                                                  List<Object> tagValueList,
                                                  List<List<Object>> dataList) {

        StringBuilder sql = new StringBuilder("INSERT INTO `" + db + "`.`" + tb + "` USING `" + stbDb + "`.`" + stb + "` TAGS " + getFieldValueSql(tagValueList) + " VALUES ");
        dataList.forEach(d -> sql.append(getFieldValueSql(d)));
        RestConnectionUtils.executeUpdate(connection, Collections.singletonList(sql.toString()));
    }

    /**
     * 不指定列，也即使用全列模式批量写入（推荐，性能较好）
     *
     * @param connection 连接
     * @param db         数据库
     * @param tb         Measurement
     * @param dataList   数据列表
     */
    public static void batchInsertFullColumn(ConnectionDTO connection, String db, String tb, List<List<Object>> dataList) {
        StringBuilder sql = new StringBuilder("INSERT INTO `" + db + "`.`" + tb + "` VALUES");
        dataList.forEach(d -> sql.append(getFieldValueSql(d)));
        RestConnectionUtils.executeUpdate(connection, Collections.singletonList(sql.toString()));
    }

    /**
     * 指定列批量插入
     *
     * @param connection 连接
     * @param db         数据库
     * @param tb         Measurement
     * @param columnList 指定列
     * @param dataList   数据列表
     */
    public static void batchInsertSpecifyColumn(ConnectionDTO connection, String db, String tb,
                                                List<String> columnList,
                                                List<List<Object>> dataList) {
        StringBuilder columns = new StringBuilder("(");
        for (String column : columnList) {
            columns.append(column + ",");
        }
        columns.deleteCharAt(columns.length() - 1);
        columns.append(")");

        StringBuilder sql = new StringBuilder("INSERT INTO `" + db + "`.`" + tb + "` " + columns + " VALUES ");
        dataList.forEach(d -> sql.append(getFieldValueSql(d)));
        RestConnectionUtils.executeUpdate(connection, Collections.singletonList(sql.toString()));
    }

    private static String getFieldValueSql(List<Object> fieldList) {
        StringBuilder sb = new StringBuilder("( ");
        for (Object obj : fieldList) {
            if (obj instanceof String) {
                sb.append("'").append(obj.toString()).append("',");
            } else if (obj instanceof LocalDateTime) {
                sb.append("'").append(DateTimeUtils.format((LocalDateTime) obj, "yyyy-MM-dd HH:mm:ss.SSS")).append("',");
            } else if (obj instanceof LocalDate) {
                sb.append("'").append(DateTimeUtils.format(((LocalDate) obj).atStartOfDay(), "yyyy-MM-dd HH:mm:ss.SSS")).append("',");
            } else {
                sb.append(obj == null ? null : obj.toString()).append(",");
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(") ");
        return sb.toString();
    }

}
