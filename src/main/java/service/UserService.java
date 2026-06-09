package service;

import dao.UserDAO;
import model.User;

public class UserService {
    private UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    /**
     * 用户注册
     */
    public boolean register(String username, String password, String email) {
        // 检查用户名是否存在
        if (userDAO.usernameExists(username)) {
            return false;
        }
        return userDAO.addUser(username, password, email);
    }

    /**
     * 用户登录
     */
    public User login(String username, String password) {
        User user = userDAO.getUserByCredentials(username, password);
        if (user != null) {
            // 更新用户状态为在线
            userDAO.updateUserStatus(user.getUserId(), "online");
        }
        return user;
    }

    /**
     * 用户登出
     */
    public boolean logout(int userId) {
        return userDAO.updateUserStatus(userId, "offline");
    }

    /**
     * 通过用户名查找用户
     */
    public User findUserByUsername(String username) {
        return userDAO.getUserByUsername(username);
    }

    /**
     * 通过ID查找用户
     */
    public User findUserById(int userId) {
        return userDAO.getUserById(userId);
    }
}