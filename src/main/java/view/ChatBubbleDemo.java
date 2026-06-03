package view;

import javax.swing.*;
import java.awt.*;

/**
 * 聊天气泡组件演示
 */
public class ChatBubbleDemo extends JFrame {

    public ChatBubbleDemo() {
        setTitle("Chat Bubble Component Demo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);

        // 创建主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 添加滚动面板
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // 添加标题
        JLabel titleLabel = new JLabel("聊天气泡演示");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(20));

        // 添加示例气泡
        // 接收方消息
        ChatBubble bubble1 = new ChatBubble("你好，这是一条接收的消息eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee", false);
        bubble1.setMaximumSize(new Dimension(400, 50));
        mainPanel.add(bubble1);
        mainPanel.add(Box.createVerticalStrut(10));

        // 发送方消息
        ChatBubble bubble2 = new ChatBubble("你好！这是一条发送的消息", true);
        bubble2.setMaximumSize(new Dimension(400, 50));
        mainPanel.add(bubble2);
        mainPanel.add(Box.createVerticalStrut(10));

        // 接收方长消息
        ChatBubble bubble3 = new ChatBubble("这是一条比较长的接收消息，展示多行文本显示效果", false);
        bubble3.setMaximumSize(new Dimension(400, 80));
        mainPanel.add(bubble3);
        mainPanel.add(Box.createVerticalStrut(10));

        // 发送方短消息
        ChatBubble bubble4 = new ChatBubble("OK", true);
        bubble4.setMaximumSize(new Dimension(400, 50));
        mainPanel.add(bubble4);
        mainPanel.add(Box.createVerticalStrut(10));

        // 自定义颜色的气泡
        ChatBubble bubble5 = new ChatBubble("自定义颜色的气泡", false);
        //bubble5.setBubbleColor(new Color(100, 200, 100));
        //bubble5.setTextColor(Color.WHITE);
        bubble5.setMaximumSize(new Dimension(400, 50));
        mainPanel.add(bubble5);
        mainPanel.add(Box.createVerticalStrut(20));

        // 添加灵活的空间
        mainPanel.add(Box.createVerticalGlue());

        add(scrollPane);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatBubbleDemo());
    }
}

