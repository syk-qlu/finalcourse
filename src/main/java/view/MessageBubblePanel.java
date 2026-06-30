package view;

import model.User;
import model.Message;
import util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class MessageBubblePanel extends JPanel {
    private User sender;
    private Message message;
    private boolean isSender;
    private Consumer<Long> recallCallback;
    private ChatBubble bubble;

    public MessageBubblePanel(User sender, Message msg, boolean isSender) {
        this.sender = sender;
        this.message = msg;
        this.isSender = isSender;
        setLayout(new BorderLayout(8, 0));
        setOpaque(false);

        if (msg.isDeleted()) {
            JLabel deletedLabel = new JLabel("该消息已被撤回", SwingConstants.CENTER);
            deletedLabel.setFont(new Font("微软雅黑", Font.ITALIC, 12));
            deletedLabel.setForeground(Color.GRAY);
            add(deletedLabel, BorderLayout.CENTER);
            return;
        }

        // 头像
        JLabel avatarLabel = new JLabel(new ImageIcon(
                sender.getHeadIcon().getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH)
        ));

        // 气泡
        bubble = new ChatBubble(msg.getContent(), isSender);
        bubble.setMaxWidth(350);

        // 时间标签
        JLabel timeLabel = new JLabel(DateUtil.formatTime(msg.getCreatedAt()));
        timeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        timeLabel.setForeground(new Color(150, 150, 150));

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.add(bubble, BorderLayout.CENTER);
        if (isSender) {
            contentPanel.add(timeLabel, BorderLayout.WEST);
        } else {
            contentPanel.add(timeLabel, BorderLayout.EAST);
        }

        // ★ 修正头像位置：自己的消息头像在右侧，对方在左侧
        if (isSender) {
            add(contentPanel, BorderLayout.CENTER);
            add(avatarLabel, BorderLayout.EAST);
        } else {
            add(avatarLabel, BorderLayout.WEST);
            add(contentPanel, BorderLayout.CENTER);
        }

        // 右键撤回（仅自己的消息）
        if (isSender && msg.getMessageId() > 0) {
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e) && recallCallback != null) {
                        recallCallback.accept(msg.getMessageId());
                    }
                }
            });
        }
    }

    public void setOnRecallCallback(Consumer<Long> cb) { this.recallCallback = cb; }
}



