package view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * 高级聊天气泡组件
 * 支持多行文本、带尖角指示、阴影等高级特性
 */
public class ChatBubble extends JPanel {
    private String message;
    private boolean isSender;
    private Color bubbleColor;
    private Color textColor;
    private int arcSize = 15;
    private int padding = 12;
    private int tailSize = 10;  // 尖角大小
    private boolean showTail = true;  // 是否显示尖角
    private boolean showShadow = true;  // 是否显示阴影
    private int maxWidth = 300;  // 最大宽度，用于换行

    public ChatBubble(String message, boolean isSender) {
        this.message = message;
        this.isSender = isSender;

        if (isSender) {
            this.bubbleColor = new Color(79, 183, 245);
            this.textColor = Color.WHITE;
        } else {
            this.bubbleColor = new Color(230, 230, 230);
            this.textColor = Color.BLACK;
        }

        setOpaque(false);
        setFont(new Font("微软雅黑", Font.PLAIN, 14));
    }

    /**
     * 将文本分行显示
     */
    private String[] wrapText(String text, FontMetrics fm) {
        if (text == null || text.isEmpty()) return new String[]{""};

        List<String> lines = new ArrayList<>();
        int maxLineWidth = maxWidth - padding * 2; // 可用文本总宽度
        int currentWidth = 0;
        StringBuilder currentLine = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            // 处理换行符（保留）
            if (c == '\n') {
                lines.add(currentLine.toString());
                currentLine.setLength(0);
                currentWidth = 0;
                continue;
            }

            int charWidth = fm.charWidth(c);  // 单字符宽度（中英文都适应）
            if (currentWidth + charWidth > maxLineWidth) {
                // 当前字符放不下，先提交已有行
                lines.add(currentLine.toString());
                currentLine.setLength(0);
                currentWidth = 0;
            }

            currentLine.append(c);
            currentWidth += charWidth;
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines.isEmpty() ? new String[]{""} : lines.toArray(new String[0]);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        FontMetrics fm = g2d.getFontMetrics();
        String[] lines = wrapText(message, fm);

        int lineHeight = fm.getHeight();
        int textHeight = lineHeight * lines.length;
        int maxTextWidth = 0;
        for (String line : lines) {
            maxTextWidth = Math.max(maxTextWidth, fm.stringWidth(line));
        }

        int bubbleWidth = maxTextWidth + padding * 2;
        int bubbleHeight = textHeight + padding * 2;

        int x, y;
        if (isSender) {
            x = getWidth() - bubbleWidth - 20 - (showTail ? tailSize : 0);
        } else {
            x = 10 + (showTail ? tailSize : 0);
        }
        y = 10;

        // 绘制阴影
        if (showShadow) {
            g2d.setColor(new Color(0, 0, 0, 30));
            RoundRectangle2D shadow = new RoundRectangle2D.Float(
                    x + 2, y + 2, bubbleWidth, bubbleHeight, arcSize, arcSize
            );
            g2d.fill(shadow);
        }

        // 绘制气泡背景
        RoundRectangle2D bubble = new RoundRectangle2D.Float(
                x, y, bubbleWidth, bubbleHeight, arcSize, arcSize
        );
        g2d.setColor(bubbleColor);
        g2d.fill(bubble);

        // 绘制尖角
        if (showTail) {
            Path2D tail = new Path2D.Float();
            if (isSender) {
                // 右侧尖角
                tail.moveTo(x + bubbleWidth, y + 20);
                tail.lineTo(x + bubbleWidth + tailSize, y + 15);
                tail.lineTo(x + bubbleWidth, y + 30);
                tail.closePath();
            } else {
                // 左侧尖角
                tail.moveTo(x, y + 20);
                tail.lineTo(x - tailSize, y + 15);
                tail.lineTo(x, y + 30);
                tail.closePath();
            }
            g2d.fill(tail);
        }

        // 绘制气泡边框
        g2d.setColor(new Color(150, 150, 150, 30));
        g2d.setStroke(new BasicStroke(1));
        g2d.draw(bubble);

        // 绘制文字
        g2d.setColor(textColor);
        g2d.setFont(getFont());
        int textX = x + padding;
        int textY = y + padding + fm.getAscent();

        for (String line : lines) {
            g2d.drawString(line, textX, textY);
            textY += lineHeight;
        }
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        String[] lines = wrapText(message, fm);

        int lineHeight = fm.getHeight();
        int textHeight = lineHeight * lines.length;
        int maxTextWidth = 0;
        for (String line : lines) {
            maxTextWidth = Math.max(maxTextWidth, fm.stringWidth(line));
        }

        int bubbleWidth = maxTextWidth + padding * 2 + (showTail ? tailSize : 0);
        int bubbleHeight = textHeight + padding * 2 + 20;

        return new Dimension(bubbleWidth + 30, bubbleHeight);
    }

    // Getter和Setter
    public void setMessage(String message) {
        this.message = message;
        repaint();
        revalidate();
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        repaint();
        revalidate();
    }

    public void setShowTail(boolean showTail) {
        this.showTail = showTail;
        repaint();
    }

    public void setShowShadow(boolean showShadow) {
        this.showShadow = showShadow;
        repaint();
    }

    // 设置圆角大小
    public void setArcSize(int arcSize) {
        this.arcSize = arcSize;
        repaint();
    }


}




