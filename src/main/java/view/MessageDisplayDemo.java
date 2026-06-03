package view;

import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 消息显示组件演示
 */
public class MessageDisplayDemo extends JFrame {
    private JPanel chatPanel;
    private JScrollPane scrollPane;
    private JTextField inputField;
    private JButton sendButton;
    private User currentUser;
    private User otherUser;

    public MessageDisplayDemo() {
        setTitle("Message Display Component Demo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 800);
        setLocationRelativeTo(null);

        // 初始化用户
        currentUser = new User("我", createDefaultAvatar(new Color(79, 183, 245)));
        otherUser = new User("朋友", createDefaultAvatar(new Color(100, 200, 100)));

        // 创建顶部面板
        JPanel topPanel = createTopPanel();

        // 创建聊天内容面板
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(new Color(245, 245, 245));
        chatPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // 创建滚动面板
        scrollPane = new JScrollPane(chatPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);

        // 创建底部输入面板
        JPanel bottomPanel = createBottomPanel();

        // 添加组件到主窗口
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // 添加示例消息
        addSampleMessages();

        setVisible(true);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(79, 183, 245));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("消息显示组件演示");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);

        panel.add(titleLabel);
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.WHITE);

        inputField = new JTextField();
        inputField.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        inputField.addActionListener(e -> sendMessage());

        sendButton = new JButton("发送");
        sendButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        sendButton.setPreferredSize(new Dimension(80, 40));
        sendButton.addActionListener(e -> sendMessage());
        sendButton.setBackground(new Color(79, 183, 245));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorder(BorderFactory.createEmptyBorder());
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        panel.add(inputField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);

        return panel;
    }

    private void addSampleMessages() {
        // 添加示例消息
        addMessageToChat("你好，这是一条接收的消息", false, System.currentTimeMillis() - 300000);
        addMessageToChat("我是用Java Swing实现的消息显示组件", true, System.currentTimeMillis() - 250000);
        addMessageToChat("支持用户头像、用户名、发送时间和自动换行。这是一条比较长的消息，会自动进行换行处理。",
                false, System.currentTimeMillis() - 200000);
        addMessageToChat("没错！", true, System.currentTimeMillis() - 150000);
        addMessageToChat("你可以在下面输入消息，点击发送按钮发送消息。系统会自动模拟对方回复。",
                false, System.currentTimeMillis() - 100000);
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) {
            return;
        }

        // 添加发送的消息
        addMessageToChat(text, true, System.currentTimeMillis());

        // 清空输入框
        inputField.setText("");

        // 模拟接收回复
        Timer timer = new Timer(1500, e -> {
            String[] replies = {
                    "我收到了你的消息！",
                    "这很不错呀！",
                    "继续加油！",
                    "你说得对！",
                    "我同意你的观点",
                    "这是自动回复消息",
                    "谢谢你的消息",
                    "很高兴和你聊天"
            };
            int index = (int) (System.currentTimeMillis() % replies.length);
            addMessageToChat(replies[index], false, System.currentTimeMillis());
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void addMessageToChat(String text, boolean isSender, long timestamp) {
        User sender = isSender ? currentUser : otherUser;

        MessageDisplay messageDisplay = new MessageDisplay(sender, text, isSender, timestamp);
        messageDisplay.setAlignmentX(Component.CENTER_ALIGNMENT);
        messageDisplay.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        messageDisplay.setMaxMessageWidth(400);

        chatPanel.add(messageDisplay);
        chatPanel.add(Box.createVerticalStrut(5));

        // 滚动到底部
        SwingUtilities.invokeLater(() -> {
            chatPanel.revalidate();
            chatPanel.repaint();
            JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
            verticalBar.setValue(verticalBar.getMaximum());
        });
    }

    /**
     * 创建默认头像（带颜色的圆形）
     */
    private ImageIcon createDefaultAvatar(Color color) {
        BufferedImage image = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(color);
        g2d.fillOval(0, 0, 40, 40);

        g2d.dispose();
        return new ImageIcon(image);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MessageDisplayDemo());
    }
}

