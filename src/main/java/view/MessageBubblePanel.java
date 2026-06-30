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

        // ========== 头像 + 名称面板 ==========
        JPanel avatarPanel = new JPanel();
        avatarPanel.setLayout(new BoxLayout(avatarPanel, BoxLayout.Y_AXIS));
        avatarPanel.setOpaque(false);

        // 名称标签
        JLabel nameLabel = new JLabel(sender.getName() != null ? sender.getName() : sender.getUsername());
        nameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        nameLabel.setForeground(new Color(120, 120, 120));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 头像
        JLabel avatarLabel = new JLabel(new ImageIcon(
                sender.getHeadIcon().getImage().getScaledInstance(36, 36, Image.SCALE_SMOOTH)
        ));
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        avatarPanel.add(nameLabel);
        avatarPanel.add(Box.createVerticalStrut(3));
        avatarPanel.add(avatarLabel);

        // ========== 气泡 + 时间 ==========
        bubble = new ChatBubble(msg.getContent(), isSender);
        bubble.setMaxWidth(300);
        bubble.setShowTail(false);     // 纯圆角矩形
        bubble.setArcSize(18);         // 圆角大小

        JLabel timeLabel = new JLabel(DateUtil.formatTime(msg.getCreatedAt()));
        timeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        timeLabel.setForeground(new Color(160, 160, 160));

        JPanel bubblePanel = new JPanel(new BorderLayout(0, 4));
        bubblePanel.setOpaque(false);
        bubblePanel.add(bubble, BorderLayout.CENTER);
        bubblePanel.add(timeLabel, BorderLayout.SOUTH);

        // ========== 组装布局 ==========
        // 自己的消息：头像在右，气泡在左
        // 对方的消息：头像在左，气泡在右
        if (isSender) {
            add(bubblePanel, BorderLayout.CENTER);
            add(avatarPanel, BorderLayout.EAST);
            // 时间右对齐
            timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        } else {
            add(avatarPanel, BorderLayout.WEST);
            add(bubblePanel, BorderLayout.CENTER);
            timeLabel.setHorizontalAlignment(SwingConstants.LEFT);
        }

        //右键撤回（仅自己的正式消息）
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

    public void setOnRecallCallback(Consumer<Long> cb) {
        this.recallCallback = cb;
    }
    public void setMessageMaxWidth(int width) {
        if (bubble != null) {
            bubble.setMaxWidth(width);
        }
    }
    @Override
    public Dimension getPreferredSize() {
        Dimension bubbleDim = (bubble != null) ? bubble.getPreferredSize() : new Dimension(0, 0);
        int avatarWidth = 50;   // 头像区域固定宽度
        int height = Math.max(bubbleDim.height, 60);
        return new Dimension(bubbleDim.width + avatarWidth, height);
    }
}



