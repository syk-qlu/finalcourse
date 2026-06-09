package dao;

import model.User;
import model.FriendRequest;
import util.ImageUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FriendshipDAO {

    /**
     * 获取用户的好友列表
     */
    public List<User> getUserFriends(int userId) {
        List<User> friends = new ArrayList<>();
        String sql = "SELECT u.* FROM users u " +
                "INNER JOIN friendships f ON (u.user_id = f.friend_id OR u.user_id = f.user_id) " +
                "WHERE (f.user_id = ? OR f.friend_id = ?) AND f.status = 'accepted' " +
                "AND u.user_id != ? " +
                "ORDER BY f.is_top DESC, f.top_time DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, userId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                friends.add(extractUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeAll(conn, pstmt, rs);
        }
        return friends;
    }

    /**
     * 检查是否为好友
     */
    public boolean isFriend(int userId1, int userId2) {
        String sql = "SELECT COUNT(*) FROM friendships " +
                "WHERE ((user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)) " +
                "AND status = 'accepted'";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId1);
            pstmt.setInt(2, userId2);
            pstmt.setInt(3, userId2);
            pstmt.setInt(4, userId1);
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
     * 添加好友
     */
    public boolean addFriend(int userId1, int userId2) {
        String sql = "INSERT INTO friendships (user_id, friend_id, status) " +
                "VALUES (?, ?, 'accepted'), (?, ?, 'accepted')";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId1);
            pstmt.setInt(2, userId2);
            pstmt.setInt(3, userId2);
            pstmt.setInt(4, userId1);
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
     * 删除好友
     */
    public boolean removeFriend(int userId1, int userId2) {
        String sql = "DELETE FROM friendships WHERE " +
                "(user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId1);
            pstmt.setInt(2, userId2);
            pstmt.setInt(3, userId2);
            pstmt.setInt(4, userId1);
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
     * 置顶好友
     */
    public boolean topFriend(int userId, int friendId) {
        String sql = "UPDATE friendships SET is_top = 1, top_time = ? " +
                "WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, System.currentTimeMillis());
            pstmt.setInt(2, userId);
            pstmt.setInt(3, friendId);
            pstmt.setInt(4, friendId);
            pstmt.setInt(5, userId);
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
     * 取消置顶好友
     */
    public boolean unTopFriend(int userId, int friendId) {
        String sql = "UPDATE friendships SET is_top = 0, top_time = 0 " +
                "WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, friendId);
            pstmt.setInt(3, friendId);
            pstmt.setInt(4, userId);
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
     * 检查是否已置顶
     */
    public boolean isTopped(int userId, int friendId) {
        String sql = "SELECT is_top FROM friendships " +
                "WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, friendId);
            pstmt.setInt(3, friendId);
            pstmt.setInt(4, userId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("is_top") == 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeAll(conn, pstmt, rs);
        }
        return false;
    }

    /**
     * 发送好友请求
     */
    public boolean sendFriendRequest(int fromUserId, int toUserId, String message) {
        String sql = "INSERT INTO friend_requests (from_user_id, to_user_id, status, message) " +
                "VALUES (?, ?, 'pending', ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, fromUserId);
            pstmt.setInt(2, toUserId);
            pstmt.setString(3, message);
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
     * 获取好友请求列表
     */
    public List<FriendRequest> getFriendRequests(int userId) {
        List<FriendRequest> requests = new ArrayList<>();
        String sql = "SELECT fr.*, u.username as from_username FROM friend_requests fr " +
                "INNER JOIN users u ON fr.from_user_id = u.user_id " +
                "WHERE fr.to_user_id = ? AND fr.status = 'pending' " +
                "ORDER BY fr.created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                FriendRequest request = new FriendRequest();
                request.setRequestId(rs.getInt("request_id"));
                request.setFromUserId(rs.getInt("from_user_id"));
                request.setFromUsername(rs.getString("from_username"));
                request.setToUserId(rs.getInt("to_user_id"));
                request.setStatus(rs.getString("status"));
                request.setMessage(rs.getString("message"));
                requests.add(request);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeAll(conn, pstmt, rs);
        }
        return requests;
    }

    /**
     * 接受好友请求
     */
    public boolean acceptFriendRequest(int requestId, int fromUserId, int toUserId) {
        String sql1 = "UPDATE friend_requests SET status = 'accepted' WHERE request_id = ?";
        String sql2 = "INSERT INTO friendships (user_id, friend_id, status) " +
                "VALUES (?, ?, 'accepted'), (?, ?, 'accepted')";
        Connection conn = null;
        PreparedStatement pstmt1 = null;
        PreparedStatement pstmt2 = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            pstmt1 = conn.prepareStatement(sql1);
            pstmt1.setInt(1, requestId);
            pstmt1.executeUpdate();

            pstmt2 = conn.prepareStatement(sql2);
            pstmt2.setInt(1, fromUserId);
            pstmt2.setInt(2, toUserId);
            pstmt2.setInt(3, toUserId);
            pstmt2.setInt(4, fromUserId);
            pstmt2.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DBConnection.closeStatement(pstmt1);
            DBConnection.closeStatement(pstmt2);
            DBConnection.closeConnection(conn);
        }
        return false;
    }

    /**
     * 拒绝好友请求
     */
    public boolean rejectFriendRequest(int requestId) {
        String sql = "UPDATE friend_requests SET status = 'rejected' WHERE request_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, requestId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeStatement(pstmt);
            DBConnection.closeConnection(conn);
        }
        return false;
    }

    private User extractUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setStatus(rs.getString("status"));

        byte[] avatarData = rs.getBytes("avatar");
        if (avatarData != null) {
            user.setHeadIcon(ImageUtil.bytesToImageIcon(avatarData));
        } else {
            user.setHeadIcon(ImageUtil.createDefaultAvatar(user.getUsername()));
        }

        return user;
    }
}
