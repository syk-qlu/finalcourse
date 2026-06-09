package model;

import javax.swing.*;
import java.io.Serializable;
import java.sql.Timestamp;

public class User implements Serializable {
    private int userId;
    private String username;
    private String email;
    private ImageIcon headIcon;
    private String status; // online, offline, away
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // 构造函数
    public User() {}

    public User(String username, ImageIcon headIcon) {
        this.username = username;
        this.headIcon = headIcon;
        this.status = "offline";
    }

    public User(int userId, String username, String email,
                ImageIcon headIcon, String status) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.headIcon = headIcon;
        this.status = status;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public ImageIcon getHeadIcon() { return headIcon; }
    public void setHeadIcon(ImageIcon headIcon) { this.headIcon = headIcon; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getName() { return username; }
    public void setName(String name) { this.username = name; }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}