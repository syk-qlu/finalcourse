package dao;

import model.User;
import util.ImageUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    /**
     * 添加新用户
     */
    public boolean addUser(String username, String password, String email) {
        String sql = "INSERT INTO users (username, password, email, status) VALUES (?, ?, ?, 'offline')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, encryptPassword(password));
            if (email != null && !email.trim().isEmpty()) {
                pstmt.setString(3, email.trim());
            } else {
                pstmt.setNull(3, Types.VARCHAR);
            }
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取用户（通过用户名和密码验证）
     */
    public User getUserByCredentials(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, encryptPassword(password));
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeAll(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 通过ID获取用户
     */
    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeAll(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 通过用户名获取用户
     */
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeAll(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 更新用户状态
     */
    public boolean updateUserStatus(int userId, String status) {
        String sql = "UPDATE users SET status = ? WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeStatement(pstmt);
            DBConnection.closeConnection(conn);
        }
        return false;
    }

    /**
     * 获取所有在线用户
     */
    public List<User> getOnlineUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE status = 'online'";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                users.add(extractUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeAll(conn, stmt, rs);
        }
        return users;
    }

    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY username ASC";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                users.add(extractUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeAll(conn, stmt, rs);
        }
        return users;
    }

    /**
     * 检查用户名是否存在
     */
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeAll(conn, pstmt, rs);
        }
        return false;
    }

    /**
     * 提取User对象
     */
    private User extractUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setStatus(rs.getString("status"));

        // 从数据库加载头像或使用默认头像
        byte[] avatarData = rs.getBytes("avatar");
        if (avatarData != null) {
            user.setHeadIcon(ImageUtil.bytesToImageIcon(avatarData));
        } else {
            user.setHeadIcon(ImageUtil.createDefaultAvatar(user.getUsername()));
        }

        return user;
    }

    /**
     * 更新用户名
     */
    public boolean updateUsername(int userId, String newUsername) {
        String sql = "UPDATE users SET username = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newUsername);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 更新密码
     */
    public boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, encryptPassword(newPassword));   // 使用已有的加密方法
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 密码加密（未完成）
     */
    private String encryptPassword(String password) {
        return password;
    }
}