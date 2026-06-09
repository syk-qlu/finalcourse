package view;

import model.User;
import model.Message;
import model.Group;
import service.ChatService;
import util.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class ChatFrame extends JFrame {
    private User currentUser;
    private User currentChatUser;
    private Group currentGroup;

    // 左侧导航栏
    private JPanel navPane;
    private JTabbedPane tabbedPane;

    // 好友列表面板
    private ContactListPane contactListPane;

    // 群组列表面板
    private GroupListPane groupListPane;

    // 聊天区
    private JPanel chatPanel;
    private JLabel chatTitle;
    private JLabel statusLabel;
    private JScrollPane chatScroller;
    private JPanel messageDisplayPanel;
    private JTextArea inputArea;
    private JButton sendButton;

    // 业务逻辑
    private ChatService chatService;
    private List<User> contacts;
    private List<Group> groups;

    public ChatFrame(User loginUser) {
        this.currentUser = loginUser;
        this.chatService = new ChatService();
        this.contacts = new ArrayList<>();
        this.groups = new ArrayList<>();

        initializeUI();
        loadData();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                chatService.updateUserOnlineStatus(currentUser.getUserId(), false);
                System.exit(0);
            }
        });

        setVisible(true);
    }

    private void initializeUI() {
        setTitle("聊天应用 - " + currentUser.getUsername());
        setSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 初始化导航栏
        initLeftNav();

        // 初始化聊天区
        initChatPanel();

        // 组合左侧面板
        JPanel leftAll = new JPanel(null);
        leftAll.setBounds(0, 0, 310, Constants.WINDOW_HEIGHT);
        leftAll.add(navPane);

        // 创建选项卡
        tabbedPane = new JTabbedPane();
        tabbedPane.setBounds(60, 0, 250, Constants.WINDOW_HEIGHT);

        // 好友标签
        JPanel friendPanel = new JPanel(null);
        friendPanel.setLayout(null);
        contactListPane = new ContactListPane(contacts, this::switchChat, chatService, currentUser.getUserId());
        friendPanel.add(contactListPane);
        tabbedPane.addTab("好友", friendPanel);

        // 群组标签
        JPanel groupPanel = new JPanel(null);
        groupPanel.setLayout(null);
        groupListPane = new GroupListPane(groups, this::switchGroup);
        groupPanel.add(groupListPane);
        tabbedPane.addTab("群聊", groupPanel);

        leftAll.add(tabbedPane);

        // 创建分割面板
        JSplitPane mainSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, leftAll, chatPanel
        );
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
        chatScroller.setBounds(0, 60, Constants.WINDOW_WIDTH - 310, 380);
        chatScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatScroller.getVerticalScrollBar().setUnitIncrement(20);
        chatScroller.setBorder(BorderFactory.createEmptyBorder());
        SmoothScroll.enable(chatScroller);
        chatPanel.add(chatScroller);

        // 输入框
        inputArea = new JTextArea(4, 1);
        inputArea.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        JScrollPane inputScroll = new JScrollPane(inputArea);
        inputScroll.setBounds(0, 440, Constants.WINDOW_WIDTH - 310 - 100, 120);
        inputScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatPanel.add(inputScroll);

        // 发送按钮
        sendButton = new JButton("发送");
        sendButton.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        sendButton.setBounds(Constants.WINDOW_WIDTH - 310 - 90, 440, 80, 120);
        sendButton.setBackground(Constants.COLOR_SENDER);
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorder(BorderFactory.createEmptyBorder());
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sendButton.addActionListener(e -> sendMessage());
        chatPanel.add(sendButton);
    }

    private void loadData() {
        // 更新在线状态
        chatService.updateUserOnlineStatus(currentUser.getUserId(), true);

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
        if (user == null) {
            return;
        }

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
        if (group == null) {
            return;
        }

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

        messageDisplayPanel.revalidate();
        messageDisplayPanel.repaint();
    }

    private void sendMessage() {
        String text = inputArea.getText().trim();

        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入内容");
            return;
        }

        boolean success = false;

        if (currentChatUser != null) {
            success = chatService.sendMessage(
                    currentUser.getUserId(),
                    currentChatUser.getUserId(),
                    text
            );
        } else if (currentGroup != null) {
            success = chatService.sendGroupMessage(
                    currentGroup.getGroupId(),
                    currentUser.getUserId(),
                    text
            );
        } else {
            JOptionPane.showMessageDialog(this, "请先选择聊天对象");
            return;
        }

        if (success) {
            inputArea.setText("");

            if (currentChatUser != null) {
                displayMessages();
            } else if (currentGroup != null) {
                displayGroupMessages();
            }

            SwingUtilities.invokeLater(() -> {
                JScrollBar verticalBar = chatScroller.getVerticalScrollBar();
                verticalBar.setValue(verticalBar.getMaximum());
            });
        } else {
            JOptionPane.showMessageDialog(this, "发送消息失败");
        }
    }
}
