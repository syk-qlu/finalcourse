package view;

import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * 完整的聊天应用程序演示
 * 展示MessageDisplay在实际应用中的使用
 */
public class ChatApplication extends JFrame {
    private JPanel chatPanel;
    private JScrollPane scrollPane;
    private JTextArea inputArea;
    private JButton sendButton;
    private User currentUser;
    private User otherUser;
    private int messageCount = 0;

    public ChatApplication() {
        setTitle("完整聊天应用 - Java Swing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 900);
        setLocationRelativeTo(null);

        // 初始化用户
        currentUser = new User("张三", createColorAvatar("张", new Color(79, 183, 245)));
        otherUser = new User("李四", createColorAvatar("李", new Color(100, 200, 100)));

        // 创建顶部信息面板
        JPanel topPanel = createTopPanel();

        // 创建聊天内容面板
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(new Color(248, 248, 248));
        chatPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 创建滚动面板
        scrollPane = new JScrollPane(chatPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        SmoothScroll.enable(scrollPane);

        // 创建底部输入面板
        JPanel bottomPanel = createBottomPanel();

        // 布局
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // 初始化对话
        initializeChat();

        setVisible(true);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(79, 183, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

        JLabel titleLabel = new JLabel("张三");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);

        JLabel statusLabel = new JLabel("在线 ●");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(200, 255, 200));

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(statusLabel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // 输入区域
        inputArea = new JTextArea(3, 1);
        inputArea.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        JScrollPane inputScrollPane = new JScrollPane(inputArea);
        inputScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // 发送按钮
        sendButton = new JButton("发送");
        sendButton.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        sendButton.setPreferredSize(new Dimension(80, 70));
        sendButton.setBackground(new Color(79, 183, 245));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorder(BorderFactory.createEmptyBorder());
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sendButton.addActionListener(e -> sendMessage());

        // 快捷回复面板
        JPanel quickReplyPanel = createQuickReplyPanel();

        // 主布局
        JPanel inputPanelLeft = new JPanel(new BorderLayout());
        inputPanelLeft.add(inputScrollPane, BorderLayout.CENTER);
        inputPanelLeft.add(quickReplyPanel, BorderLayout.SOUTH);

        panel.add(inputPanelLeft, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);

        return panel;
    }

    private JPanel createQuickReplyPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel.setBackground(Color.WHITE);

        String[] quickReplies = {"😊 好的", "👍 同意", "❤️ 赞", "😂 哈哈"};

        for (String reply : quickReplies) {
            JButton btn = new JButton(reply);
            btn.setFont(new Font("微软雅黑", Font.PLAIN, 11));
            btn.setBackground(new Color(240, 240, 240));
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createRaisedBevelBorder());
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> {
                inputArea.setText(reply);
                sendMessage();
            });
            panel.add(btn);
        }

        return panel;
    }

    private void initializeChat() {
        addMessageToChat("嘿，你好！", false, System.currentTimeMillis() - 600000);
        addMessageToChat("你好啊！最近在忙什么呢？", true, System.currentTimeMillis() - 550000);
        addMessageToChat("我在开发一个Java Swing的消息显示组件。这是一个功能完整的消息显示系统，支持用户头像、用户名、发送时间和自动换行。",
                false, System.currentTimeMillis() - 500000);
        addMessageToChat("哇，听起来很有趣！你能告诉我更多的细节吗？", true, System.currentTimeMillis() - 450000);
        addMessageToChat("当然可以！这个组件包括以下特性：\n1. 支持用户头像显示（圆形裁剪）\n2. 显示用户名和消息\n3. 自动格式化时间戳\n4. 支持消息自动换行\n5. 左右对齐显示发送方和接收方消息",
                false, System.currentTimeMillis() - 400000);
        addMessageToChat("太棒了！我很感兴趣。可以用这个组件来构建一个完整的聊天应用吗？",
                true, System.currentTimeMillis() - 300000);
    }

    private void sendMessage() {
        String text = inputArea.getText().trim();
        if (text.isEmpty()) {
            return;
        }

        // 添加发送的消息
        addMessageToChat(text, true, System.currentTimeMillis());

        // 清空输入框
        inputArea.setText("");

        // 模拟接收回复
        Timer timer = new Timer(1000 + (int)(Math.random() * 2000), e -> {
            String[] replies = {
                    "好的，我理解了！",
                    "这个想法很不错呢！",
                    "我非常同意你的观点",
                    "继续加油！你做得很好！",
                    "这太棒了，我喜欢这个设计",
                    "能否告诉我更多的细节？",
                    "让我想想这个问题...",
                    "你说得非常有道理",
                    "我们什么时候可以合作？",
                    "这个功能真的很强大"
            };
            int index = messageCount % replies.length;
            addMessageToChat(replies[index], false, System.currentTimeMillis());
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void addMessageToChat(String text, boolean isSender, long timestamp) {
        User sender = isSender ? currentUser : otherUser;

        MessageDisplay messageDisplay = new MessageDisplay(sender, text, isSender, timestamp);
        messageDisplay.setAlignmentX(Component.LEFT_ALIGNMENT);
        messageDisplay.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        messageDisplay.setMaxMessageWidth(450);

        chatPanel.add(messageDisplay);
        chatPanel.add(Box.createVerticalStrut(8));

        // 滚动到底部
        SwingUtilities.invokeLater(() -> {
            chatPanel.revalidate();
            chatPanel.repaint();
            JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
            verticalBar.setValue(verticalBar.getMaximum());
        });

        messageCount++;
    }

    /**
     * 创建带颜色和文字的头像
     */
    private ImageIcon createColorAvatar(String text, Color color) {
        BufferedImage image = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 绘制背景
        g2d.setColor(color);
        g2d.fillOval(0, 0, 40, 40);

        // 绘制文字
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 18));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (40 - fm.stringWidth(text)) / 2;
        int textY = (40 - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(text, textX, textY);

        g2d.dispose();
        return new ImageIcon(image);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatApplication());
    }
}

