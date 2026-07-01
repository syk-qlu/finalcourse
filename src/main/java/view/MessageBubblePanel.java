package view;

import model.User;
import model.Message;
import util.DateUtil;
import util.FileUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

public class MessageBubblePanel extends JPanel {
    private ChatBubble bubble;               // 文本气泡（文件/图片时为 null）
    private JPanel avatarPanel;
    private Consumer<Long> recallCallback;
    JPanel bubbleWithTime = new JPanel(new BorderLayout(0, 2));

    public MessageBubblePanel(User sender, Message msg, boolean isSender) {
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);

        // 撤回消息
        if (msg.isDeleted()) {
            JLabel del = new JLabel("该消息已被撤回", SwingConstants.CENTER);
            del.setFont(new Font("微软雅黑", Font.ITALIC, 12));
            del.setForeground(Color.GRAY);
            add(del, BorderLayout.CENTER);
            return;
        }

        // ---- 头像 + 名称 ----
        avatarPanel = new JPanel();
        avatarPanel.setLayout(new BoxLayout(avatarPanel, BoxLayout.Y_AXIS));
        avatarPanel.setOpaque(false);
        avatarPanel.setPreferredSize(new Dimension(50, 10));
        avatarPanel.setMinimumSize(new Dimension(50, 10));
        avatarPanel.setMaximumSize(new Dimension(50, Integer.MAX_VALUE));

        JLabel name = new JLabel(sender.getName() != null ? sender.getName() : sender.getUsername());
        name.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        name.setForeground(new Color(120, 120, 120));
        name.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel avatar = new JLabel(new ImageIcon(
                sender.getHeadIcon().getImage().getScaledInstance(36, 36, Image.SCALE_SMOOTH)));
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        avatarPanel.add(name);
        avatarPanel.add(Box.createVerticalStrut(2));
        avatarPanel.add(avatar);

        // ---- 根据消息类型构建内容 ----
        if ("file".equals(msg.getMessageType()) || "image".equals(msg.getMessageType())) {
            // 文件卡片（取消文本气泡）
            bubble = null;
            JPanel fileCard = createFileCard(msg, isSender);
            bubbleWithTime.add(fileCard, BorderLayout.CENTER);
        } else {
            // 文本气泡
            bubble = new ChatBubble(msg.getContent(), isSender);
            bubble.setShowTail(false);
            bubble.setArcSize(18);
            bubbleWithTime.add(bubble, BorderLayout.CENTER);
        }

        // 时间标签
        JLabel time = new JLabel(msg.getCreatedAt() > 0 ? DateUtil.formatTime(msg.getCreatedAt()) : "");
        time.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        time.setForeground(new Color(160, 160, 160));
        bubbleWithTime.add(time, BorderLayout.SOUTH);

        // 整体布局：自己的消息头像在右，对方在左
        if (isSender) {
            add(bubbleWithTime, BorderLayout.CENTER);
            add(avatarPanel, BorderLayout.EAST);
        } else {
            add(avatarPanel, BorderLayout.WEST);
            add(bubbleWithTime, BorderLayout.CENTER);
        }

        // 右键撤回（仅自己的正式消息）
        if (isSender && msg.getMessageId() > 0) {
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e) && recallCallback != null)
                        recallCallback.accept(msg.getMessageId());
                }
            });
        }
    }

    /**
     * 创建文件卡片，包含图标、文件名、大小，接收方显示下载按钮
     */
    private JPanel createFileCard(Message msg, boolean isSender) {
        String filePath = msg.getContent();
        String fileName = new File(filePath).getName();
        long fileSize = 0;
        try { fileSize = new File(filePath).length(); } catch (Exception ignored) {}

        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(isSender ? new Color(18, 183, 245) : Color.WHITE);

        // 图标：图片用🖼，其他用📄
        JLabel iconLabel = new JLabel(FileUtil.isImage(fileName) ? "🖼" : "📄");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        card.add(iconLabel, BorderLayout.WEST);

        // 文件名和大小
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(fileName);
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 13));
        nameLabel.setForeground(isSender ? Color.WHITE : Color.BLACK);

        JLabel sizeLabel = new JLabel(FileUtil.formatFileSize(fileSize));
        sizeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        sizeLabel.setForeground(isSender ? new Color(220, 240, 255) : Color.GRAY);

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(2));
        infoPanel.add(sizeLabel);

        card.add(infoPanel, BorderLayout.CENTER);

        // 接收方显示下载按钮
        if (!isSender) {
            JButton downloadBtn = new JButton("下载");
            downloadBtn.setFont(new Font("微软雅黑", Font.PLAIN, 11));
            downloadBtn.setFocusPainted(false);
            downloadBtn.setBackground(new Color(52, 211, 153));
            downloadBtn.setForeground(Color.WHITE);
            downloadBtn.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            downloadBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            downloadBtn.addActionListener(e -> downloadFile(filePath, fileName));
            card.add(downloadBtn, BorderLayout.EAST);
        }

        // 圆角边框
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200, 100), 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        return card;
    }

    /**
     * 下载文件：弹出保存对话框，复制后打开文件
     */
    private void downloadFile(String sourcePath, String defaultName) {
        // 1. 检查源文件是否存在
        File sourceFile = new File(sourcePath);
        if (!sourceFile.exists()) {
            JOptionPane.showMessageDialog(this.getParent(),
                    "源文件不存在，可能已被删除或路径错误:\n" + sourceFile.getAbsolutePath(),
                    "下载失败", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 弹出保存对话框
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(defaultName));
        int result = chooser.showSaveDialog(this.getParent());
        if (result != JFileChooser.APPROVE_OPTION) return;

        File target = chooser.getSelectedFile();
        if (target == null) return;

        //检查目标路径是否可写
        if (target.exists() && !target.canWrite()) {
            JOptionPane.showMessageDialog(this.getParent(),
                    "文件无法覆盖，请检查是否被其他程序占用或权限不足",
                    "下载失败", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //执行复制
        try {
            Files.copy(sourceFile.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            JOptionPane.showMessageDialog(this.getParent(), "下载完成，文件已保存到:\n" + target.getAbsolutePath());
            // 打开文件
            FileUtil.openFile(target.getAbsolutePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this.getParent(),
                    "下载失败: " + ex.getMessage() + "\n请检查目标路径是否有效",
                    "下载失败", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public void setMessageMaxWidth(int w) {
        if (bubble != null) bubble.setMaxWidth(w);
    }

    public void setOnRecallCallback(Consumer<Long> cb) {
        this.recallCallback = cb;
    }

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



