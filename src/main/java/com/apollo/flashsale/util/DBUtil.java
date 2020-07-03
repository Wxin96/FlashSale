package com.apollo.flashsale.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * 类的用途:
 * UserUtil中使用, 将用户信息存入数据库
 * 采用方式:
 * 原始JDBC的方式
 */
public class DBUtil {

    // 数据库的属性
    private static Properties properties;

    // 加载配置
    static {
        try {
            InputStream in = DBUtil.class.getClassLoader().getResourceAsStream("application.properties");
            properties = new Properties();
            properties.load(in);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 获取连接
    public static Connection getConn() throws Exception {
        String url = properties.getProperty("spring.datasource.url");
        String username = properties.getProperty("spring.datasource.username");
        String password = properties.getProperty("spring.datasource.password");
        String driver = properties.getProperty("spring.datasource.driver-class-name");

        Class.forName(driver);
        return DriverManager.getConnection(url, username, password);
    }

}
