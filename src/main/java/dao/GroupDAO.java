package dao;

import model.Group;
import model.User;
import util.ImageUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupDAO {

    /**
     * 创建群组
     */
    public int createGroup(String groupName, int creatorId, String description) {
        String sql = "INSERT INTO `groups` (group_name, creator_id, description) VALUES (?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, groupName);
            pstmt.setInt(2, creatorId);
            pstmt.setString(3, description);
            pstmt.executeUpdate();

            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeAll(conn, pstmt, rs);
        }
        return -1;
    }

    /**
     * 添加群成员
     */
    public boolean addGroupMember(int groupId, int userId, String role) {
        String sql = "INSERT INTO group_members (group_id, user_id, role) VALUES (?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, groupId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, role);
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
     * 获取用户加入的所有群组
     */
    public List<Group> getUserGroups(int userId) {
        List<Group> groups = new ArrayList<>();
        String sql = "SELECT g.*, u.username as creator_name, COUNT(gm.user_id) as member_count " +
                "FROM `groups` g " +
                "INNER JOIN users u ON g.creator_id = u.user_id " +
                "INNER JOIN group_members gm ON g.group_id = gm.group_id " +
                "WHERE gm.user_id = ? " +
                "GROUP BY g.group_id " +
                "ORDER BY g.created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Group group = new Group();
                group.setGroupId(rs.getInt("group_id"));
                group.setGroupName(rs.getString("group_name"));
                group.setCreatorId(rs.getInt("creator_id"));
                group.setCreatorName(rs.getString("creator_name"));
                group.setDescription(rs.getString("description"));
                group.setMemberCount(rs.getInt("member_count"));
                groups.add(group);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeAll(conn, pstmt, rs);
        }
        return groups;
    }

    /**
     * 获取群组详情
     */
    public Group getGroupById(int groupId) {
        String sql = "SELECT g.*, u.username as creator_name, COUNT(gm.user_id) as member_count " +
                "FROM `groups` g " +
                "INNER JOIN users u ON g.creator_id = u.user_id " +
                "LEFT JOIN group_members gm ON g.group_id = gm.group_id " +
                "WHERE g.group_id = ? " +
                "GROUP BY g.group_id";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, groupId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                Group group = new Group();
                group.setGroupId(rs.getInt("group_id"));
                group.setGroupName(rs.getString("group_name"));
                group.setCreatorId(rs.getInt("creator_id"));
                group.setCreatorName(rs.getString("creator_name"));
                group.setDescription(rs.getString("description"));
                group.setMemberCount(rs.getInt("member_count"));
                return group;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeAll(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 获取群组成员
     */
    public List<User> getGroupMembers(int groupId) {
        List<User> members = new ArrayList<>();
        String sql = "SELECT u.* FROM users u " +
                "INNER JOIN group_members gm ON u.user_id = gm.user_id " +
                "WHERE gm.group_id = ? " +
                "ORDER BY gm.role DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, groupId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                members.add(extractUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeAll(conn, pstmt, rs);
        }
        return members;
    }

    /**
     * 检查用户是否在群组中
     */
    public boolean isMember(int groupId, int userId) {
        String sql = "SELECT COUNT(*) FROM group_members WHERE group_id = ? AND user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, groupId);
            pstmt.setInt(2, userId);
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
