package com.gitee.dbquery.influxdbgui.store;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * H2ConnectionPool
 *
 * @author pc
 * @since 2024/02/01
 **/
public class H2ConnectionPool {
    private static final String url = "jdbc:h2:~/influx_tsdb_jui";
    private static final String username = "sa";
    private static final String password = "";
    private static final HikariConfig config = new HikariConfig();
    private static final HikariDataSource dataSource;

    static {
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        // 设置连接池大小，默认为10
        config.setMaximumPoolSize(10);
        dataSource = new HikariDataSource(config);
    }

    public static void close() {
        dataSource.close();
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
