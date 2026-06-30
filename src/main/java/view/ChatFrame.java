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
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class ChatFrame extends JFrame implements ChatClient.MessageListener {
    private User currentUser;
    private User currentChatUser;
    private Group currentGroup;

    // 顶级容器
    private JPanel leftNavPane;
    private JPanel contactPanelWrapper;
    private JPanel chatAreaPanel;
    private ContactListPane contactListPane;
    private GroupListPane groupListPane;
    private JTabbedPane tabbedPane;

    // 聊天区组件
    private JLabel chatTitleLabel;
    private JLabel chatStatusLabel;
    private JScrollPane chatScrollPane;
    private JPanel messageDisplayPanel;
    private JTextArea inputArea;
    private JButton sendBtn;
    private JPanel toolBar;

    // 服务
    private ChatService chatService;
    private FileService fileService;
    private ChatClient chatClient;
    private List<User> contacts;
    private List<Group> groups;
    private Timer heartbeatTimer;

    // 未读消息计数：好友ID -> 未读数
    protected Map<Integer, Integer> unreadCountMap = new HashMap<>();

    // 窗口尺寸
    private int frameWidth = Constants.WINDOW_WIDTH;
    private int frameHeight = Constants.WINDOW_HEIGHT;

    // 布局常量
    private static final int NAV_WIDTH = 60;
    private static final int LIST_WIDTH = 260;
    private static final int CHAT_HEADER_HEIGHT = 60;
    private static final int TOOLBAR_HEIGHT = 36;
    private static final int INPUT_HEIGHT = 100;
    private static final int SEND_BTN_WIDTH = 80;

    public ChatFrame(User loginUser) {
        this.currentUser = loginUser;
        this.chatService = new ChatService();
        this.fileService = new FileService();
        this.chatClient = new ChatClient();
        this.contacts = new ArrayList<>();
        this.groups = new ArrayList<>();

        connectToServer();
        initUI();
        loadData();
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
            chatClient.login(currentUser.getUserId(), currentUser.getUsername());
        } else {
            JOptionPane.showMessageDialog(this, "无法连接到服务器");
        }
    }

    private void initUI() {
        setTitle("QChat - " + currentUser.getUsername());
        setSize(frameWidth, frameHeight);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 整体使用 BorderLayout，左侧固定 NAV+LIST，右侧聊天
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Constants.COLOR_NAV_BG);

        // 左侧导航栏
        leftNavPane = createNavPane();
        mainPanel.add(leftNavPane, BorderLayout.WEST);

        // 中间列表区域（带 Tab 切换好友/群聊）
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setPreferredSize(new Dimension(260, frameHeight));
        listPanel.setBackground(Constants.COLOR_LIST_BG);

        // 搜索框
        JTextField searchField = new JTextField();
        searchField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 10, 5, 10),
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true)
        ));
        searchField.setPreferredSize(new Dimension(LIST_WIDTH - 20, 30));
        searchField.setBackground(Color.WHITE);
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        searchPanel.setBackground(Constants.COLOR_LIST_BG);
        searchPanel.add(searchField);
        listPanel.add(searchPanel, BorderLayout.NORTH);

        // Tab 面板（好友/群聊）
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        tabbedPane.setBackground(Constants.COLOR_LIST_BG);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder());

        contactListPane = new ContactListPane(contacts, this::switchChat, chatService, currentUser.getUserId(), this);
        tabbedPane.addTab("好友", contactListPane);

        groupListPane = new GroupListPane(groups, this::switchGroup, chatService, currentUser.getUserId(), this);
        tabbedPane.addTab("群聊", groupListPane);

        listPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(listPanel, BorderLayout.CENTER);

        // 右侧聊天区
        chatAreaPanel = createChatPanel();
        mainPanel.add(chatAreaPanel, BorderLayout.EAST);

        // 设置 searchField 搜索功能
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String keyword = searchField.getText().trim();
                if (tabbedPane.getSelectedIndex() == 0) {
                    contactListPane.filterContacts(keyword);
                } else {
                    // 群聊搜索暂略
                }
            }
        });

        setContentPane(mainPanel);
    }

    private JPanel createNavPane() {
        JPanel nav = new JPanel(null);
        nav.setPreferredSize(new Dimension(NAV_WIDTH, frameHeight));
        nav.setBackground(Constants.COLOR_NAV_BG);

        // 头像
        JLabel headLabel = new JLabel(currentUser.getHeadIcon());
        headLabel.setBounds(10, 15, 40, 40);
        headLabel.setToolTipText(currentUser.getUsername());
        nav.add(headLabel);

        // 在线状态
        JLabel statusLabel = new JLabel("在线");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 9));
        statusLabel.setForeground(new Color(52, 211, 153));
        statusLabel.setBounds(12, 58, 36, 16);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        nav.add(statusLabel);

        // 分隔线
        JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
        sep.setBounds(5, 80, 50, 1);
        sep.setForeground(new Color(80, 80, 80));
        nav.add(sep);

        // 功能按钮
        int btnY = 100;
        JButton addFriendBtn = createNavButton("+好友");
        addFriendBtn.setBounds(5, btnY, 50, 30);
        addFriendBtn.addActionListener(e -> showAddFriendDialog());
        nav.add(addFriendBtn);

        btnY += 40;
        JButton addGroupBtn = createNavButton("+群聊");
        addGroupBtn.setBounds(5, btnY, 50, 30);
        addGroupBtn.addActionListener(e -> showJoinGroupDialog());
        nav.add(addGroupBtn);

        btnY += 40;
        JButton settingBtn = createNavButton("设置");
        settingBtn.setBounds(5, btnY, 50, 30);
        settingBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "设置功能开发中"));
        nav.add(settingBtn);

        return nav;
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(70, 76, 90));
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(100, 106, 120)); }
            public void mouseExited(MouseEvent e) { btn.setBackground(new Color(70, 76, 90)); }
        });
        return btn;
    }

    private JPanel createChatPanel() {
        JPanel chat = new JPanel(null);
        int chatWidth = frameWidth - NAV_WIDTH - LIST_WIDTH;
        chat.setPreferredSize(new Dimension(chatWidth, frameHeight));
        chat.setBackground(Constants.COLOR_CHAT_BG);

        // 标题栏
        JPanel header = new JPanel(new BorderLayout());
        header.setBounds(0, 0, chatWidth, CHAT_HEADER_HEIGHT);
        header.setBackground(Constants.COLOR_PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        chatTitleLabel = new JLabel("QChat");
        chatTitleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        chatTitleLabel.setForeground(Color.WHITE);
        header.add(chatTitleLabel, BorderLayout.WEST);

        chatStatusLabel = new JLabel("");
        chatStatusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        chatStatusLabel.setForeground(new Color(220, 240, 255));
        header.add(chatStatusLabel, BorderLayout.EAST);
        chat.add(header);

        // 消息显示区
        messageDisplayPanel = new JPanel();
        messageDisplayPanel.setLayout(new BoxLayout(messageDisplayPanel, BoxLayout.Y_AXIS));
        messageDisplayPanel.setBackground(new Color(245, 245, 245));
        messageDisplayPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        chatScrollPane = new JScrollPane(messageDisplayPanel);
        chatScrollPane.setBounds(0, CHAT_HEADER_HEIGHT, chatWidth,
                frameHeight - CHAT_HEADER_HEIGHT - TOOLBAR_HEIGHT - INPUT_HEIGHT);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(20);
        chatScrollPane.setBorder(BorderFactory.createEmptyBorder());
        chat.add(chatScrollPane);

        // 工具栏
        int toolbarY = frameHeight - TOOLBAR_HEIGHT - INPUT_HEIGHT;
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setBounds(0, toolbarY, chatWidth, TOOLBAR_HEIGHT);
        toolbar.setBackground(new Color(245, 245, 245));
        toolbar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(210, 210, 210)));

        addToolButton(toolbar, "表情", e -> {});
        addToolButton(toolbar, "截图", e -> {});
        JButton imgBtn = addToolButton(toolbar, "图片", e -> sendImage());
        JButton fileBtn = addToolButton(toolbar, "文件", e -> sendFile());
        chat.add(toolbar);

        // 输入区域
        inputArea = new JTextArea();
        inputArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        JScrollPane inputScroll = new JScrollPane(inputArea);
        inputScroll.setBounds(0, frameHeight - INPUT_HEIGHT, chatWidth - SEND_BTN_WIDTH, INPUT_HEIGHT);
        inputScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chat.add(inputScroll);

        // 发送按钮
        sendBtn = new JButton("发送");
        sendBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        sendBtn.setBounds(chatWidth - SEND_BTN_WIDTH, frameHeight - INPUT_HEIGHT, SEND_BTN_WIDTH, INPUT_HEIGHT);
        sendBtn.setBackground(Constants.COLOR_PRIMARY);
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setFocusPainted(false);
        sendBtn.setBorder(BorderFactory.createEmptyBorder());
        sendBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sendBtn.addActionListener(e -> sendMessage());
        chat.add(sendBtn);

        return chat;
    }

    private JButton addToolButton(JPanel toolbar, String text, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
                BorderFactory.createEmptyBorder(3, 10, 3, 10)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        toolbar.add(btn);
        return btn;
    }

    // ------------------ 数据加载 ------------------
    protected void loadData() {
        try {
            contacts.clear();
            List<User> friends = chatService.getFriendList(currentUser.getUserId());
            contacts.addAll(friends);
            // 初始化未读计数
            for (User f : friends) {
                unreadCountMap.putIfAbsent(f.getUserId(), 0);
            }
            contactListPane.updateContacts(contacts);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载好友列表失败");
        }

        try {
            groups.clear();
            List<Group> gs = chatService.getUserGroups(currentUser.getUserId());
            groups.addAll(gs);
            groupListPane.updateGroups(groups);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载群聊列表失败");
        }
    }

    // ------------------ 切换聊天 ------------------
    private void switchChat(User user) {
        currentChatUser = user;
        currentGroup = null;
        chatTitleLabel.setText(user.getUsername());
        if ("online".equals(user.getStatus())) {
            chatStatusLabel.setText("在线 ●");
            chatStatusLabel.setForeground(new Color(52, 211, 153));
        } else {
            chatStatusLabel.setText("离线");
            chatStatusLabel.setForeground(new Color(180, 180, 180));
        }
        // 清除该好友未读
        unreadCountMap.put(user.getUserId(), 0);
        contactListPane.updateContacts(contacts); // 刷新红点
        displayMessages();
        scrollToBottom();
    }

    private void switchGroup(Group group) {
        currentGroup = group;
        currentChatUser = null;
        chatTitleLabel.setText(group.getGroupName() + " (" + group.getMemberCount() + ")");
        chatStatusLabel.setText("");
        displayGroupMessages();
        scrollToBottom();
    }

    private void displayMessages() {
        messageDisplayPanel.removeAll();
        if (currentChatUser == null) {
            messageDisplayPanel.revalidate();
            messageDisplayPanel.repaint();
            return;
        }
        List<Message> msgs = chatService.getChatHistory(currentUser.getUserId(), currentChatUser.getUserId());
        for (Message msg : msgs) {
            addMessageToDisplay(msg, msg.getSenderId() == currentUser.getUserId());
        }
        messageDisplayPanel.revalidate();
        messageDisplayPanel.repaint();
    }

    private void displayGroupMessages() {
        messageDisplayPanel.removeAll();
        if (currentGroup == null) { refreshMessagePanel(); return; }
        List<Message> msgs = chatService.getGroupMessages(currentGroup.getGroupId());
        for (Message msg : msgs) {
            boolean isMe = msg.getSenderId() == currentUser.getUserId();
            addMessageToDisplay(msg, isMe);
        }
        refreshMessagePanel();
    }

    private void addMessageToDisplay(Message msg, boolean isSender) {
        User sender = isSender ? currentUser : chatService.getUserById(msg.getSenderId());
        if (sender == null) sender = currentUser;

        MessageBubblePanel bubble = new MessageBubblePanel(sender, msg, isSender);
        bubble.setOpaque(false);

        // 获取聊天区可视宽度
        int chatWidth = chatScrollPane.getViewport().getWidth();
        if (chatWidth <= 0) chatWidth = 600;

        // 文本最大宽度限制
        int maxTextWidth = chatWidth - 110;
        bubble.setMessageMaxWidth(maxTextWidth);

        // ★ 关键：让组件宽度 = 气泡实际宽度，而不是聊天区全宽
        Dimension prefSize = bubble.getPreferredSize();
        bubble.setMaximumSize(new Dimension(prefSize.width, prefSize.height + 10));
        bubble.setPreferredSize(new Dimension(prefSize.width, prefSize.height + 10));
        bubble.setMinimumSize(new Dimension(prefSize.width, prefSize.height + 10));

        // 水平对齐
        bubble.setAlignmentX(isSender ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);

        // 撤回回调（仅正式消息）
        if (isSender && msg.getMessageId() > 0) {
            bubble.setOnRecallCallback(id -> {
                if (currentChatUser != null) {
                    chatClient.recallMessage(currentUser.getUserId(), id,
                            currentChatUser.getUserId(), null);
                } else if (currentGroup != null) {
                    chatClient.recallMessage(currentUser.getUserId(), id,
                            0, currentGroup.getGroupId());
                }
                displayMessages();
            });
        }

        messageDisplayPanel.add(bubble);
    }
    //刷新消息面板
    private void refreshMessagePanel() {
        messageDisplayPanel.revalidate();
        messageDisplayPanel.repaint();
    }
    //新增消息后，滚动面板自动滑到底部
    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = chatScrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    // ------------------ 消息发送 ------------------
    private void sendMessage() {
        String text = inputArea.getText().trim();
        if (text.isEmpty()) return;

        if (currentChatUser != null) {
            Message tempMsg = new Message(currentUser.getUserId(), currentChatUser.getUserId(), text);
            tempMsg.setMessageId(-1L);
            addMessageToDisplay(tempMsg, true);

            messageDisplayPanel.revalidate();
            scrollToBottom();

            chatClient.sendPrivateMessage(currentUser.getUserId(), currentUser.getUsername(),
                    currentChatUser.getUserId(), text);
        } else if (currentGroup != null) {
            Message tempMsg = new Message(currentUser.getUserId(),
                    currentGroup.getGroupId(), text, System.currentTimeMillis());
            tempMsg.setMessageId(-1L);
            addMessageToDisplay(tempMsg, true);
            messageDisplayPanel.revalidate();
            scrollToBottom();

            chatClient.sendGroupMessage(currentUser.getUserId(), currentUser.getUsername(),
                    currentGroup.getGroupId(), text);
        } else {
            JOptionPane.showMessageDialog(this, "请选择聊天对象");
            return;
        }
        inputArea.setText("");
    }

    private void sendImage() {
        File imageFile = FileTransferDialog.selectImageToSend(this);
        if (imageFile == null) return;
        try {
            byte[] data = FileService.uploadFile(imageFile);
            if (currentChatUser != null) {
                chatClient.sendFile(currentUser.getUserId(), currentUser.getUsername(),
                        currentChatUser.getUserId(), null, imageFile.getName(), data);
            } else if (currentGroup != null) {
                chatClient.sendFile(currentUser.getUserId(), currentUser.getUsername(),
                        0, currentGroup.getGroupId(), imageFile.getName(), data);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "发送图片失败");
        }
    }

    private void sendFile() {
        File file = FileTransferDialog.selectFileToSend(this);
        if (file == null) return;
        try {
            byte[] data = FileService.uploadFile(file);
            if (currentChatUser != null) {
                chatClient.sendFile(currentUser.getUserId(), currentUser.getUsername(),
                        currentChatUser.getUserId(), null, file.getName(), data);
            } else if (currentGroup != null) {
                chatClient.sendFile(currentUser.getUserId(), currentUser.getUsername(),
                        0, currentGroup.getGroupId(), file.getName(), data);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "发送文件失败");
        }
    }

    //添加好友/群聊对话框
    private void showAddFriendDialog() {
        String input = JOptionPane.showInputDialog(this, "输入好友的用户ID：");
        if (input == null || input.trim().isEmpty()) return;
        try {
            int friendId = Integer.parseInt(input.trim());
            if (friendId == currentUser.getUserId()) { JOptionPane.showMessageDialog(this, "不能添加自己"); return; }
            if (chatService.isFriend(currentUser.getUserId(), friendId)) {
                JOptionPane.showMessageDialog(this, "已是好友"); return;
            }
            boolean ok = chatService.addFriend(currentUser.getUserId(), friendId);
            if (ok) {
                JOptionPane.showMessageDialog(this, "添加成功");
                loadData();
            } else JOptionPane.showMessageDialog(this, "添加失败");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "无效ID");
        }
    }
    //显示加入群聊情况
    private void showJoinGroupDialog() {
        String input = JOptionPane.showInputDialog(this, "输入群聊ID：");
        if (input == null || input.trim().isEmpty()) return;
        try {
            int gid = Integer.parseInt(input.trim());
            Group g = chatService.getGroupInfo(gid);
            if (g == null) { JOptionPane.showMessageDialog(this, "群不存在"); return; }
            if (chatService.isGroupMember(gid, currentUser.getUserId())) {
                JOptionPane.showMessageDialog(this, "已在群中"); return;
            }
            boolean ok = chatService.addGroupMember(gid, currentUser.getUserId());
            if (ok) {
                JOptionPane.showMessageDialog(this, "加入成功");
                loadData();
            } else JOptionPane.showMessageDialog(this, "加入失败");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "无效ID");
        }
    }

    // ------------------ 未读消息处理 ------------------
    public void incrementUnread(int userId) {
        unreadCountMap.merge(userId, 1, Integer::sum);
        contactListPane.updateUnreadCount(userId, unreadCountMap.getOrDefault(userId, 0));
    }

    public void clearUnread(int userId) {
        unreadCountMap.put(userId, 0);
        contactListPane.updateUnreadCount(userId, 0);
    }

    // ------------------ 服务器消息回调 ------------------
    @Override
    public void onMessageReceived(ChatMessage message) {
        SwingUtilities.invokeLater(() -> {
            switch (message.getType()) {
                case ChatMessage.TYPE_MESSAGE:
                    handlePrivateMsg(message);
                    break;
                case ChatMessage.TYPE_GROUP_MESSAGE:
                    handleGroupMsg(message);
                    break;
                case ChatMessage.TYPE_FILE:
                    handleFileMsg(message);
                    break;
                case ChatMessage.TYPE_MESSAGE_RECALL:
                    handleRecallMsg(message);
                    break;
            }
        });
    }

    private void handlePrivateMsg(ChatMessage msg) {
        int from = msg.getFromUserId();
        int to = msg.getToUserId();
        // 如果当前聊天对象是发送者或接收者，刷新
        if (currentChatUser != null && (currentChatUser.getUserId() == from || currentChatUser.getUserId() == to)) {
            displayMessages();
            scrollToBottom();
        } else if (currentChatUser == null || currentChatUser.getUserId() != from) {
            // 未在聊天，增加未读
            incrementUnread(from);
        }
    }

    private void handleGroupMsg(ChatMessage msg) {
        if (currentGroup != null && currentGroup.getGroupId() == msg.getGroupId()) {
            displayGroupMessages();
            scrollToBottom();
        }
    }

    private void handleFileMsg(ChatMessage msg) {
        // 文件接收已由 FileReceiveDialog 处理，这里只刷新消息
        if (currentChatUser != null && currentChatUser.getUserId() == msg.getFromUserId()) {
            displayMessages();
        }
    }
    //撤回消息处理
    private void handleRecallMsg(ChatMessage msg) {
        if (currentChatUser != null) {
            displayMessages();
        } else if (currentGroup != null) {
            displayGroupMessages();
        }
    }

    // ------------------ 工具方法 ------------------
    private void startHeartbeat() {
        heartbeatTimer = new Timer();
        heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() { if (chatClient.isConnected()) chatClient.sendHeartbeat(); }
        }, 30000, 30000);
    }

    private void logout() {
        if (heartbeatTimer != null) heartbeatTimer.cancel();
        chatService.updateUserOnlineStatus(currentUser.getUserId(), false);
        chatClient.disconnect();
        System.exit(0);
    }

    @Override public void onConnected() { System.out.println("已连接"); }
    @Override public void onDisconnected() { JOptionPane.showMessageDialog(this, "断开连接"); }
    @Override public void onError(String err) { System.err.println(err); }
}
