package service;

import dao.MessageDAO;
import dao.UserDAO;
import dao.GroupDAO;
import model.ChatMessage;
import model.User;
import model.Message;
import service.ChatServer;
import util.FileUtil;

import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 * 客户端处理器 - 处理单个客户端的连接
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private ChatServer server;
    private int userId;
    private String username;
    private InputStream inputStream;
    private OutputStream outputStream;
    private volatile boolean connected = true;

    private MessageDAO messageDAO;
    private UserDAO userDAO;
    private GroupDAO groupDAO;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
        this.messageDAO = new MessageDAO();
        this.userDAO = new UserDAO();
        this.groupDAO = new GroupDAO();

        try {
            this.inputStream = socket.getInputStream();
            this.outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (connected) {
                ChatMessage message = MessageProtocol.receiveMessage(inputStream);
                handleMessage(message);
            }
        } catch (EOFException e) {
            System.out.println("客户端 " + username + " 断开连接");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("处理消息异常: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    /**
     * 处理接收到的消息
     */
    private void handleMessage(ChatMessage message) throws IOException {
        switch (message.getType()) {
            case ChatMessage.TYPE_LOGIN:
                handleLogin(message);
                break;
            case ChatMessage.TYPE_MESSAGE:
                handlePrivateMessage(message);
                break;
            case ChatMessage.TYPE_GROUP_MESSAGE:
                handleGroupMessage(message);
                break;
            case ChatMessage.TYPE_FILE:
                handleFileTransfer(message);
                break;
            case ChatMessage.TYPE_MESSAGE_RECALL:
                handleMessageRecall(message);
                break;
            case ChatMessage.TYPE_HEARTBEAT:
                handleHeartbeat(message);
                break;
            default:
                System.out.println("未知消息类型: " + message.getType());
        }
    }

    /**
     * 处理登录
     */
    private void handleLogin(ChatMessage message) throws IOException {
        this.userId = message.getFromUserId();
        this.username = message.getFromUsername();

        // 注册客户端
        server.registerClient(userId, this);

        // 更新数据库状态
        userDAO.updateUserStatus(userId, "online");

        // 发送登录成功确认
        ChatMessage response = new ChatMessage(ChatMessage.TYPE_LOGIN);
        response.setContent("登录成功");
        response.setStatus("success");
        sendMessage(response);

        System.out.println("用户 " + username + " (ID: " + userId + ") 已登录，在线用户数: " + server.getOnlineUserCount());
    }

    /**
     * 处理私聊消息
     */
    private void handlePrivateMessage(ChatMessage message) throws IOException {
        int receiverId = message.getToUserId();

        // 保存到数据库
        Message dbMessage = new Message(
                message.getFromUserId(),
                receiverId,
                message.getContent()
        );
        dbMessage.setMessageType(message.getExtra("messageType") != null ?
                (String) message.getExtra("messageType") : "text");

        messageDAO.saveMessage(dbMessage);

        // 转发给接收者
        message.setMessageId(dbMessage.getMessageId());
        server.forwardMessage(message);

        System.out.println("私聊: " + username + " -> " + receiverId + ": " + message.getContent());
    }

    /**
     * 处理群组消息
     */
    private void handleGroupMessage(ChatMessage message) throws IOException {
        int groupId = message.getGroupId();

        // 保存到数据库
        messageDAO.saveGroupMessage(
                groupId,
                message.getFromUserId(),
                message.getContent()
        );

        // 获取群组成员
        List<User> members = groupDAO.getGroupMembers(groupId);

        // 转发给所有成员
        for (User member : members) {
            if (member.getUserId() != message.getFromUserId()) {
                message.setToUserId(member.getUserId());
                server.forwardMessage(message);
            }
        }

        System.out.println("群聊: " + username + " 在群组 " + groupId + " 发送了消息");
    }

    /**
     * 处理文件转移
     */
    private void handleFileTransfer(ChatMessage message) throws IOException {
        int receiverId = message.getToUserId();
        Integer groupId = message.getGroupId();

        // 保存文件到本地
        String filePath = FileUtil.saveReceivedFile(message.getFileName(), message.getFileData());

        // 保存到数据库
        Message dbMessage = new Message(
                message.getFromUserId(),
                receiverId,
                filePath
        );
        dbMessage.setMessageType("file");
        dbMessage.setContent(message.getFileName());
        messageDAO.saveMessage(dbMessage);

        // 转发文件消息
        message.setMessageId(dbMessage.getMessageId());
        message.setContent(filePath);
        message.putExtra("fileName", message.getFileName());
        message.putExtra("fileSize", message.getFileSize());

        if (groupId != null) {
            // 群组文件
            List<User> members = groupDAO.getGroupMembers(groupId);
            for (User member : members) {
                if (member.getUserId() != message.getFromUserId()) {
                    message.setToUserId(member.getUserId());
                    server.forwardMessage(message);
                }
            }
        } else {
            // 私聊文件
            server.forwardMessage(message);
        }

        System.out.println("文件转移: " + username + " 发送了文件: " + message.getFileName());
    }

    /**
     * 处理消息撤回
     */
    private void handleMessageRecall(ChatMessage message) throws IOException {
        long messageId = message.getMessageId();

        // 撤回消息
        if (message.getGroupId() != null) {
            messageDAO.recallGroupMessage(messageId, userId);
        } else {
            messageDAO.recallMessage(messageId, userId);
        }

        // 通知接收者
        int receiverId = message.getToUserId();
        if (receiverId > 0) {
            server.forwardMessage(message);
        }

        System.out.println("消息撤回: " + username + " 撤回了消息 " + messageId);
    }

    /**
     * 处理心跳
     */
    private void handleHeartbeat(ChatMessage message) throws IOException {
        // 响应心跳
        ChatMessage response = new ChatMessage(ChatMessage.TYPE_HEARTBEAT);
        sendMessage(response);
    }

    /**
     * 发送消息给客户端
     */
    public void sendMessage(ChatMessage message) throws IOException {
        MessageProtocol.sendMessage(outputStream, message);
    }

    /**
     * 断开连接
     */
    private void disconnect() {
        connected = false;

        // 注销客户端
        if (userId > 0) {
            server.unregisterClient(userId);
            userDAO.updateUserStatus(userId, "offline");
        }

        // 关闭连接
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return connected && socket != null && socket.isConnected();
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }
}
