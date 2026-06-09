package model;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private long messageId;
    private int senderId;
    private int receiverId;
    private Integer groupId; // 群聊ID，为null表示私聊
    private String senderName;
    private String content;
    private String messageType; // text, image, file
    private boolean isDeleted; // 消息是否被撤回
    private long createdAt;
    private Long deletedAt; // 撤回时间

    public Message() {
    }

    public Message(int senderId, int receiverId, String content) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.messageType = "text";
        this.isDeleted = false;
        this.createdAt = System.currentTimeMillis();
    }

    public Message(long messageId, int senderId, int receiverId, String content,
                   String messageType, boolean isDeleted, long createdAt) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.messageType = messageType;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
    }

    // 群聊消息构造函数
    public Message(int senderId, Integer groupId, String content, long createdAt) {
        this.senderId = senderId;
        this.groupId = groupId;
        this.content = content;
        this.messageType = "text";
        this.isDeleted = false;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Long deletedAt) {
        this.deletedAt = deletedAt;
    }
}
