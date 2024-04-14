package com.gitee.dbquery.influxdbgui.sdk.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.gitee.dbquery.influxdbgui.model.ConnectionModel;
import com.gitee.dbquery.influxdbgui.sdk.annotation.TdField;
import com.gitee.dbquery.influxdbgui.sdk.dto.ConnectionDTO;
import com.gitee.dbquery.influxdbgui.sdk.dto.QueryRstDTO;
import com.gitee.dbquery.influxdbgui.sdk.dto.res.BaseResDTO;
import com.gitee.dbquery.influxdbgui.util.ObjectUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.*;

/**
 * @author chenpi
 * @since 2024/3/19
 **/
@Slf4j
public class RestConnectionUtils {
    public final static Map<Integer, String> columnMetaTypeMap = new HashMap<>();
    static {
        columnMetaTypeMap.put(1, "BOOL");
        columnMetaTypeMap.put(2, "TINYINT");
        columnMetaTypeMap.put(3, "SMALLINT");
        columnMetaTypeMap.put(4, "INT");
        columnMetaTypeMap.put(5, "BIGINT");
        columnMetaTypeMap.put(6, "FLOAT");
        columnMetaTypeMap.put(7, "DOUBLE");
        columnMetaTypeMap.put(8, "BINARY");
        columnMetaTypeMap.put(9, "TIMESTAMP");
        columnMetaTypeMap.put(10, "NCHAR");
    }


    public static String getServerVersion(ConnectionModel connectionModel) {
        ConnectionDTO connectionDTO = new ConnectionDTO();
        connectionDTO.setIp(connectionModel.getIp());
        connectionDTO.setRestfulPort(connectionModel.getPort());
        connectionDTO.setUsername(connectionModel.getUsername());
        connectionDTO.setPassword(connectionModel.getPassword());
        BaseResDTO baseResDTO = executeSql(connectionDTO, "select server_version();");
        return baseResDTO.getData().get(0).get(0).toString();
    }

    public static BaseResDTO executeSql(ConnectionDTO connectionDTO, String sql) {
        HttpRequest request = HttpUtil.createPost("http://"+connectionDTO.getIp()+":"+connectionDTO.getRestfulPort()+"/query?epoch=ms"
                + (connectionDTO.getDb()==null ? "":("&db=" + connectionDTO.getDb()))
                + (connectionDTO.getUsername()==null ? "":("&u=" + connectionDTO.getUsername()))
                + (connectionDTO.getPassword()==null ? "":("&p=" + connectionDTO.getPassword()))
                + (connectionDTO.getDb()==null ? "":("&db=" + connectionDTO.getDb()))
                + "&q=" + sql);
        request.header("Authorization", "Basic " + Base64.encode(connectionDTO.getUsername() + ":" + connectionDTO.getPassword()));
        request.body(sql);
        HttpResponse response = request.execute();
        JSONObject jsonObject =  JSONUtil.parseObj(response.body());

        BaseResDTO baseResDTO = new BaseResDTO();

        List results = (List) jsonObject.get("results");
        JSONObject firstResult = (JSONObject) results.get(0);
        List series = (List) firstResult.get("series");

        if(ObjectUtils.isNotEmpty(series)) {
            JSONObject firstSeries = (JSONObject) series.get(0);
            List<String> columns = (List<String>) firstSeries.get("columns");

            List<List<Object>> column_meta = new ArrayList<>();
            for(String column:columns) {
                column_meta.add(Collections.singletonList(column));
            }
            baseResDTO.setColumn_meta(column_meta);
            baseResDTO.setData((List<List<Object>>) firstSeries.get("values"));
        }




        baseResDTO.setStatus("succ");
        if(baseResDTO.getColumn_meta() == null) {
            baseResDTO.setColumn_meta(Collections.emptyList());
        }
        if(baseResDTO.getData() == null) {
            baseResDTO.setData(Collections.emptyList());
        }


        if(!new Integer(0).equals(baseResDTO.getCode())  && !"succ".equals(baseResDTO.getStatus())) {
            throw new RuntimeException(baseResDTO.getDesc());
        }

        return baseResDTO;
    }

    public static <T> List<T> executeQuery(ConnectionDTO connection, String sql, Class<T> clazz) {
        log.info("======> 执行 TD SQL： {}", sql);
        if (ObjectUtils.isEmpty(sql)) {
            return Collections.emptyList();
        }

        try {

            BaseResDTO baseResDTO  = executeSql(connection, sql);
            Field[] fields = clazz.getDeclaredFields();
            List<T> list = new ArrayList<>();
            for (List<Object> record : baseResDTO.getData()) {
                Map<String, Object> dbObj = getDbObj(baseResDTO.getColumn_meta(), record);

                T obj = clazz.newInstance();
                for (Field field : fields) {
                    TdField fieldAnnotation = field.getAnnotation(TdField.class);
                    if (fieldAnnotation == null) {
                        continue;
                    }
                    String columnName = fieldAnnotation.value();
                    setValue(obj, columnName, field, dbObj);
                }
                list.add(obj);
            }
            return list;
        } catch (SQLException | IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
            log.error(e.toString(), e);
            log.error("======> 执行SQL失败， sql:{}, error：{}", sql, e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 执行查询操作
     * 注意Connection需要调用方手动关闭
     *
     * @param connection 连接
     * @param sql        SQL语句
     * @return 查询结果
     */
    public static QueryRstDTO executeQuery(ConnectionDTO connection, String sql) {
        log.info("======> 执行 TD SQL： {}", sql);
        if (ObjectUtils.isEmpty(sql)) {
            return null;
        }

        try {
            BaseResDTO baseResDTO  = executeSql(connection, sql);
            List<Map<String, Object>> list = new ArrayList<>();

            List<List<Object>> md = baseResDTO.getColumn_meta();
            List<String> columnList = new ArrayList<>();
            for (int i = 0; i < md.size(); i++) {
                columnList.add(md.get(i).get(0).toString());
            }
            for (List<Object> record : baseResDTO.getData()) {
                Map<String, Object> dbObj = getDbObj(baseResDTO.getColumn_meta(), record);
                list.add(dbObj);
            }
            return new QueryRstDTO(columnList, list);
        } catch (Exception e) {
            log.error(e.toString(), e);
            log.error("======> 执行SQL失败， sql:{}, error：{}", sql, e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }


    /**
     * 执行更新操作
     * 注意Connection需要调用方手动关闭
     *
     * @param connection java.sql.Connection连接
     * @param batchSql   sql语句列表
     * @return an array of update counts containing one element for each
     * command in the batch.  The elements of the array are ordered according
     * to the order in which commands were added to the batch.
     */
    public static List<QueryRstDTO> executeUpdate(ConnectionDTO connection, List<String> batchSql) {
        log.info("======> 执行 TD SQL： {}", batchSql);
        if (ObjectUtils.isEmpty(batchSql)) {
            return Collections.emptyList();
        }
        try {
            List<QueryRstDTO> resList = new ArrayList<>();
            for (String sqlTmp : batchSql) {
                resList.add(executeQuery(connection, sqlTmp));
            }
            return resList;
        } catch (Exception e) {
            log.error(e.toString(), e);
            log.error("======> 执行SQL失败， sql:{}, error：{}", batchSql, e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    private static Map<String, Object> getDbObj  (List<List<Object>> columnMeta, List<Object> record) {
        Map<String, Object> obj = new HashMap<>();
        for(int i = 0; i < columnMeta.size(); i++) {
            obj.put(columnMeta.get(i).get(0).toString(), record.get(i));
        }
        return obj;
    }

    private static void setValue(Object obj, String columnName, Field field, Map<String, Object> rs) throws NoSuchMethodException, SQLException, InvocationTargetException, IllegalAccessException {
        String tdColumnName = ObjectUtils.isEmpty(columnName) ? StrUtil.toUnderlineCase(field.getName()) : columnName;
        if(rs.get(tdColumnName) == null) {
            return;
        }

        String fieldName = field.getName();
        String setMethodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        Method setMethod = obj.getClass().getMethod(setMethodName, field.getType());
        Class<?> fieldType = field.getType();
        if (fieldType.equals(String.class)) {
            setMethod.invoke(obj, rs.get(tdColumnName) + "");
        } else if (fieldType.equals(Double.class) || fieldType.equals(Double.TYPE)) {
            setMethod.invoke(obj, Double.valueOf(rs.get(tdColumnName) + ""));
        } else if (fieldType.equals(Integer.class) || fieldType.equals(Integer.TYPE)) {
            setMethod.invoke(obj, Integer.valueOf(rs.get(tdColumnName) + ""));
        } else if (fieldType.equals(Float.class) || fieldType.equals(Float.TYPE)) {
            setMethod.invoke(obj, Float.valueOf(rs.get(tdColumnName) + ""));
        } else if (fieldType.equals(Long.class) || fieldType.equals(Long.TYPE)) {
            setMethod.invoke(obj, Long.valueOf(rs.get(tdColumnName) + ""));
        } else if (fieldType.equals(Boolean.class) || fieldType.equals(Boolean.TYPE)) {
            setMethod.invoke(obj, Boolean.valueOf(rs.get(tdColumnName) + ""));
        } else {
            setMethod.invoke(obj, rs.get(tdColumnName));
        }
    }


    public static void main(String[] args) {
        ConnectionDTO connectionDTO1 = new ConnectionDTO();
        connectionDTO1.setIp("10.162.201.62");
        connectionDTO1.setRestfulPort("6041");
        connectionDTO1.setUsername("root");
        connectionDTO1.setPassword("Abc123_");
        connectionDTO1.setVersion("2.6.0.34");



        ConnectionDTO connectionDTO2 = new ConnectionDTO();
        connectionDTO2.setIp("10.162.201.112");
        connectionDTO2.setRestfulPort("6041");
        connectionDTO2.setUsername("root");
        connectionDTO2.setPassword("taosdata");
        connectionDTO2.setVersion("3.2.3.0");

//        List<DatabaseResDTO> list = executeQuery(connectionDTO2, "show databases;", DatabaseResDTO.class);
//        System.out.println(list);

        QueryRstDTO res = executeQuery(connectionDTO2, "create STABLE ptest.p_stable (time TIMESTAMP,tag_value DOUBLE,is_good BOOL) tags (tag_name NCHAR(120),company_code TINYINT,point_source NCHAR(8),alis NCHAR(50));");
        System.out.println(res);
    }
}
