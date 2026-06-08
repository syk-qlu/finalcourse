package view;

import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 消息显示组件
 * 包含用户头像、用户名、发送时间、消息内容
 * 支持自动换行
 */
public class MessageDisplay extends JPanel {
    private User user;
    private String message;
    private boolean isSender;  // true表示发送方（右对齐），false表示接收方（左对齐）
    private long timestamp;    // 消息发送时间戳
    private int maxMessageWidth = 350;  // 消息最大宽度
    private int avatarSize = 40;  // 头像大小
    private int messageAreaMargin = 10;  // 消息区域边距

    // 颜色设置
    private Color senderBubbleColor = new Color(79, 183, 245);  // 蓝色
    private Color receiverBubbleColor = new Color(230, 230, 230);  // 灰色
    private Color senderTextColor = Color.WHITE;
    private Color receiverTextColor = Color.BLACK;
    private Color timeTextColor = new Color(150, 150, 150);  // 时间文字颜色
    private Color usernameColor = new Color(100, 100, 100);  // 用户名颜色

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

    /**
     * 将文本分行显示
     */
    private String[] wrapText(String text, FontMetrics fm) {
        if (text == null || text.isEmpty()) {
            return new String[]{""};
        }

        List<String> lines = new ArrayList<>();

        // 先按换行符分割
        String[] paragraphs = text.split("\n");

        for (String paragraph : paragraphs) {
            if (paragraph.isEmpty()) {
                lines.add("");
                continue;
            }

            // 按空格或汉字分割
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

    /**
     * 格式化时间
     */
    private String formatTime(long timestamp) {
        try {
            SimpleDateFormat sdf;
            long now = System.currentTimeMillis();
            long diff = now - timestamp;

            // 如果是今天
            if (diff < 24 * 60 * 60 * 1000) {
                sdf = new SimpleDateFormat("HH:mm");
            } else {
                sdf = new SimpleDateFormat("MM-dd HH:mm");
            }

            return sdf.format(new Date(timestamp));
        } catch (Exception e) {
            return "未知时间";
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 检查必要参数
        if (g == null || user == null) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        try {
            // 获取字体信息
            Font messageFont = getFont();
            if (messageFont == null) {
                messageFont = new Font("微软雅黑", Font.PLAIN, 13);
            }
            g2d.setFont(messageFont);

            FontMetrics fm = g2d.getFontMetrics(messageFont);
            if (fm == null) {
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

            // 确保最小气泡宽度
            maxTextWidth = Math.max(maxTextWidth, 30);

            // 计算用户名高度
            Font usernameFont = new Font("微软雅黑", Font.BOLD, 12);
            FontMetrics usernameFm = g2d.getFontMetrics(usernameFont);
            int usernameHeight = usernameFm != null ? usernameFm.getHeight() : 20;

            // 计算时间戳高度
            Font timeFont = new Font("微软雅黑", Font.PLAIN, 11);
            FontMetrics timeFm = g2d.getFontMetrics(timeFont);
            int timeHeight = timeFm != null ? timeFm.getHeight() : 16;

            // 获取面板宽度
            int panelWidth = getWidth();
            if (panelWidth <= 0) {
                panelWidth = 500;
            }

            // 计算气泡宽度（包含内边距）
            int bubbleWidth = maxTextWidth + 20;
            int bubbleHeight = messageHeight + 16;

            // 计算头像和消息气泡位置
            int avatarX, messageX, messageY, nameX;
            messageY = messageAreaMargin;

            if (isSender) {
                // 发送方：头像在右侧，消息在左侧（向右对齐）
                avatarX = panelWidth - avatarSize - 10;
                messageX = panelWidth - bubbleWidth - avatarSize - 15;  // 确保不超出面板
                nameX = messageX;
            } else {
                // 接收方：头像在左侧，消息在右侧（向左对齐）
                avatarX = 10;
                messageX = avatarSize + 15;
                nameX = messageX;
            }

            // 确保消息框不超出面板边界
            if (messageX < 5) {
                messageX = 5;
            }
            if (messageX + bubbleWidth > panelWidth - 5) {
                messageX = panelWidth - bubbleWidth - 5;
            }

            // 绘制用户名和时间戳（顶部）
            int nameY = messageY + usernameHeight;

            g2d.setColor(usernameColor);
            g2d.setFont(usernameFont);
            String username = user.getName() != null ? user.getName() : "用户";
            g2d.drawString(username, nameX, nameY);

            // 绘制时间戳
            String timeStr = formatTime(timestamp);
            g2d.setColor(timeTextColor);
            g2d.setFont(timeFont);
            int timeX = nameX + timeFm.stringWidth(username) + 10;
            g2d.drawString(timeStr, timeX, nameY);

            // 调整消息气泡的Y坐标（在用户名下方）
            messageY = nameY + 8;

            // 绘制头像
            drawAvatar(g2d, avatarX, messageY, user.getHeadIcon());

            // 绘制消息气泡背景
            RoundRectangle2D bubble = new RoundRectangle2D.Float(
                    messageX, messageY, bubbleWidth, bubbleHeight, 12, 12
            );

            // 设置气泡背景色
            if (isSender) {
                g2d.setColor(senderBubbleColor);
            } else {
                g2d.setColor(receiverBubbleColor);
            }
            g2d.fill(bubble);

            // 绘制气泡边框
            g2d.setColor(new Color(150, 150, 150, 50));
            g2d.setStroke(new BasicStroke(1));
            g2d.draw(bubble);

            // 绘制消息文本
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
     * 绘制头像
     */
    private void drawAvatar(Graphics2D g2d, int x, int y, ImageIcon avatar) {
        try {
            if (avatar != null && avatar.getImage() != null) {
                // 缩放图片到指定大小
                Image scaledImage = avatar.getImage().getScaledInstance(
                        avatarSize, avatarSize, Image.SCALE_SMOOTH
                );

                // 创建圆形裁剪路径
                Ellipse2D circle = new Ellipse2D.Float(x, y, avatarSize, avatarSize);
                Shape oldClip = g2d.getClip();
                g2d.setClip(circle);

                // 绘制图片
                g2d.drawImage(scaledImage, x, y, null);

                // 恢复裁剪区域
                g2d.setClip(oldClip);

                // 绘制圆形边框
                g2d.setColor(new Color(200, 200, 200));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawOval(x, y, avatarSize, avatarSize);
            } else {
                // 如果没有头像，绘制默认圆形
                g2d.setColor(new Color(79, 183, 245));
                g2d.fillOval(x, y, avatarSize, avatarSize);

                // 绘制首字母
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("微软雅黑", Font.BOLD, 16));
                String initial = "?";
                if (user != null && user.getName() != null && !user.getName().isEmpty()) {
                    initial = user.getName().substring(0, 1);
                }
                FontMetrics fm = g2d.getFontMetrics();
                if (fm != null) {
                    int textX = x + (avatarSize - fm.stringWidth(initial)) / 2;
                    int textY = y + (avatarSize - fm.getHeight()) / 2 + fm.getAscent();
                    g2d.drawString(initial, textX, textY);
                }
            }
        } catch (Exception e) {
            // 绘制默认圆形
            g2d.setColor(new Color(79, 183, 245));
            g2d.fillOval(x, y, avatarSize, avatarSize);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        try {
            FontMetrics fm = getFontMetrics(getFont());
            if (fm == null) {
                return new Dimension(500, 100);
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
            int totalWidth = 500;  // 固定宽度

            return new Dimension(totalWidth, Math.max(totalHeight, 80));
        } catch (Exception e) {
            return new Dimension(500, 100);
        }
    }

    // Getter和Setter
    public void setMessage(String message) {
        this.message = message != null ? message : "";
        repaint();
        revalidate();
    }

    public void setUser(User user) {
        this.user = user;
        repaint();
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        repaint();
    }

    public void setMaxMessageWidth(int width) {
        this.maxMessageWidth = Math.max(width, 100);
        repaint();
        revalidate();
    }

    public void setAvatarSize(int size) {
        this.avatarSize = Math.max(size, 20);
        repaint();
        revalidate();
    }

    public void setSenderBubbleColor(Color color) {
        this.senderBubbleColor = color != null ? color : new Color(79, 183, 245);
        repaint();
    }

    public void setReceiverBubbleColor(Color color) {
        this.receiverBubbleColor = color != null ? color : new Color(230, 230, 230);
        repaint();
    }

    public void setSenderTextColor(Color color) {
        this.senderTextColor = color != null ? color : Color.WHITE;
        repaint();
    }

    public void setReceiverTextColor(Color color) {
        this.receiverTextColor = color != null ? color : Color.BLACK;
        repaint();
    }

    public void setTimeTextColor(Color color) {
        this.timeTextColor = color != null ? color : new Color(150, 150, 150);
        repaint();
    }

    public void setUsernameColor(Color color) {
        this.usernameColor = color != null ? color : new Color(100, 100, 100);
        repaint();
    }
}



