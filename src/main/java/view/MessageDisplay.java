package view;

import model.User;
import util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 消息显示组件 - 支持撤回功能
 */
public class MessageDisplay extends JPanel {
    private User user;
    private String message;
    private boolean isSender;
    private long timestamp;
    private long messageId;
    private int maxMessageWidth = 350;
    private int avatarSize = 40;
    private int messageAreaMargin = 10;

    private Color senderBubbleColor = new Color(79, 183, 245);
    private Color receiverBubbleColor = new Color(230, 230, 230);
    private Color senderTextColor = Color.WHITE;
    private Color receiverTextColor = Color.BLACK;
    private Color timeTextColor = new Color(150, 150, 150);
    private Color usernameColor = new Color(100, 100, 100);

    private boolean isDeleted = false;
    private Consumer<Long> onRecallCallback;
    private boolean canRecall = false; // 是否可以撤回

    public MessageDisplay(User user, String message, boolean isSender) {
        this.user = user;
        this.message = message != null ? message : "";
        this.isSender = isSender;
        this.timestamp = System.currentTimeMillis();

        setOpaque(false);
        setFont(new Font("微软雅黑", Font.PLAIN, 13));
    }

    public MessageDisplay(User user, String message, boolean isSender, long timestamp) {
        this.user = user;
        this.message = message != null ? message : "";
        this.isSender = isSender;
        this.timestamp = timestamp;

        setOpaque(false);
        setFont(new Font("微软雅黑", Font.PLAIN, 13));
    }

    public MessageDisplay(User user, String message, boolean isSender, long timestamp, long messageId) {
        this.user = user;
        this.message = message != null ? message : "";
        this.isSender = isSender;
        this.timestamp = timestamp;
        this.messageId = messageId;
        this.canRecall = isSender; // 只有发送者可以撤回

        setOpaque(false);
        setFont(new Font("微软雅黑", Font.PLAIN, 13));

        // 添加鼠标监听器
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isSender && SwingUtilities.isRightMouseButton(e)) {
                    showContextMenu(e);
                }
            }
        });
    }

    /**
     * 显示右键菜单
     */
    private void showContextMenu(MouseEvent e) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem recallItem = new JMenuItem("撤回消息");
        recallItem.addActionListener(action -> {
            if (onRecallCallback != null) {
                onRecallCallback.accept(messageId);
                isDeleted = true;
                repaint();
            }
        });
        menu.add(recallItem);

        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * 将文本分行显示
     */
    private String[] wrapText(String text, FontMetrics fm) {
        if (text == null || text.isEmpty()) {
            return new String[]{""};
        }

        List<String> lines = new ArrayList<>();
        String[] paragraphs = text.split("\n");

        for (String paragraph : paragraphs) {
            if (paragraph.isEmpty()) {
                lines.add("");
                continue;
            }

            StringBuilder currentLine = new StringBuilder();
            for (char c : paragraph.toCharArray()) {
                String testLine = currentLine.toString() + c;
                if (fm.stringWidth(testLine) < maxMessageWidth - 20) {
                    currentLine.append(c);
                } else {
                    if (currentLine.length() > 0) {
                        lines.add(currentLine.toString());
                        currentLine = new StringBuilder();
                    }
                    currentLine.append(c);
                }
            }

            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }
        }

        return lines.isEmpty() ? new String[]{""} : lines.toArray(new String[0]);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (g == null || user == null) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        try {
            Font messageFont = getFont();
            if (messageFont == null) {
                messageFont = new Font("微软雅黑", Font.PLAIN, 13);
            }
            g2d.setFont(messageFont);

            FontMetrics fm = g2d.getFontMetrics(messageFont);
            if (fm == null) {
                return;
            }

            // 如果消息已被撤回
            if (isDeleted) {
                drawDeletedMessage(g2d, fm);
                return;
            }

            // 计算文本相关尺寸
            String[] messageLines = wrapText(message, fm);
            int lineHeight = fm.getHeight();
            int messageHeight = lineHeight * messageLines.length;
            int maxTextWidth = 0;

            for (String line : messageLines) {
                if (line != null) {
                    maxTextWidth = Math.max(maxTextWidth, fm.stringWidth(line));
                }
            }

            maxTextWidth = Math.max(maxTextWidth, 30);

            Font usernameFont = new Font("微软雅黑", Font.BOLD, 12);
            FontMetrics usernameFm = g2d.getFontMetrics(usernameFont);
            int usernameHeight = usernameFm != null ? usernameFm.getHeight() : 20;

            Font timeFont = new Font("微软雅黑", Font.PLAIN, 11);
            FontMetrics timeFm = g2d.getFontMetrics(timeFont);
            int timeHeight = timeFm != null ? timeFm.getHeight() : 16;

            int panelWidth = getWidth();
            if (panelWidth <= 0) {
                panelWidth = 500;
            }

            int bubbleWidth = maxTextWidth + 20;
            int bubbleHeight = messageHeight + 16;

            int avatarX, messageX, messageY, nameX;
            messageY = messageAreaMargin;

            if (isSender) {
                avatarX = panelWidth - avatarSize - 10;
                messageX = panelWidth - bubbleWidth - avatarSize - 15;
                nameX = messageX;
            } else {
                avatarX = 10;
                messageX = avatarSize + 15;
                nameX = messageX;
            }

            if (messageX < 5) {
                messageX = 5;
            }
            if (messageX + bubbleWidth > panelWidth - 5) {
                messageX = panelWidth - bubbleWidth - 5;
            }

            int nameY = messageY + usernameHeight;

            g2d.setColor(usernameColor);
            g2d.setFont(usernameFont);
            String username = user.getName() != null ? user.getName() : "用户";
            g2d.drawString(username, nameX, nameY);

            String timeStr = DateUtil.formatTime(timestamp);
            g2d.setColor(timeTextColor);
            g2d.setFont(timeFont);
            int timeX = nameX + timeFm.stringWidth(username) + 10;
            g2d.drawString(timeStr, timeX, nameY);

            messageY = nameY + 8;

            drawAvatar(g2d, avatarX, messageY, user.getHeadIcon());

            RoundRectangle2D bubble = new RoundRectangle2D.Float(
                    messageX, messageY, bubbleWidth, bubbleHeight, 12, 12
            );

            if (isSender) {
                g2d.setColor(senderBubbleColor);
            } else {
                g2d.setColor(receiverBubbleColor);
            }
            g2d.fill(bubble);

            g2d.setColor(new Color(150, 150, 150, 50));
            g2d.setStroke(new BasicStroke(1));
            g2d.draw(bubble);

            if (isSender) {
                g2d.setColor(senderTextColor);
            } else {
                g2d.setColor(receiverTextColor);
            }
            g2d.setFont(messageFont);
            int textX = messageX + 10;
            int textY = messageY + 10 + fm.getAscent();

            for (String line : messageLines) {
                if (line != null && !line.isEmpty()) {
                    g2d.drawString(line, textX, textY);
                }
                textY += lineHeight;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 绘制已撤回的消息
     */
    private void drawDeletedMessage(Graphics2D g2d, FontMetrics fm) {
        String deletedText = "该消息已被撤回";
        int textWidth = fm.stringWidth(deletedText);
        int x = (getWidth() - textWidth) / 2;
        int y = getHeight() / 2 + fm.getAscent() / 2;

        g2d.setColor(new Color(160, 160, 160));
        g2d.setFont(new Font("微软雅黑", Font.ITALIC, 12));
        g2d.drawString(deletedText, x, y);
    }

    /**
     * 绘制头像
     */
    private void drawAvatar(Graphics2D g2d, int x, int y, ImageIcon avatar) {
        try {
            if (avatar != null && avatar.getImage() != null) {
                Image scaledImage = avatar.getImage().getScaledInstance(
                        avatarSize, avatarSize, Image.SCALE_SMOOTH
                );

                Ellipse2D circle = new Ellipse2D.Float(x, y, avatarSize, avatarSize);
                Shape oldClip = g2d.getClip();
                g2d.setClip(circle);

                g2d.drawImage(scaledImage, x, y, null);

                g2d.setClip(oldClip);

                g2d.setColor(new Color(200, 200, 200));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawOval(x, y, avatarSize, avatarSize);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        try {
            FontMetrics fm = getFontMetrics(getFont());
            if (fm == null) {
                return new Dimension(500, 100);
            }

            if (isDeleted) {
                return new Dimension(500, 50);
            }

            String[] lines = wrapText(message, fm);

            int lineHeight = fm.getHeight();
            int messageHeight = lineHeight * Math.max(lines.length, 1);
            int maxTextWidth = 0;
            for (String line : lines) {
                if (line != null) {
                    maxTextWidth = Math.max(maxTextWidth, fm.stringWidth(line));
                }
            }

            FontMetrics usernameFm = getFontMetrics(new Font("微软雅黑", Font.BOLD, 12));
            int usernameHeight = usernameFm != null ? usernameFm.getHeight() : 20;

            int totalHeight = messageAreaMargin + usernameHeight + 8 + messageHeight + 16 + messageAreaMargin;
            int totalWidth = 500;

            return new Dimension(totalWidth, Math.max(totalHeight, 80));
        } catch (Exception e) {
            return new Dimension(500, 100);
        }
    }

    // Getters and Setters
    public void setMessage(String message) {
        this.message = message != null ? message : "";
        repaint();
        revalidate();
    }

    public void setDeleted(boolean deleted) {
        this.isDeleted = deleted;
        repaint();
    }

    public void setOnRecallCallback(Consumer<Long> callback) {
        this.onRecallCallback = callback;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMaxMessageWidth(int width) {
        this.maxMessageWidth = Math.max(width, 100);
        repaint();
        revalidate();
    }
}



