package view;

import model.User;
import model.Message;
import model.Group;
import model.ChatMessage;
import service.ChatService;
import service.FileService;
import service.ChatClient;
import util.Constants;
import util.FileUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ChatFrame extends JFrame implements ChatClient.MessageListener {
    private User currentUser;
    private User currentChatUser;
    private Group currentGroup;

    private JPanel navPane;
    private JTabbedPane tabbedPane;
    private ContactListPane contactListPane;
    private GroupListPane groupListPane;
    private JPanel chatPanel;
    private JLabel chatTitle;
    private JLabel statusLabel;
    private JScrollPane chatScroller;
    private JPanel messageDisplayPanel;
    private JTextArea inputArea;
    private JButton sendButton;
    private JButton sendFileButton;
    private JButton sendImageButton;

    private ChatService chatService;
    private FileService fileService;
    private ChatClient chatClient;
    private List<User> contacts;
    private List<Group> groups;
    private Timer heartbeatTimer;

    public ChatFrame(User loginUser) {
        this.currentUser = loginUser;
        this.chatService = new ChatService();
        this.fileService = FileService.class.getEnumConstants() == null ? null : new FileService();
        this.chatClient = new ChatClient();
        this.contacts = new ArrayList<>();
        this.groups = new ArrayList<>();

        // 连接到服务器
        connectToServer();

        // 初始化UI
        initializeUI();

        // 加载数据
        loadData();

        // 启动心跳检测
        startHeartbeat();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                logout();
            }
        });

        setVisible(true);
    }

    private void connectToServer() {
        boolean connected = chatClient.connect("localhost", 9999);
        if (connected) {
            chatClient.setMessageListener(this);
            // 登录
            chatClient.login(currentUser.getUserId(), currentUser.getUsername());
        } else {
            JOptionPane.showMessageDialog(this, "无法连接到服务器");
        }
    }

    private void initializeUI() {
        setTitle("聊天应用 - " + currentUser.getUsername());
        setSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initLeftNav();
        initChatPanel();

        JPanel leftAll = new JPanel(null);
        leftAll.setBounds(0, 0, 310, Constants.WINDOW_HEIGHT);
        leftAll.add(navPane);

        tabbedPane = new JTabbedPane();
        tabbedPane.setBounds(60, 0, 250, Constants.WINDOW_HEIGHT);

        JPanel friendPanel = new JPanel(null);
        friendPanel.setLayout(null);
        contactListPane = new ContactListPane(contacts, this::switchChat, chatService, currentUser.getUserId());
        friendPanel.add(contactListPane);
        tabbedPane.addTab("好友", friendPanel);

        JPanel groupPanel = new JPanel(null);
        groupPanel.setLayout(null);
        groupListPane = new GroupListPane(groups, this::switchGroup);
        groupPanel.add(groupListPane);
        tabbedPane.addTab("群聊", groupPanel);

        leftAll.add(tabbedPane);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftAll, chatPanel);
        mainSplit.setDividerSize(0);
        mainSplit.setDividerLocation(310);
        mainSplit.setEnabled(false);

        setContentPane(mainSplit);
    }

    private void initLeftNav() {
        navPane = new JPanel(null);
        navPane.setBounds(0, 0, 60, Constants.WINDOW_HEIGHT);
        navPane.setBackground(new Color(29, 35, 42));

        JLabel headLabel = new JLabel(currentUser.getHeadIcon());
        headLabel.setBounds(6, 6, 48, 48);
        navPane.add(headLabel);

        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
        separator.setBounds(0, 60, 60, 1);
        navPane.add(separator);

        JLabel statusLight = new JLabel();
        statusLight.setBounds(10, 70, 40, 40);
        statusLight.setOpaque(true);
        statusLight.setBackground(new Color(52, 211, 153));
        statusLight.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        statusLight.setText("在线");
        statusLight.setHorizontalAlignment(SwingConstants.CENTER);
        navPane.add(statusLight);
    }

    private void initChatPanel() {
        chatPanel = new JPanel(null);
        chatPanel.setBounds(0, 0, Constants.WINDOW_WIDTH - 310, Constants.WINDOW_HEIGHT);
        chatPanel.setBackground(new Color(244, 244, 244));

        // 标题栏
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBounds(0, 0, Constants.WINDOW_WIDTH - 310, 60);
        titlePanel.setBackground(new Color(79, 183, 245));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        chatTitle = new JLabel("请选择聊天对象");
        chatTitle.setFont(new Font("微软雅黑", Font.BOLD, 16));
        chatTitle.setForeground(Color.WHITE);

        statusLabel = new JLabel("");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(200, 255, 200));

        titlePanel.add(chatTitle, BorderLayout.WEST);
        titlePanel.add(statusLabel, BorderLayout.EAST);
        chatPanel.add(titlePanel);

        // 消息显示区
        messageDisplayPanel = new JPanel();
        messageDisplayPanel.setLayout(new BoxLayout(messageDisplayPanel, BoxLayout.Y_AXIS));
        messageDisplayPanel.setBackground(new Color(248, 248, 248));
        messageDisplayPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        chatScroller = new JScrollPane(messageDisplayPanel);
        chatScroller.setBounds(0, 60, Constants.WINDOW_WIDTH - 310, 340);
        chatScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatScroller.getVerticalScrollBar().setUnitIncrement(20);
        chatScroller.setBorder(BorderFactory.createEmptyBorder());
        SmoothScroll.enable(chatScroller);
        chatPanel.add(chatScroller);

        // 工具栏
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        toolBar.setBounds(0, 400, Constants.WINDOW_WIDTH - 310, 35);
        toolBar.setBackground(new Color(244, 244, 244));
        toolBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)));

        sendImageButton = new JButton("图片");
        sendImageButton.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        sendImageButton.setFocusPainted(false);
        sendImageButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sendImageButton.addActionListener(e -> sendImage());
        toolBar.add(sendImageButton);

        sendFileButton = new JButton("文件");
        sendFileButton.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        sendFileButton.setFocusPainted(false);
        sendFileButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sendFileButton.addActionListener(e -> sendFile());
        toolBar.add(sendFileButton);

        chatPanel.add(toolBar);

        // 输入框
        inputArea = new JTextArea(3, 1);
        inputArea.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        JScrollPane inputScroll = new JScrollPane(inputArea);
        inputScroll.setBounds(0, 435, Constants.WINDOW_WIDTH - 310 - 100, 120);
        inputScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatPanel.add(inputScroll);

        // 发送按钮
        sendButton = new JButton("发送");
        sendButton.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        sendButton.setBounds(Constants.WINDOW_WIDTH - 310 - 90, 435, 80, 120);
        sendButton.setBackground(Constants.COLOR_SENDER);
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorder(BorderFactory.createEmptyBorder());
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sendButton.addActionListener(e -> sendMessage());
        chatPanel.add(sendButton);
    }

    private void loadData() {
        // 加载好友列表
        contacts.clear();
        List<User> friends = chatService.getFriendList(currentUser.getUserId());
        contacts.addAll(friends);
        if (contactListPane != null) {
            contactListPane.updateContacts(contacts);
        }

        // 加载群组列表
        groups.clear();
        List<Group> userGroups = chatService.getUserGroups(currentUser.getUserId());
        groups.addAll(userGroups);
        if (groupListPane != null) {
            groupListPane.updateGroups(groups);
        }
    }

    private void switchChat(User user) {
        if (user == null) return;

        currentChatUser = user;
        currentGroup = null;
        chatTitle.setText(currentChatUser.getUsername());

        String status = currentChatUser.getStatus();
        if ("online".equals(status)) {
            statusLabel.setText("在线 ●");
            statusLabel.setForeground(new Color(52, 211, 153));
        } else {
            statusLabel.setText("离线");
            statusLabel.setForeground(new Color(160, 160, 160));
        }

        displayMessages();

        SwingUtilities.invokeLater(() -> {
            JScrollBar verticalBar = chatScroller.getVerticalScrollBar();
            verticalBar.setValue(verticalBar.getMaximum());
        });
    }

    private void switchGroup(Group group) {
        if (group == null) return;

        currentGroup = group;
        currentChatUser = null;
        chatTitle.setText(group.getGroupName() + " (" + group.getMemberCount() + "人)");
        statusLabel.setText("");

        displayGroupMessages();

        SwingUtilities.invokeLater(() -> {
            JScrollBar verticalBar = chatScroller.getVerticalScrollBar();
            verticalBar.setValue(verticalBar.getMaximum());
        });
    }

    private void displayMessages() {
        messageDisplayPanel.removeAll();

        if (currentChatUser == null) {
            messageDisplayPanel.revalidate();
            messageDisplayPanel.repaint();
            return;
        }

        List<Message> messages = chatService.getChatHistory(
                currentUser.getUserId(),
                currentChatUser.getUserId()
        );

        for (Message msg : messages) {
            User sender = msg.getSenderId() == currentUser.getUserId()
                    ? currentUser : currentChatUser;
            boolean isSender = msg.getSenderId() == currentUser.getUserId();

            if ("file".equals(msg.getMessageType())) {
                // 显示文件消息
                addFileMessageDisplay(sender, msg.getContent(), msg.getMessageType(), isSender, msg.getMessageId());
            } else if ("image".equals(msg.getMessageType())) {
                // 显示图片消息
                addImageMessageDisplay(sender, msg.getContent(), isSender, msg.getMessageId());
            } else {
                // 显示文本消息
                MessageDisplay display = new MessageDisplay(
                        sender,
                        msg.isDeleted() ? "" : msg.getContent(),
                        isSender,
                        msg.getCreatedAt(),
                        msg.getMessageId()
                );

                if (msg.isDeleted()) {
                    display.setDeleted(true);
                }

                display.setOnRecallCallback(messageId -> {
                    boolean success = chatService.recallMessage(messageId, currentUser.getUserId());
                    if (success) {
                        displayMessages();
                    }
                });

                display.setAlignmentX(Component.CENTER_ALIGNMENT);
                display.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
                display.setMaxMessageWidth(Constants.MESSAGE_MAX_WIDTH);

                messageDisplayPanel.add(display);
                messageDisplayPanel.add(Box.createVerticalStrut(8));
            }
        }

        messageDisplayPanel.revalidate();
        messageDisplayPanel.repaint();
    }

    private void displayGroupMessages() {
        messageDisplayPanel.removeAll();

        if (currentGroup == null) {
            messageDisplayPanel.revalidate();
            messageDisplayPanel.repaint();
            return;
        }

        List<Message> messages = chatService.getGroupMessages(currentGroup.getGroupId());

        for (Message msg : messages) {
            User sender = chatService.getUserById(msg.getSenderId());
            if (sender == null) continue;

            boolean isSender = msg.getSenderId() == currentUser.getUserId();

            if ("file".equals(msg.getMessageType())) {
                addFileMessageDisplay(sender, msg.getContent(), msg.getMessageType(), isSender, msg.getMessageId());
            } else if ("image".equals(msg.getMessageType())) {
                addImageMessageDisplay(sender, msg.getContent(), isSender, msg.getMessageId());
            } else {
                MessageDisplay display = new MessageDisplay(
                        sender,
                        msg.isDeleted() ? "" : msg.getContent(),
                        isSender,
                        msg.getCreatedAt(),
                        msg.getMessageId()
                );

                if (msg.isDeleted()) {
                    display.setDeleted(true);
                }

                display.setOnRecallCallback(messageId -> {
                    boolean success = chatService.recallGroupMessage(messageId, currentUser.getUserId());
                    if (success) {
                        displayGroupMessages();
                    }
                });

                display.setAlignmentX(Component.CENTER_ALIGNMENT);
                display.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
                display.setMaxMessageWidth(Constants.MESSAGE_MAX_WIDTH);

                messageDisplayPanel.add(display);
                messageDisplayPanel.add(Box.createVerticalStrut(8));
            }
        }

        messageDisplayPanel.revalidate();
        messageDisplayPanel.repaint();
    }

    private void addFileMessageDisplay(User sender, String filePath, String fileType, boolean isSender, long messageId) {
        JPanel filePanel = new JPanel(new FlowLayout(isSender ? FlowLayout.RIGHT : FlowLayout.LEFT));
        filePanel.setOpaque(false);
        filePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JButton fileButton = new JButton("📁 " + new File(filePath).getName());
        fileButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        fileButton.setBackground(new Color(79, 183, 245));
        fileButton.setForeground(Color.WHITE);
        fileButton.setFocusPainted(false);
        fileButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        fileButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        fileButton.addActionListener(e -> FileUtil.openFile(filePath));

        filePanel.add(fileButton);
        messageDisplayPanel.add(filePanel);
    }

    private void addImageMessageDisplay(User sender, String imagePath, boolean isSender, long messageId) {
        try {
            JPanel imagePanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    try {
                        java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(new File(imagePath));
                        if (image != null) {
                            int maxWidth = 300;
                            int maxHeight = 300;
                            int width = image.getWidth();
                            int height = image.getHeight();

                            if (width > maxWidth || height > maxHeight) {
                                double scale = Math.min((double) maxWidth / width, (double) maxHeight / height);
                                width = (int) (width * scale);
                                height = (int) (height * scale);
                            }

                            Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                            g.drawImage(scaledImage, 10, 10, null);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            imagePanel.setPreferredSize(new Dimension(320, 320));
            imagePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
            imagePanel.setOpaque(false);
            messageDisplayPanel.add(imagePanel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String text = inputArea.getText().trim();

        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入内容");
            return;
        }

        if (currentChatUser != null) {
            chatClient.sendPrivateMessage(
                    currentUser.getUserId(),
                    currentUser.getUsername(),
                    currentChatUser.getUserId(),
                    text
            );
        } else if (currentGroup != null) {
            chatClient.sendGroupMessage(
                    currentUser.getUserId(),
                    currentUser.getUsername(),
                    currentGroup.getGroupId(),
                    text
            );
        } else {
            JOptionPane.showMessageDialog(this, "请先选择聊天对象");
            return;
        }

        inputArea.setText("");
    }

    private void sendImage() {
        if (currentChatUser == null && currentGroup == null) {
            JOptionPane.showMessageDialog(this, "请先选择聊天对象");
            return;
        }

        File imageFile = FileTransferDialog.selectImageToSend(this);
        if (imageFile == null) return;

        try {
            byte[] imageData = FileService.uploadFile(imageFile);

            if (currentChatUser != null) {
                chatClient.sendFile(
                        currentUser.getUserId(),
                        currentUser.getUsername(),
                        currentChatUser.getUserId(),
                        null,
                        imageFile.getName(),
                        imageData
                );
            } else {
                chatClient.sendFile(
                        currentUser.getUserId(),
                        currentUser.getUsername(),
                        0,
                        currentGroup.getGroupId(),
                        imageFile.getName(),
                        imageData
                );
            }

            JOptionPane.showMessageDialog(this, "图片发送中...");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "发送图片失败: " + e.getMessage());
        }
    }

    private void sendFile() {
        if (currentChatUser == null && currentGroup == null) {
            JOptionPane.showMessageDialog(this, "请先选择聊天对象");
            return;
        }

        File file = FileTransferDialog.selectFileToSend(this);
        if (file == null) return;

        try {
            byte[] fileData = FileService.uploadFile(file);

            if (currentChatUser != null) {
                chatClient.sendFile(
                        currentUser.getUserId(),
                        currentUser.getUsername(),
                        currentChatUser.getUserId(),
                        null,
                        file.getName(),
                        fileData
                );
            } else {
                chatClient.sendFile(
                        currentUser.getUserId(),
                        currentUser.getUsername(),
                        0,
                        currentGroup.getGroupId(),
                        file.getName(),
                        fileData
                );
            }

            JOptionPane.showMessageDialog(this, "文件发送中...");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "发送文件失败: " + e.getMessage());
        }
    }

    private void startHeartbeat() {
        heartbeatTimer = new Timer();
        heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (chatClient.isConnected()) {
                    chatClient.sendHeartbeat();
                }
            }
        }, 30000, 30000); // 每30秒发送一次心跳
    }

    private void logout() {
        if (heartbeatTimer != null) {
            heartbeatTimer.cancel();
        }
        chatService.updateUserOnlineStatus(currentUser.getUserId(), false);
        chatClient.disconnect();
        System.exit(0);
    }

    @Override
    public void onMessageReceived(ChatMessage message) {
        SwingUtilities.invokeLater(() -> {
            switch (message.getType()) {
                case ChatMessage.TYPE_MESSAGE:
                    handlePrivateMessage(message);
                    break;
                case ChatMessage.TYPE_GROUP_MESSAGE:
                    handleGroupMessage(message);
                    break;
                case ChatMessage.TYPE_FILE:
                    handleFileMessage(message);
                    break;
                case ChatMessage.TYPE_MESSAGE_RECALL:
                    handleRecall(message);
                    break;
            }
        });
    }

    private void handlePrivateMessage(ChatMessage message) {
        int fromUserId = message.getFromUserId();
        if (currentChatUser != null && currentChatUser.getUserId() == fromUserId) {
            displayMessages();
            JScrollBar verticalBar = chatScroller.getVerticalScrollBar();
            verticalBar.setValue(verticalBar.getMaximum());
        }
    }

    private void handleGroupMessage(ChatMessage message) {
        Integer groupId = message.getGroupId();
        if (currentGroup != null && currentGroup.getGroupId() == groupId) {
            displayGroupMessages();
            JScrollBar verticalBar = chatScroller.getVerticalScrollBar();
            verticalBar.setValue(verticalBar.getMaximum());
        }
    }

    private void handleFileMessage(ChatMessage message) {
        int fromUserId = message.getFromUserId();
        Integer groupId = message.getGroupId();

        boolean isRelevant = (currentChatUser != null && currentChatUser.getUserId() == fromUserId) ||
                (currentGroup != null && currentGroup.getGroupId() == groupId);

        if (isRelevant) {
            String filePath = (String) message.getExtra("filePath");
            String fileName = message.getFileName();
            long fileSize = message.getFileSize();

            new FileReceiveDialog(this, fileName, filePath, fileSize);
            displayMessages();
        }
    }

    private void handleRecall(ChatMessage message) {
        if (currentChatUser != null) {
            displayMessages();
        } else if (currentGroup != null) {
            displayGroupMessages();
        }
    }

    @Override
    public void onConnected() {
        System.out.println("已连接到服务器");
    }

    @Override
    public void onDisconnected() {
        JOptionPane.showMessageDialog(this, "已断开与服务器的连接");
    }

    @Override
    public void onError(String error) {
        System.err.println("错误: " + error);
    }
}
