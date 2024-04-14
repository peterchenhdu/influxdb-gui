package com.gitee.dbquery.tdgenie.sdk.util;

import com.gitee.dbquery.tdgenie.sdk.dto.res.TableFieldResDTO;
import com.gitee.dbquery.tdgenie.sdk.dto.tb.TableAddByStableDTO;
import com.gitee.dbquery.tdgenie.sdk.dto.tb.TableUpdateDTO;
import com.gitee.dbquery.tdgenie.sdk.exception.TableAlreadyExistException;
import com.gitee.dbquery.tdgenie.util.ObjectUtils;
import com.gitee.dbquery.tdgenie.sdk.dto.ConnectionDTO;
import com.gitee.dbquery.tdgenie.sdk.dto.field.TableFieldDTO;
import com.gitee.dbquery.tdgenie.sdk.dto.res.TableCreateResDTO;
import com.gitee.dbquery.tdgenie.sdk.dto.res.TableResDTO;
import com.gitee.dbquery.tdgenie.sdk.dto.tb.TableAddDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 表工具类
 *
 * @author 风一样的码农
 * @since 2023/8/18
 **/
public class TableUtils {

    /**
     * 创建表
     *
     * @param connection   连接
     * @param stableAddDTO 建表信息
     */
    public static void createTable(ConnectionDTO connection, TableAddDTO stableAddDTO) {
        List<TableFieldDTO> fieldList = stableAddDTO.getFieldList().stream().filter(f -> !f.getIsTag()).collect(Collectors.toList());
        List<TableFieldDTO> tagList = stableAddDTO.getFieldList().stream().filter(TableFieldDTO::getIsTag).collect(Collectors.toList());

        StringBuilder sql = new StringBuilder("create " + (ObjectUtils.isEmpty(tagList) ? "TABLE " : "STABLE ") + "`" + stableAddDTO.getDb() + "`.`");
        sql.append(stableAddDTO.getTb()).append("` (");
        fieldList.forEach(field -> {
            sql.append(field.getName()).append(" ").append(getFieldType(field)).append(",");
        });

        if (fieldList.size() > 0) {
            sql.deleteCharAt(sql.lastIndexOf(","));
            sql.append(")");
        }


        if (ObjectUtils.isNotEmpty(tagList)) {
            sql.append(" tags (");
            tagList.forEach(tag -> {
                sql.append(tag.getName()).append(" ").append(getFieldType(tag)).append(",");
            });

            if (tagList.size() > 0) {
                sql.deleteCharAt(sql.lastIndexOf(","));
                sql.append(")");
            }
        }

        sql.append(";");

        try {
            RestConnectionUtils.executeUpdate(connection, Collections.singletonList(sql.toString()));
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("TDengine ERROR (360)")) {
                throw new TableAlreadyExistException();
            }
            throw e;
        }
    }

    /**
     * 通过超级表批量建表
     *
     * @param connection 连接
     * @param dtoList    建表信息
     */
    public static void createTableUsingStable(ConnectionDTO connection, List<TableAddByStableDTO> dtoList) {
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        for (TableAddByStableDTO dto : dtoList) {
            sb.append("`").append(dto.getDbName()).append("`.`").append(dto.getTableName()).append("`").append(" USING `").append(dto.getDbName()).append("`.`").append(dto.getStableName()).append("`");
            StringBuilder tags = new StringBuilder("(");
            StringBuilder tagValues = new StringBuilder("(");
            dto.getTagValueMap().forEach((k, v) -> {
                tags.append(k).append(",");
                tagValues.append("'").append(v).append("'").append(",");
            });
            sb.append(tags.substring(0, tags.length() - 1)).append(") TAGS ").append(tagValues.substring(0, tagValues.length() - 1)).append(") ");
        }
        RestConnectionUtils.executeUpdate(connection, Collections.singletonList(sb.toString() + ";"));
    }

    /**
     * 删除表，谨慎操作
     *
     * @param connection 连接
     * @param dbName     库名
     * @param tbName     表名
     */
    public static void deleteTable(ConnectionDTO connection, String dbName, String tbName) {
        RestConnectionUtils.executeUpdate(connection, Collections.singletonList("DROP TABLE IF EXISTS `" + dbName + "`.`" + tbName + "`;"));
    }

    /**
     * 获取所有表
     *
     * @param connection 连接
     * @param db         库
     * @return 表列表
     */
    public static List<TableResDTO> getAllTable(ConnectionDTO connection, String db) {
        return RestConnectionUtils.executeQuery(connection, "show `" + db + "`.tables;", TableResDTO.class);
    }

    /**
     * 生成字段类型
     *
     * @param field 字段
     * @return 类型+长度
     */
    private static String getFieldType(TableFieldDTO field) {
        if (ObjectUtils.isEmpty(field.getLength())) {
            return field.getDataType();
        } else {
            return field.getDataType() + "(" + field.getLength() + ")";
        }
    }

    /**
     * 获取单个表
     *
     * @param connection 连接
     * @param db         库
     * @param tb         表
     * @return 表
     */
    public static TableResDTO getStable(ConnectionDTO connection, String db, String tb) {
        List<TableResDTO> allStb = getAllTable(connection, db);
        for (TableResDTO stbTmp : allStb) {
            if (stbTmp.getName().equals(tb)) {
                return stbTmp;
            }
        }
        return null;
    }

    /**
     * 获取表字段信息
     *
     * @param connection 连接
     * @param dbName     库
     * @param tbName     表
     * @return 字段列表
     */
    public static List<TableFieldDTO> getTableField(ConnectionDTO connection, String dbName, String tbName) {
        List<TableFieldResDTO> fieldList = RestConnectionUtils.executeQuery(connection, "DESCRIBE `" + dbName + "`.`" + tbName + "`;", TableFieldResDTO.class);

        return fieldList.stream().map(f -> {
            TableFieldDTO tf = new TableFieldDTO();
            tf.setLength(f.getLength());
            tf.setDataType(f.getType());
            tf.setName(f.getField());
            tf.setIsTag("TAG".equals(f.getNote()));
            return tf;
        }).collect(Collectors.toList());
    }

    /**
     * 获取建表语句
     *
     * @param connection 连接
     * @param dbName     库
     * @param tb         超级表
     * @return SQL
     */
    public static String getTableSql(ConnectionDTO connection, String dbName, String tb) {
        List<TableCreateResDTO> list = RestConnectionUtils.executeQuery(connection, "SHOW CREATE TABLE  `" + dbName + "`.`" + tb + "`;", TableCreateResDTO.class);
        return list.get(0).getCreateDbSql();
    }

    /**
     * 修改超级表
     *
     * @param connection      连接
     * @param stableUpdateDTO 更新信息
     */
    public static void updateTable(ConnectionDTO connection, TableUpdateDTO stableUpdateDTO) {
        List<String> batchSql = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(stableUpdateDTO.getAddList())) {
            stableUpdateDTO.getAddList().forEach(f -> batchSql.add("ALTER TABLE `" + stableUpdateDTO.getDb() + "`.`" + stableUpdateDTO.getTb() + "` ADD " + ("COLUMN ") + f.getName() + " " + getFieldType(f) + ";"));
        }

        if (ObjectUtils.isNotEmpty(stableUpdateDTO.getDeleteList())) {
            stableUpdateDTO.getDeleteList().forEach(f -> batchSql.add("ALTER TABLE `" + stableUpdateDTO.getDb() + "`.`" + stableUpdateDTO.getTb() + "` DROP " + ("COLUMN ") + f.getName() + ";"));
        }

        if (ObjectUtils.isNotEmpty(stableUpdateDTO.getUpdateList())) {
            stableUpdateDTO.getUpdateList().forEach(f -> batchSql.add("ALTER TABLE `" + stableUpdateDTO.getDb() + "`.`" + stableUpdateDTO.getTb() + "` MODIFY  " + ("COLUMN ") + f.getName() + " " + getFieldType(f) + ";"));
        }

        if (ObjectUtils.isNotEmpty(stableUpdateDTO.getTagNameChangeMap())) {
            stableUpdateDTO.getTagNameChangeMap().forEach((k, v) -> batchSql.add("ALTER TABLE `" + stableUpdateDTO.getDb() + "`.`" + stableUpdateDTO.getTb() + "` SET  TAG " + k + " = '" + v + "';"));
        }

        RestConnectionUtils.executeUpdate(connection, batchSql);
    }
}
