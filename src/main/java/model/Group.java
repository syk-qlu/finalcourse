package model;

import javax.swing.*;
import java.io.Serializable;

public class Group implements Serializable {
    private static final long serialVersionUID = 1L;

    private int groupId;
    private String groupName;
    private int creatorId;
    private String creatorName;
    private ImageIcon avatar;
    private String description;
    private int memberCount;
    private long createdAt;

    public Group() {
    }

    public Group(String groupName, int creatorId) {
        this.groupName = groupName;
        this.creatorId = creatorId;
    }

    public Group(int groupId, String groupName, int creatorId, String creatorName,
                 String description, int memberCount) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.description = description;
        this.memberCount = memberCount;
    }

    // Getters and Setters
    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public ImageIcon getAvatar() {
        return avatar;
    }

    public void setAvatar(ImageIcon avatar) {
        this.avatar = avatar;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return groupName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return groupId == group.groupId;
    }

    @Override
    public int hashCode() {
        return groupId;
    }
}