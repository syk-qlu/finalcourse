package model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * TCP通信消息体
 */
public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String TYPE_LOGIN = "LOGIN";
    public static final String TYPE_LOGOUT = "LOGOUT";
    public static final String TYPE_MESSAGE = "MESSAGE";
    public static final String TYPE_GROUP_MESSAGE = "GROUP_MESSAGE";
    public static final String TYPE_FILE = "FILE";
    public static final String TYPE_FILE_ACK = "FILE_ACK";
    public static final String TYPE_USER_STATUS = "USER_STATUS";
    public static final String TYPE_MESSAGE_RECALL = "MESSAGE_RECALL";
    public static final String TYPE_HEARTBEAT = "HEARTBEAT";

    private String type;
    private int fromUserId;
    private String fromUsername;
    private int toUserId;
    private Integer groupId;
    private String content;
    private byte[] fileData;
    private String fileName;
    private long fileSize;
    private long timestamp;
    private long messageId;
    private String status;
    private Map<String, Object> extra; // 额外信息

    public ChatMessage() {
        this.extra = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
    }

    public ChatMessage(String type) {
        this();
        this.type = type;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(int fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getFromUsername() {
        return fromUsername;
    }

    public void setFromUsername(String fromUsername) {
        this.fromUsername = fromUsername;
    }

    public int getToUserId() {
        return toUserId;
    }

    public void setToUserId(int toUserId) {
        this.toUserId = toUserId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void putExtra(String key, Object value) {
        this.extra.put(key, value);
    }

    public Object getExtra(String key) {
        return this.extra.get(key);
    }
}