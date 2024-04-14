package com.gitee.dbquery.influxdbgui.sdk.util;

import com.gitee.dbquery.influxdbgui.sdk.dto.stb.StableUpdateDTO;
import com.gitee.dbquery.influxdbgui.sdk.exception.TableAlreadyExistException;
import com.gitee.dbquery.influxdbgui.util.ObjectUtils;
import com.gitee.dbquery.influxdbgui.sdk.dto.ConnectionDTO;
import com.gitee.dbquery.influxdbgui.sdk.dto.QueryRstDTO;
import com.gitee.dbquery.influxdbgui.sdk.dto.field.TableFieldDTO;
import com.gitee.dbquery.influxdbgui.sdk.dto.res.StableCreateResDTO;
import com.gitee.dbquery.influxdbgui.sdk.dto.stb.StableAddDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 超级表工具类
 *
 * @author 风一样的码农
 * @since 2023/8/18
 **/
public class SuperTableUtils {

    /**
     * 创建超级表
     *
     * @param connection   连接
     * @param stableAddDTO 建表信息
     */
    public static void createStable(ConnectionDTO connection, StableAddDTO stableAddDTO) {
        List<TableFieldDTO> fieldList = stableAddDTO.getFieldList().stream().filter(f -> !f.getIsTag()).collect(Collectors.toList());
        List<TableFieldDTO> tagList = stableAddDTO.getFieldList().stream().filter(TableFieldDTO::getIsTag).collect(Collectors.toList());

        StringBuilder sql = new StringBuilder("create " + (ObjectUtils.isEmpty(tagList) ? "TABLE " : "STABLE ") + "`" +  stableAddDTO.getDb() + "`.`");
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
     * 删除超级表，谨慎操作
     *
     * @param connection 连接
     * @param dbName     库名
     * @param tbName     表名
     */
    public static void deleteStable(ConnectionDTO connection, String dbName, String tbName) {
        connection.setDb(dbName);
        RestConnectionUtils.executeUpdate(connection, Collections.singletonList("DROP MEASUREMENT \"" + tbName + "\";"));
    }

    /**
     * 获取所有超级表
     *
     * @param connection 连接
     * @param db         库
     * @return 超级表列表
     */
    public static QueryRstDTO getAllStable(ConnectionDTO connection, String db) {
//        if(VersionUtils.compareVersion(connection.getVersion(), "3.0") > 0) {
//            QueryRstDTO queryRstDTO =  RestConnectionUtils.executeQuery(connection, "select  stable_name as name, * from INFORMATION_SCHEMA.INS_STABLES where db_name = '"+db+"';");
//
//            if(null!= queryRstDTO) {
//                if(ObjectUtils.isNotEmpty(queryRstDTO.getColumnList())) {
//                    queryRstDTO.getColumnList().remove("stable_name");
//                }
//
//                for(Map<String, Object> map : queryRstDTO.getDataList()) {
//                    if(map.get("stable_name") !=null) {
//                        map.remove("stable_name");
//                    }
//                }
//            }
//
//
//            return queryRstDTO;
//        }
        return RestConnectionUtils.executeQuery(connection, "SHOW MEASUREMENTS ON " + db + ";");
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
     * 获取单个超级表
     *
     * @param connection 连接
     * @param db         库
     * @param stb        超级表
     * @return 超级表
     */
    public static Map<String, Object> getStable(ConnectionDTO connection, String db, String stb) {
        QueryRstDTO allStb = getAllStable(connection, db);
        for (Map<String, Object> stbTmp : allStb.getDataList()) {
            if (stbTmp.get("name").equals(stb)) {
                return stbTmp;
            }
        }
        return null;
    }

    /**
     * 获取超级表字段信息
     *
     * @param connection 连接
     * @param dbName     库
     * @param tbName     表
     * @return 字段列表
     */
    public static List<TableFieldDTO> getStableField(ConnectionDTO connection, String dbName, String tbName) {
        QueryRstDTO fieldList = RestConnectionUtils.executeQuery(connection, "DESCRIBE `" + dbName + "`.`" + tbName + "`;");

        return fieldList.getDataList().stream().map(f -> {
            TableFieldDTO tf = new TableFieldDTO();
            tf.setLength(f.get("length") == null ? Integer.valueOf((f.get("Length") + "")) :Integer.valueOf( f.get("length").toString()));
            tf.setDataType(f.get("type") == null ? (f.get("Type") + "") : f.get("type").toString());
            tf.setName(f.get("field") == null ? (f.get("Field") + "") : f.get("field").toString());
            tf.setIsTag("TAG".equals(f.get("note")) || "TAG".equals(f.get("Note")));
            return tf;
        }).collect(Collectors.toList());
    }

    /**
     * 获取建表语句
     *
     * @param connection 连接
     * @param dbName     库
     * @param stb        超级表
     * @return SQL
     */
    public static String getStableSql(ConnectionDTO connection, String dbName, String stb) {
        List<StableCreateResDTO> list = RestConnectionUtils.executeQuery(connection, "SHOW CREATE STABLE  `" + dbName + "`.`" + stb + "`;", StableCreateResDTO.class);
        return list.get(0).getCreateDbSql();
    }

    /**
     * 修改超级表
     *
     * @param connection      连接
     * @param stableUpdateDTO 更新信息
     */
    public static void updateStable(ConnectionDTO connection, StableUpdateDTO stableUpdateDTO) {
        List<String> batchSql = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(stableUpdateDTO.getAddList())) {
            stableUpdateDTO.getAddList().forEach(f -> batchSql.add("ALTER STABLE `" + stableUpdateDTO.getDb() + "`.`" + stableUpdateDTO.getTb() + "` ADD " + (f.getIsTag() ? "TAG " : "COLUMN ") + f.getName() + " " + getFieldType(f) + ";"));
        }

        if (ObjectUtils.isNotEmpty(stableUpdateDTO.getDeleteList())) {
            stableUpdateDTO.getDeleteList().forEach(f -> batchSql.add("ALTER STABLE `" + stableUpdateDTO.getDb() + "`.`" + stableUpdateDTO.getTb() + "` DROP " + (f.getIsTag() ? "TAG " : "COLUMN ") + f.getName() + ";"));
        }

        if (ObjectUtils.isNotEmpty(stableUpdateDTO.getUpdateList())) {
            stableUpdateDTO.getUpdateList().forEach(f -> batchSql.add("ALTER STABLE `" + stableUpdateDTO.getDb() + "`.`" + stableUpdateDTO.getTb() + "` MODIFY  " + (f.getIsTag() ? "TAG " : "COLUMN ") + f.getName() + " " + getFieldType(f) + ";"));
        }

        if (ObjectUtils.isNotEmpty(stableUpdateDTO.getTagNameChangeMap())) {
            stableUpdateDTO.getTagNameChangeMap().forEach((k, v) -> batchSql.add("ALTER STABLE `" + stableUpdateDTO.getDb() + "`.`" + stableUpdateDTO.getTb() + "` CHANGE  TAG " + k + " " + v + ";"));
        }

        RestConnectionUtils.executeUpdate(connection, batchSql);
    }
}
