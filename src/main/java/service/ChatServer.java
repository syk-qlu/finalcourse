package service;

import dao.UserDAO;
import model.User;
import model.ChatMessage;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.*;

/**
 * 聊天服务器 - 管理客户端连接和消息转发
 */
public class ChatServer {
    private static final int PORT = 9999;
    private static final int MAX_CLIENTS = 1000;

    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private Map<Integer, ClientHandler> clientHandlers; // userId -> ClientHandler
    private UserDAO userDAO;

    public ChatServer() {
        this.clientHandlers = new ConcurrentHashMap<>();
        this.userDAO = new UserDAO();
        this.threadPool = Executors.newFixedThreadPool(MAX_CLIENTS);
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("聊天服务器已启动，监听端口: " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("新客户端连接: " + socket.getInetAddress().getHostAddress());

                ClientHandler handler = new ClientHandler(socket, this);
                threadPool.execute(handler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    /**
     * 获取客户端处理器
     */
    public ClientHandler getClientHandler(int userId) {
        return clientHandlers.get(userId);
    }

    /**
     * 注册客户端
     */
    public void registerClient(int userId, ClientHandler handler) {
        clientHandlers.put(userId, handler);
        System.out.println("用户 " + userId + " 已连接");
    }

    /**
     * 注销客户端
     */
    public void unregisterClient(int userId) {
        clientHandlers.remove(userId);
        System.out.println("用户 " + userId + " 已断开连接");
    }

    /**
     * 转发消息给指定用户
     */
    public void forwardMessage(ChatMessage message) {
        int toUserId = message.getToUserId();
        ClientHandler handler = getClientHandler(toUserId);

        if (handler != null && handler.isConnected()) {
            try {
                handler.sendMessage(message);
                System.out.println("消息已转发给用户 " + toUserId);
            } catch (IOException e) {
                System.err.println("转发消息失败: " + e.getMessage());
            }
        } else {
            System.out.println("用户 " + toUserId + " 不在线，消息未转发");
        }
    }

    /**
     * 转发群组消息
     */
    public void forwardGroupMessage(ChatMessage message, List<Integer> memberIds) {
        for (int memberId : memberIds) {
            if (memberId != message.getFromUserId()) {
                message.setToUserId(memberId);
                forwardMessage(message);
            }
        }
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(int userId) {
        return clientHandlers.containsKey(userId);
    }

    /**
     * 获取在线用户数
     */
    public int getOnlineUserCount() {
        return clientHandlers.size();
    }

    /**
     * 关闭服务器
     */
    public void shutdown() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            threadPool.shutdown();
            System.out.println("服务器已关闭");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }
}
