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
        //
        btnY += 40;
        JButton addGroupBtn = createNavButton("+群聊");
        addGroupBtn.setBounds(5, btnY, 50, 30);
        addGroupBtn.addActionListener(e -> showJoinGroupDialog());
        nav.add(addGroupBtn);

        btnY += 40;
        JButton profileBtn = createNavButton("主页");
        profileBtn.setBounds(5, btnY, 50, 30);
        profileBtn.addActionListener(e -> showProfileDialog());
        nav.add(profileBtn);

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
    //创建右侧聊天区域，包含消息显示区域，消息输入区域
    private JPanel createChatPanel() {
        // 右侧聊天总面板，使用 BorderLayout
        JPanel chat = new JPanel(new BorderLayout());
        chat.setPreferredSize(new Dimension(frameWidth - NAV_WIDTH - LIST_WIDTH, frameHeight));
        chat.setBackground(Constants.COLOR_CHAT_BG);

        //标题栏
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(1, CHAT_HEADER_HEIGHT));
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
        chat.add(header, BorderLayout.NORTH);

        //底部区域—— 工具栏 + 输入框 + 发送按钮
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Constants.COLOR_CHAT_BG);

        //工具栏
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setPreferredSize(new Dimension(1, TOOLBAR_HEIGHT));
        toolbar.setBackground(new Color(245, 245, 245));
        toolbar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(210, 210, 210)));

        addToolButton(toolbar, "表情", e -> {});
        addToolButton(toolbar, "截图", e -> {});
        addToolButton(toolbar, "图片", e -> sendImage());
        addToolButton(toolbar, "文件", e -> sendFile());
        bottomPanel.add(toolbar, BorderLayout.NORTH);

        //输入框 + 发送按钮
        JPanel inputRow = new JPanel(new BorderLayout(0, 0));
        inputRow.setBackground(Color.WHITE);

        inputArea = new JTextArea();
        inputArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        JScrollPane inputScroll = new JScrollPane(inputArea);
        inputScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        inputRow.add(inputScroll, BorderLayout.CENTER);   // 输入框占满中央

        // 发送按钮（EAST of inputRow）—— 右下角，距边界 5px
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 5));  // 下、右各5px

        sendBtn = new JButton("发送");
        sendBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        sendBtn.setPreferredSize(new Dimension(75, 32));
        sendBtn.setBackground(Constants.COLOR_PRIMARY);
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setFocusPainted(false);
        sendBtn.setBorder(BorderFactory.createEmptyBorder());
        sendBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sendBtn.addActionListener(e -> sendMessage());
        btnPanel.add(sendBtn);
        inputRow.add(btnPanel, BorderLayout.EAST);

        bottomPanel.add(inputRow, BorderLayout.CENTER);
        chat.add(bottomPanel, BorderLayout.SOUTH);

        //消息显示区
        messageDisplayPanel = new JPanel();
        messageDisplayPanel.setLayout(new BoxLayout(messageDisplayPanel, BoxLayout.Y_AXIS));
        messageDisplayPanel.setBackground(new Color(240, 240, 240));
        messageDisplayPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        chatScrollPane = new JScrollPane(messageDisplayPanel);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(20);
        chatScrollPane.setBorder(BorderFactory.createEmptyBorder());
        chatScrollPane.getVerticalScrollBar().setUI(new ScrollBarUI());
        chat.add(chatScrollPane, BorderLayout.CENTER);


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

    //数据加载
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

    // 切换聊天
    private void switchChat(User user) {
        currentChatUser = user;
        currentGroup = null;
        chatTitleLabel.setText(user.getUsername());
        // 状态显示
        if ("online".equals(user.getStatus())) {
            chatStatusLabel.setText("在线 ●");
            chatStatusLabel.setForeground(new Color(52, 211, 153));
        } else {
            chatStatusLabel.setText("离线");
            chatStatusLabel.setForeground(new Color(180, 180, 180));
        }
        // 清除未读并刷新红点（不必重新查询数据库，只需更新未读数）
        unreadCountMap.put(user.getUserId(), 0);
        contactListPane.updateUnreadCount(user.getUserId(), 0);   // 只更新单个 item 的红点
        // 加载聊天记录
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

    /**
     * 将消息显示在消息面板中
     * @param msg
     * @param isSender
     */
    private void addMessageToDisplay(Message msg, boolean isSender) {
        User sender = isSender ? currentUser : chatService.getUserById(msg.getSenderId());
        if (sender == null) sender = currentUser;

        MessageBubblePanel bubble = new MessageBubblePanel(sender, msg, isSender);
        bubble.setOpaque(false);

        // 1. 限制文本最大宽度（聊天区宽度 - 预留空间）
        int chatWidth = chatScrollPane.getViewport().getWidth();
        if (chatWidth <= 0) chatWidth = 600;
        bubble.setMessageMaxWidth(chatWidth - 110);      // 头像区50 + 边距60

        // 2. 构建一行，用 BorderLayout 左/右对齐
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        if (isSender) {
            row.add(bubble, BorderLayout.EAST);          // 自己的消息靠右
        } else {
            row.add(bubble, BorderLayout.WEST);          // 对方的消息靠左
        }

        // 3. 行的高度 = 气泡的实际高度，宽度随便（BoxLayout不会横向拉伸行）
        Dimension bubbleSize = bubble.getPreferredSize();
        row.setPreferredSize(new Dimension(1, bubbleSize.height));
        row.setMinimumSize(new Dimension(1, bubbleSize.height));
        row.setMaximumSize(new Dimension(Short.MAX_VALUE, bubbleSize.height));

        // 4. 撤回回调（仅正式消息）
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

        // 5. 将行加入垂直面板
        messageDisplayPanel.add(row);
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

    //消息发送
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

    //服务器消息回调
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
    //显示个人主页
    private void showProfileDialog() {
        // 模态对话框
        JDialog profileDialog = new JDialog(this, "个人主页", true);
        profileDialog.setSize(300, 350);
        profileDialog.setLocationRelativeTo(this);
        profileDialog.setLayout(new BorderLayout());

        //顶部面板（头像+名称+ID）
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));
        topPanel.setBackground(Color.WHITE);

        // 头像（居中）
        JLabel avatarLabel = new JLabel(new ImageIcon(
                currentUser.getHeadIcon().getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH)));
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(avatarLabel);
        topPanel.add(Box.createVerticalStrut(10));

        // 用户名
        JLabel nameLabel = new JLabel("用户名: " + currentUser.getUsername());
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(nameLabel);
        topPanel.add(Box.createVerticalStrut(5));

        // ID
        JLabel idLabel = new JLabel("ID: " + currentUser.getUserId());
        idLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        idLabel.setForeground(Color.GRAY);
        idLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(idLabel);
        topPanel.add(Box.createVerticalStrut(15));

        profileDialog.add(topPanel, BorderLayout.NORTH);

        //按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1, 0, 8));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        buttonPanel.setBackground(Color.WHITE);

        // 更改用户名按钮
        JButton changeNameBtn = new JButton("更改名称");
        changeNameBtn.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        changeNameBtn.setBackground(new Color(79, 183, 245));
        changeNameBtn.setForeground(Color.WHITE);
        changeNameBtn.setFocusPainted(false);
        changeNameBtn.addActionListener(e -> {
            String newName = JOptionPane.showInputDialog(profileDialog, "请输入新用户名:", currentUser.getUsername());
            if (newName != null && !newName.trim().isEmpty()) {
                newName = newName.trim();
                if (newName.equals(currentUser.getUsername())) {
                    JOptionPane.showMessageDialog(profileDialog, "与当前用户名相同");
                    return;
                }
                if (chatService.usernameExists(newName)) {
                    JOptionPane.showMessageDialog(profileDialog, "用户名已存在");
                    return;
                }
                boolean ok = chatService.updateUsername(currentUser.getUserId(), newName);
                if (ok) {
                    currentUser.setUsername(newName);
                    nameLabel.setText("用户名: " + newName);
                    setTitle("QChat - " + newName);
                    // 更新导航栏头像区域的 tooltip 等（可以简单重绘，但需获取引用，这里可调用 revalidate）
                    JOptionPane.showMessageDialog(profileDialog, "用户名修改成功");
                } else {
                    JOptionPane.showMessageDialog(profileDialog, "修改失败");
                }
            }
        });
        buttonPanel.add(changeNameBtn);

        // 更改密码按钮
        JButton changePwdBtn = new JButton("更改密码");
        changePwdBtn.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        changePwdBtn.setBackground(new Color(79, 183, 245));
        changePwdBtn.setForeground(Color.WHITE);
        changePwdBtn.setFocusPainted(false);
        changePwdBtn.addActionListener(e -> {
            JPasswordField pwdField = new JPasswordField();
            int option = JOptionPane.showConfirmDialog(profileDialog, pwdField, "请输入新密码:", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                String newPwd = new String(pwdField.getPassword());
                if (newPwd.isEmpty()) {
                    JOptionPane.showMessageDialog(profileDialog, "密码不能为空");
                    return;
                }
                boolean ok = chatService.updatePassword(currentUser.getUserId(), newPwd);
                if (ok) {
                    JOptionPane.showMessageDialog(profileDialog, "密码修改成功，下次登录生效");
                } else {
                    JOptionPane.showMessageDialog(profileDialog, "修改失败");
                }
            }
        });
        buttonPanel.add(changePwdBtn);

        profileDialog.add(buttonPanel, BorderLayout.CENTER);

        // 关闭按钮
        JButton closeBtn = new JButton("关闭");
        closeBtn.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        closeBtn.addActionListener(e -> profileDialog.dispose());
        JPanel closePanel = new JPanel();
        closePanel.add(closeBtn);
        profileDialog.add(closePanel, BorderLayout.SOUTH);

        profileDialog.setVisible(true);
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
