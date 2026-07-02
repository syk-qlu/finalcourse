package dao;

import java.sql.*;
import java.util.Properties;
import java.io.InputStream;

public class DBConnection {
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static String URL;
    private static String USER;
    private static String PASSWORD;

    static {
        try {
            Properties props = new Properties();
            InputStream input = DBConnection.class.getClassLoader()
                    .getResourceAsStream("database.properties");

            if (input != null) {
                props.load(input);
                URL = props.getProperty("db.url");
                USER = props.getProperty("db.user");
                PASSWORD = props.getProperty("db.password");
                input.close();
            }

            Class.forName(DRIVER);
        } catch (Exception e) {
            System.err.println("数据库连接初始化失败");
            e.printStackTrace();
        }
    }
//
    public static Connection getConnection() throws SQLException {
        if (URL == null || USER == null || PASSWORD == null) {
            throw new SQLException("数据库配置文件未正确加载");
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeAll(Connection conn, Statement stmt, ResultSet rs) {
        closeResultSet(rs);
        closeStatement(stmt);
        closeConnection(conn);
    }
}