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
    private ChatBubble bubble;
    private JPanel avatarPanel;
    private Consumer<Long> recallCallback;
    JPanel bubbleWithTime = new JPanel(new BorderLayout(0, 2));

    public MessageBubblePanel(User sender, Message msg, boolean isSender) {
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);

        if (msg.isDeleted()) {
            JLabel del = new JLabel("该消息已被撤回", SwingConstants.CENTER);
            del.setFont(new Font("微软雅黑", Font.ITALIC, 12));
            del.setForeground(Color.GRAY);
            add(del, BorderLayout.CENTER);
            return;
        }

        // 头像 + 名称
        avatarPanel = new JPanel();
        avatarPanel.setLayout(new BoxLayout(avatarPanel, BoxLayout.Y_AXIS));
        avatarPanel.setOpaque(false);
        avatarPanel.setPreferredSize(new Dimension(50, 10));
        avatarPanel.setMinimumSize(new Dimension(50, 10));
        avatarPanel.setMaximumSize(new Dimension(50, Integer.MAX_VALUE));
        JLabel name = new JLabel(sender.getName() != null ? sender.getName() : sender.getUsername());
        name.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        name.setForeground(new Color(120,120,120));
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel avatar = new JLabel(new ImageIcon(sender.getHeadIcon().getImage().getScaledInstance(36,36,Image.SCALE_SMOOTH)));
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        avatarPanel.add(name);
        avatarPanel.add(Box.createVerticalStrut(2));
        avatarPanel.add(avatar);

        // 气泡
        bubble = new ChatBubble(msg.getContent(), isSender);
        bubble.setShowTail(false);
        bubble.setArcSize(18);

        JLabel time = new JLabel(msg.getCreatedAt() > 0 ? DateUtil.formatTime(msg.getCreatedAt()) : "");
        time.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        time.setForeground(new Color(160,160,160));

        bubbleWithTime.setOpaque(false);
        bubbleWithTime.add(bubble, BorderLayout.CENTER);
        bubbleWithTime.add(time, BorderLayout.SOUTH);
        if (isSender) {
            add(bubbleWithTime, BorderLayout.CENTER);
            add(avatarPanel, BorderLayout.EAST);
        } else {
            add(avatarPanel, BorderLayout.WEST);
            add(bubbleWithTime, BorderLayout.CENTER);
        }

        if (isSender && msg.getMessageId() > 0) {
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e) && recallCallback != null)
                        recallCallback.accept(msg.getMessageId());
                }
            });
        }
    }

    public void setMessageMaxWidth(int w) { if (bubble != null) bubble.setMaxWidth(w); }
    public void setOnRecallCallback(Consumer<Long> cb) { this.recallCallback = cb; }

    @Override
    public Dimension getPreferredSize() {
        Dimension bubbleTimeSize = (bubbleWithTime != null)
                ? bubbleWithTime.getPreferredSize() : new Dimension(0, 0);
        Dimension avSize = (avatarPanel != null)
                ? avatarPanel.getPreferredSize() : new Dimension(0, 0);
        int w = bubbleTimeSize.width + avSize.width;
        int h = Math.max(bubbleTimeSize.height, avSize.height);
        return new Dimension(w, h);
    }
}



