package dao;

import model.Message;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    /**
     * 保存消息
     */
    public boolean saveMessage(Message message) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, group_id, content, message_type, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, message.getSenderId());
            pstmt.setObject(2, message.getReceiverId() == 0 ? null : message.getReceiverId());
            pstmt.setObject(3, message.getGroupId());
            pstmt.setString(4, message.getContent());
            pstmt.setString(5, message.getMessageType());
            pstmt.setLong(6, message.getCreatedAt());
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
     * 获取两个用户之间的聊天记录
     */
    public List<Message> getConversation(int userId1, int userId2, int limit) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE " +
                "(sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?) " +
                "ORDER BY created_at ASC LIMIT ?";
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
            pstmt.setInt(5, limit);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                messages.add(extractMessage(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeAll(conn, pstmt, rs);
        }
        return messages;
    }

    /**
     * 撤回消息
     */
    public boolean recallMessage(long messageId, int userId) {
        String sql = "UPDATE messages SET is_deleted = true, deleted_at = ? " +
                "WHERE message_id = ? AND sender_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, System.currentTimeMillis());
            pstmt.setLong(2, messageId);
            pstmt.setInt(3, userId);
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
     * 获取群聊消息
     */
    public List<Message> getGroupMessages(int groupId, int limit) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT gm.message_id, gm.sender_id, gm.group_id, gm.content, gm.message_type, " +
                "gm.is_deleted, gm.created_at, u.username as sender_name " +
                "FROM group_messages gm " +
                "INNER JOIN users u ON gm.sender_id = u.user_id " +
                "WHERE gm.group_id = ? " +
                "ORDER BY gm.created_at ASC LIMIT ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, groupId);
            pstmt.setInt(2, limit);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Message msg = new Message();
                msg.setMessageId(rs.getLong("message_id"));
                msg.setSenderId(rs.getInt("sender_id"));
                msg.setSenderName(rs.getString("sender_name"));
                msg.setGroupId(rs.getInt("group_id"));
                msg.setContent(rs.getString("content"));
                msg.setMessageType(rs.getString("message_type"));
                msg.setDeleted(rs.getBoolean("is_deleted"));
                msg.setCreatedAt(rs.getLong("created_at"));
                messages.add(msg);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeAll(conn, pstmt, rs);
        }
        return messages;
    }

    /**
     * 保存群聊消息
     */
    public boolean saveGroupMessage(int groupId, int senderId, String content) {
        String sql = "INSERT INTO group_messages (group_id, sender_id, content, message_type, created_at) " +
                "VALUES (?, ?, ?, 'text', ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, groupId);
            pstmt.setInt(2, senderId);
            pstmt.setString(3, content);
            pstmt.setLong(4, System.currentTimeMillis());
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
     * 撤回群聊消息
     */
    public boolean recallGroupMessage(long messageId, int userId) {
        String sql = "UPDATE group_messages SET is_deleted = true, deleted_at = ? " +
                "WHERE message_id = ? AND sender_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, System.currentTimeMillis());
            pstmt.setLong(2, messageId);
            pstmt.setInt(3, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeStatement(pstmt);
            DBConnection.closeConnection(conn);
        }
        return false;
    }

    private Message extractMessage(ResultSet rs) throws SQLException {
        Message msg = new Message();
        msg.setMessageId(rs.getLong("message_id"));
        msg.setSenderId(rs.getInt("sender_id"));
        msg.setReceiverId(rs.getObject("receiver_id") != null ? rs.getInt("receiver_id") : 0);
        if (rs.getObject("group_id") != null) {
            msg.setGroupId(rs.getInt("group_id"));
        }
        msg.setContent(rs.getString("content"));
        msg.setMessageType(rs.getString("message_type"));
        msg.setDeleted(rs.getBoolean("is_deleted"));
        msg.setCreatedAt(rs.getLong("created_at"));
        return msg;
    }

    /**
     * 找到两个用户之间的最后一条消息内容
     * @param user1
     * @param user2
     * @return
     */
    public String getLastMessageContent(int user1, int user2) {
        String sql = "SELECT content FROM messages WHERE (sender_id=? AND receiver_id=?) OR (sender_id=? AND receiver_id=?) ORDER BY created_at DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, user1); ps.setInt(2, user2);
            ps.setInt(3, user2); ps.setInt(4, user1);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return "";
    }
    /**
     * 保存群文件消息（支持指定消息类型）
     */
    public boolean saveGroupFileMessage(int groupId, int senderId, String content, String messageType) {
        String sql = "INSERT INTO group_messages (group_id, sender_id, content, message_type, created_at) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, groupId);
            pstmt.setInt(2, senderId);
            pstmt.setString(3, content);          // 文件路径
            pstmt.setString(4, messageType);      // "file" 或 "image"
            pstmt.setLong(5, System.currentTimeMillis());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
