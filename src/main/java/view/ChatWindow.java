package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * 完整的聊天窗口演示
 */
public class ChatWindow extends JFrame {
    private JPanel chatPanel;
    private JScrollPane scrollPane;
    private JTextField inputField;
    private JButton sendButton;
    private int messageCount = 0;

    public ChatWindow() {
        setTitle("Java Swing Chat Bubble Demo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 700);
        setLocationRelativeTo(null);

        // 创建顶部面板
        JPanel topPanel = createTopPanel();

        // 创建聊天内容面板
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(new Color(245, 245, 245));
        chatPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

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

        // 添加初始消息
        addInitialMessages();

        setVisible(true);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(79, 183, 245));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("聊天气泡组件演示");
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
        inputField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        inputField.addActionListener(e -> sendMessage());

        sendButton = new JButton("发送");
        sendButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        sendButton.setPreferredSize(new Dimension(80, 40));
        sendButton.addActionListener(e -> sendMessage());
        sendButton.setBackground(new Color(79, 183, 245));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);

        panel.add(inputField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);

        return panel;
    }

    private void addInitialMessages() {
        addMessage("你好，这是一条接收的消息", false);
        addMessage("我是用Java Swing实现的聊天气泡", true);
        addMessage("支持多行文本和自定义样式", false);
        addMessage("尝试在下面输入消息吧！", false);
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) {
            return;
        }

        // 添加发送的消息
        addMessage(text, true);

        // 清空输入框
        inputField.setText("");

        // 模拟接收回复
        Timer timer = new Timer(1000, e -> {
            String[] replies = {
                    "收到了！",
                    "好的，明白了",
                    "不错呀！",
                    "继续加油！",
                    "这是一条自动回复的消息",
                    "你的消息很有意思"
            };
            int index = messageCount % replies.length;
            addMessage(replies[index], false);
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void addMessage(String text, boolean isSender) {
        JPanel messagePanel = new JPanel(new FlowLayout(
                isSender ? FlowLayout.RIGHT : FlowLayout.LEFT, 10, 5));
        messagePanel.setBackground(new Color(245, 245, 245));
        messagePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        messagePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 添加头像（简单的圆形）
        JPanel avatarPanel = createAvatarPanel(isSender);

        // 创建气泡组件
        ChatBubble bubble = new ChatBubble(text, isSender);
        bubble.setMaximumSize(new Dimension(400, 150));

        // 根据发送方调整顺序
        if (isSender) {
            messagePanel.add(bubble);
            messagePanel.add(avatarPanel);
        } else {
            messagePanel.add(avatarPanel);
            messagePanel.add(bubble);
        }

        chatPanel.add(messagePanel);
        chatPanel.add(Box.createVerticalStrut(5));

        // 滚动到底部
        SwingUtilities.invokeLater(() -> {
            chatPanel.revalidate();
            chatPanel.repaint();
            JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
            verticalBar.setValue(verticalBar.getMaximum());
        });

        messageCount++;
    }

    private JPanel createAvatarPanel(boolean isSender) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 绘制圆形头像
                Color avatarColor = isSender ? new Color(79, 183, 245) : new Color(100, 200, 100);
                g2d.setColor(avatarColor);
                g2d.fillOval(2, 2, 36, 36);

                // 绘制首字母
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("微软雅黑", Font.BOLD, 18));
                String initial = isSender ? "我" : "Ta";
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(initial)) / 2;
                int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(initial, textX, textY);
            }
        };
        panel.setPreferredSize(new Dimension(40, 40));
        panel.setMaximumSize(new Dimension(40, 40));
        panel.setBackground(new Color(245, 245, 245));

        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatWindow());
    }
}

