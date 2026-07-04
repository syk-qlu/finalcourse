package service;

import dao.MessageDAO;
import dao.UserDAO;
import dao.FriendshipDAO;
import dao.GroupDAO;
import model.Message;
import model.User;
import model.Group;
import model.FriendRequest;
import java.util.List;

public class ChatService {
    private MessageDAO messageDAO;
    private UserDAO userDAO;
    private FriendshipDAO friendshipDAO;
    private GroupDAO groupDAO;

    public ChatService() {
        this.messageDAO = new MessageDAO();
        this.userDAO = new UserDAO();
        this.friendshipDAO = new FriendshipDAO();
        this.groupDAO = new GroupDAO();
    }

    // ========== 私聊消息 ==========

    /**
     * 发送消息
     */
    public boolean sendMessage(int senderId, int receiverId, String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        Message message = new Message(senderId, receiverId, content.trim());
        return messageDAO.saveMessage(message);
    }

    /**
     * 获取聊天记录
     */
    public List<Message> getChatHistory(int userId1, int userId2) {
        return messageDAO.getConversation(userId1, userId2, 100);
    }

    /**
     * 撤回消息
     */
    public boolean recallMessage(long messageId, int userId) {
        return messageDAO.recallMessage(messageId, userId);
    }

    // ========== 群聊消息 ==========

    /**
     * 发送群聊消息
     */
    public boolean sendGroupMessage(int groupId, int senderId, String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        return messageDAO.saveGroupMessage(groupId, senderId, content.trim());
    }

    /**
     * 获取群聊记录
     */
    public List<Message> getGroupMessages(int groupId) {
        return messageDAO.getGroupMessages(groupId, 100);
    }

    /**
     * 撤回群聊消息
     */
    public boolean recallGroupMessage(long messageId, int userId) {
        return messageDAO.recallGroupMessage(messageId, userId);
    }

    //好友管理

    /**
     * 获取好友列表
     */
    public List<User> getFriendList(int userId) {
        return friendshipDAO.getUserFriends(userId);
    }

    /**
     * 删除好友
     */
    public boolean removeFriend(int userId1, int userId2) {
        return friendshipDAO.removeFriend(userId1, userId2);
    }

    /**
     * 置顶好友
     */
    public boolean topFriend(int userId, int friendId) {
        return friendshipDAO.topFriend(userId, friendId);
    }

    /**
     * 取消置顶好友
     */
    public boolean unTopFriend(int userId, int friendId) {
        return friendshipDAO.unTopFriend(userId, friendId);
    }

    /**
     * 检查是否已置顶
     */
    public boolean isTopped(int userId, int friendId) {
        return friendshipDAO.isTopped(userId, friendId);
    }

    /**
     * 检查是否为好友
     */
    public boolean isFriend(int userId1, int userId2) {
        return friendshipDAO.isFriend(userId1, userId2);
    }

    // ========== 好友请求 ==========

    /**
     * 发送好友请求
     */
    public boolean sendFriendRequest(int fromUserId, int toUserId, String message) {
        return friendshipDAO.sendFriendRequest(fromUserId, toUserId, message);
    }


    //群组管理

    /**
     * 创建群组
     */
    public int createGroup(String groupName, int creatorId, String description) {
        int groupId = groupDAO.createGroup(groupName, creatorId, description);
        if (groupId > 0) {
            // 添加创建者为群主
            groupDAO.addGroupMember(groupId, creatorId, "admin");
        }
        return groupId;
    }

    /**
     * 获取用户的所有群组
     */
    public List<Group> getUserGroups(int userId) {
        return groupDAO.getUserGroups(userId);
    }

    /**
     * 获取群组详情
     */
    public Group getGroupInfo(int groupId) {
        return groupDAO.getGroupById(groupId);
    }

    /**
     * 获取群组成员
     */
    public List<User> getGroupMembers(int groupId) {
        return groupDAO.getGroupMembers(groupId);
    }

    /**
     * 添加群成员
     */
    public boolean addGroupMember(int groupId, int userId) {
        return groupDAO.addGroupMember(groupId, userId, "member");
    }


    public boolean addGroupMember(int groupId, int userId, String role) {
        return groupDAO.addGroupMember(groupId, userId, role);
    }


    /**
     * 检查用户是否在群组中
     */
    public boolean isGroupMember(int groupId, int userId) {
        return groupDAO.isMember(groupId, userId);
    }

    // ========== 用户管理 ==========

    /**
     * 更新用户在线状态
     */
    public boolean updateUserOnlineStatus(int userId, boolean isOnline) {
        String status = isOnline ? "online" : "offline";
        return userDAO.updateUserStatus(userId, status);
    }

    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    /**
     * 通过ID获取用户
     */
    public User getUserById(int userId) {
        return userDAO.getUserById(userId);
    }

    /**
     * 直接添加双向好友（跳过请求步骤）
     */
    public boolean addFriend(int userId1, int userId2) {
        return friendshipDAO.addFriend(userId1, userId2);
    }

    /**
     * 获取两个用户间最后一条消息的预览文本
     */
    public String getLastMessagePreview(int userId1, int userId2) {
        return messageDAO.getLastMessageContent(userId1, userId2);
    }

    /**
     * 更新用户名称
     */
    public boolean updateUsername(int userId, String newUsername) {
        return userDAO.updateUsername(userId, newUsername);
    }

    /**
     * 更新用户密码
     */
    public boolean updatePassword(int userId, String newPassword) {
        return userDAO.updatePassword(userId, newPassword);
    }
    public boolean usernameExists(String username) {
        return userDAO.usernameExists(username);
    }
}
