package model;

import javax.swing.*;

public class User {
    private String id;
    private String name;
    private ImageIcon headIcon;
    public User(String id, String name) {
        this.id = id;
        this.name = name;
    }
    public User(String id, String name, ImageIcon headIcon) {
        this.id = id;
        this.name = name;
        this.headIcon = headIcon;
    }
    public User(String username,ImageIcon headIcon) {
        this.id = username;
        this.headIcon = headIcon;
    }
    public String getId() {
        return id;
    }
    public ImageIcon getHeadIcon() {
        return headIcon;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setHeadIcon(ImageIcon headIcon) {
        this.headIcon = headIcon;
    }


}
