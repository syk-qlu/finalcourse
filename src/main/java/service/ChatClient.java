package service;

import model.ChatMessage;

import java.io.*;
import java.net.Socket;

/**
 * 聊天客户端 - 与服务器通信
 */
public class ChatClient {
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private volatile boolean connected = false;
    private Thread receiveThread;
    private MessageListener messageListener;

    public interface MessageListener {
        void onMessageReceived(ChatMessage message);
        void onConnected();
        void onDisconnected();
        void onError(String error);
    }

    public ChatClient() {
    }

    /**
     * 连接到服务器
     */
    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            connected = true;

            if (messageListener != null) {
                messageListener.onConnected();
            }

            // 启动接收线程
            receiveThread = new Thread(this::receiveMessages);
            receiveThread.setDaemon(true);
            receiveThread.start();

            System.out.println("已连接到服务器: " + host + ":" + port);
            return true;
        } catch (IOException e) {
            if (messageListener != null) {
                messageListener.onError("连接失败: " + e.getMessage());
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 发送消息
     */
    public boolean sendMessage(ChatMessage message) {
        if (!connected) {
            if (messageListener != null) {
                messageListener.onError("未连接到服务器");
            }
            return false;
        }

        try {
            MessageProtocol.sendMessage(outputStream, message);
            return true;
        } catch (IOException e) {
            if (messageListener != null) {
                messageListener.onError("发送消息失败: " + e.getMessage());
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 接收消息
     */
    private void receiveMessages() {
        try {
            while (connected) {
                ChatMessage message = MessageProtocol.receiveMessage(inputStream);
                if (messageListener != null) {
                    messageListener.onMessageReceived(message);
                }
            }
        } catch (EOFException e) {
            System.out.println("服务器断开连接");
            disconnect();
        } catch (IOException | ClassNotFoundException e) {
            if (connected) {
                if (messageListener != null) {
                    messageListener.onError("接收消息异常: " + e.getMessage());
                }
                e.printStackTrace();
            }
        }
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        connected = false;

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (messageListener != null) {
            messageListener.onDisconnected();
        }

        System.out.println("已断开连接");
    }

    public boolean isConnected() {
        return connected && socket != null && socket.isConnected();
    }

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    /**
     * 发送登录信息
     */
    public void login(int userId, String username) {
        ChatMessage message = new ChatMessage(ChatMessage.TYPE_LOGIN);
        message.setFromUserId(userId);
        message.setFromUsername(username);
        sendMessage(message);
    }

    /**
     * 发送私聊消息
     */
    public void sendPrivateMessage(int fromUserId, String fromUsername, int toUserId, String content) {
        ChatMessage message = new ChatMessage(ChatMessage.TYPE_MESSAGE);
        message.setFromUserId(fromUserId);
        message.setFromUsername(fromUsername);
        message.setToUserId(toUserId);
        message.setContent(content);
        message.putExtra("messageType", "text");
        sendMessage(message);
    }

    /**
     * 发送群组消息
     */
    public void sendGroupMessage(int fromUserId, String fromUsername, int groupId, String content) {
        ChatMessage message = new ChatMessage(ChatMessage.TYPE_GROUP_MESSAGE);
        message.setFromUserId(fromUserId);
        message.setFromUsername(fromUsername);
        message.setGroupId(groupId);
        message.setContent(content);
        message.putExtra("messageType", "text");
        sendMessage(message);
    }

    /**
     * 发送文件
     */
    public void sendFile(int fromUserId, String fromUsername, int toUserId,
                         Integer groupId, String fileName, byte[] fileData) {
        ChatMessage message = new ChatMessage(ChatMessage.TYPE_FILE);
        message.setFromUserId(fromUserId);
        message.setFromUsername(fromUsername);
        message.setToUserId(toUserId);
        if (groupId != null) {
            message.setGroupId(groupId);
        }
        message.setFileName(fileName);
        message.setFileData(fileData);
        message.setFileSize(fileData.length);
        sendMessage(message);
    }

    /**
     * 撤回消息
     */
    public void recallMessage(int userId, long messageId, int toUserId, Integer groupId) {
        ChatMessage message = new ChatMessage(ChatMessage.TYPE_MESSAGE_RECALL);
        message.setFromUserId(userId);
        message.setMessageId(messageId);
        message.setToUserId(toUserId);
        if (groupId != null) {
            message.setGroupId(groupId);
        }
        sendMessage(message);
    }

    /**
     * 发送心跳
     */
    public void sendHeartbeat() {
        ChatMessage message = new ChatMessage(ChatMessage.TYPE_HEARTBEAT);
        sendMessage(message);
    }
}
