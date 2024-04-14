package com.gitee.dbquery.influxdbgui.store;

import com.gitee.dbquery.influxdbgui.util.ObjectUtils;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 风一样的码农
 * @since 2024/2/1
 **/
public class H2DbUtils {
    public static boolean createTable(String tableName, Map<String, Object> fieldMap) throws SQLException {
        if (ObjectUtils.isEmpty(tableName)) {
            return false;
        }
        if (fieldMap == null) {
            return false;
        }

        try (Connection conn = H2ConnectionPool.getConnection(); Statement stmt = conn.createStatement()) {
            // 连接到H2数据库
            // 创建Statement对象
            // 创建表的SQL语句
            String sql = getCreateSql(tableName, fieldMap);
            // 执行SQL语句
            stmt.executeUpdate(sql);
        }
        return true;
    }

    public static boolean dropTable(String tableName) throws SQLException {
        if (ObjectUtils.isEmpty(tableName)) {
            return false;
        }

        try (Connection conn = H2ConnectionPool.getConnection(); Statement stmt = conn.createStatement()) {
            // 连接到H2数据库
            // 创建Statement对象
            String sql = "DROP TABLE " + tableName;
            stmt.executeUpdate(sql);
        }
        return true;
    }

    private static String getCreateSql(String tableName, Map<String, Object> fieldMap) {
        StringBuilder builder = new StringBuilder("CREATE TABLE " + tableName + " (");
        fieldMap.forEach((key, value) -> {
            builder.append(key);
            builder.append(" ");
            if (value.equals(Long.class)) {
                builder.append("bigint");
            } else if (value.equals(Integer.class)) {
                builder.append("int");
            } else if (value.equals(String.class)) {
                builder.append("varchar");
            } else if (value.equals(BigDecimal.class)) {
                builder.append("decimal");
            } else if (value.equals(Double.class)) {
                builder.append("double");
            } else if (value.equals(Boolean.class)) {
                builder.append("int");
            } else if (value.equals(LocalDateTime.class)) {
                builder.append("datetime");
            } else {
                throw new RuntimeException("invalid field type");
            }
            builder.append(",");
        });
        builder.deleteCharAt(builder.length() - 1);
        return builder + ")";
    }

    private static String getInsertSql(String tableName, Map<String, Object> map, HashMap<String, Integer> indexMap) {
        StringBuilder builder = new StringBuilder("INSERT INTO " + tableName + " (");
        StringBuilder builder2 = new StringBuilder(" VALUES (");
        AtomicInteger index = new AtomicInteger(1);
        map.forEach((key, value) -> {
            builder.append(key);
            builder.append(" ");
            builder.append(",");
            builder2.append("?");
            builder2.append(" ");
            builder2.append(",");
            indexMap.put(key, index.get());
            index.getAndIncrement();
        });
        builder.deleteCharAt(builder.length() - 1);
        builder2.deleteCharAt(builder2.length() - 1);
        // 插入表的SQL语句
        return builder + ")" + builder2 + ")";
    }

    public static boolean insertByHashMap(String tableName, List<Map<String, Object>> listMap) throws SQLException {

        Connection connection = null;
        PreparedStatement statement = null;
        try {
            // 连接到H2数据库
            connection = H2ConnectionPool.getConnection();
            HashMap<String, Integer> indexMap = new HashMap<>(listMap.get(0).size());
            String sql = getInsertSql(tableName, listMap.get(0), indexMap);
            statement = connection.prepareStatement(sql);

            setValue(listMap, indexMap, statement);
            // 执行批处理
            int[] rowsInserted = statement.executeBatch();
            if (rowsInserted.length != listMap.size()) {
                return false;
            }
        } finally {
            if (connection != null) {
                connection.close();
            }
            if (statement != null) {
                statement.close();
            }
        }

        return true;
    }

    public static boolean executeUpdate(String sql) throws SQLException {
        try (Connection connection = H2ConnectionPool.getConnection(); Statement statement = connection.createStatement()) {
            // 连接到H2数据库
            // 执行批处理
            return statement.execute(sql);
        }
    }

    public static List<Map<String, Object>> query(String sql) throws SQLException {
        // 创建List对象来存储查询结果
        List<Map<String, Object>> resultList = new ArrayList<>();
        try (Connection connection = H2ConnectionPool.getConnection(); Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            // 执行查询
            // 获取结果集的元数据
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // 处理查询结果
            while (rs.next()) {
                HashMap<String, Object> map = new HashMap<>(columnCount);
                for (int i = 1; i <= columnCount; i++) {
                    map.put(metaData.getColumnName(i), rs.getObject(i));
                }
                resultList.add(map);
            }

        }
        return resultList;
    }

    private static void setValue(List<Map<String, Object>> listMap, HashMap<String, Integer> indexMap,
                                 PreparedStatement statement) throws SQLException {
        // 批量插入数据
        for (Map<String, Object> m : listMap) {
            m.forEach((key, value) -> {
                if (value instanceof Long) {
                    try {
                        statement.setLong(indexMap.get(key), (Long) value);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                } else if (value instanceof Integer) {
                    try {
                        statement.setInt(indexMap.get(key), (Integer) value);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                } else if (value instanceof String) {
                    try {
                        statement.setString(indexMap.get(key), (String) value);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                } else if (value instanceof BigDecimal) {
                    try {
                        statement.setBigDecimal(indexMap.get(key), (BigDecimal) value);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                } else if (value instanceof Double) {
                    try {
                        statement.setDouble(indexMap.get(key), (Double) value);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                } else if (value instanceof Boolean) {
                    int flag = (Boolean) value ? 1 : 0;
                    try {
                        statement.setInt(indexMap.get(key), flag);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                } else if (value instanceof Byte) {
                    try {
                        statement.setByte(indexMap.get(key), (Byte) value);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                } else if (value instanceof LocalDateTime) {
                    try {
                        statement.setTimestamp(indexMap.get(key), Timestamp.valueOf(((LocalDateTime) value)));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            statement.addBatch();
        }
    }
}
