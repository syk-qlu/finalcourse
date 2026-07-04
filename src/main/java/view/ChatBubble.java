package view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class ChatBubble extends JPanel {
    private String message;
    private boolean isSender;
    private Color bubbleColor, textColor;
    private int arcSize = 18, padding = 10, maxWidth = 300;
    private boolean showTail = false, showShadow = true;

    public ChatBubble(String message, boolean isSender) {
        this.message = message;
        this.isSender = isSender;
        this.bubbleColor = isSender ? new Color(18, 183, 245) : Color.WHITE;
        this.textColor = isSender ? Color.WHITE : Color.BLACK;
        setOpaque(false);
        setFont(new Font("微软雅黑", Font.PLAIN, 14));
    }

    // 精确换行（中英文均可）
    private String[] wrapText(String text, FontMetrics fm) {
        List<String> lines = new ArrayList<>();
        int maxLineW = maxWidth - 2 * padding;
        StringBuilder line = new StringBuilder();
        int lineW = 0;
        for (char c : text.toCharArray()) {
            if (c == '\n') {
                lines.add(line.toString());
                line.setLength(0); lineW = 0;
                continue;
            }
            int cw = fm.charWidth(c);
            if (lineW + cw > maxLineW && line.length() > 0) {
                lines.add(line.toString());
                line.setLength(0); lineW = 0;
            }
            line.append(c);
            lineW += cw;
        }
        if (line.length() > 0) lines.add(line.toString());
        return lines.isEmpty() ? new String[]{""} : lines.toArray(new String[0]);
    }
    //绘制气泡
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        FontMetrics fm = g2d.getFontMetrics();
        String[] lines = wrapText(message, fm);
        int lineH = fm.getHeight();
        int textH = lineH * lines.length;
        int maxTextW = 0;
        for (String line : lines) maxTextW = Math.max(maxTextW, fm.stringWidth(line));
        int bubbleW = maxTextW + padding * 2;
        if (showTail) bubbleW += 10;
        int bubbleH = textH + padding * 2;

        int y = 2;                     // 顶部留白
        int x = isSender ? getWidth() - bubbleW - 2 : 2;

        // 阴影
        if (showShadow) {
            g2d.setColor(new Color(0, 0, 0, 30));
            g2d.fill(new RoundRectangle2D.Float(x + 2, y + 2, bubbleW, bubbleH, arcSize, arcSize));
        }
        // 气泡
        g2d.setColor(bubbleColor);
        g2d.fill(new RoundRectangle2D.Float(x, y, bubbleW, bubbleH, arcSize, arcSize));
        // 尖角
        if (showTail) {
            Path2D tail = new Path2D.Float();
            if (isSender) {
                tail.moveTo(x + bubbleW, y + 12);
                tail.lineTo(x + bubbleW + 10, y + 8);
                tail.lineTo(x + bubbleW, y + 20);
            } else {
                tail.moveTo(x, y + 12);
                tail.lineTo(x - 10, y + 8);
                tail.lineTo(x, y + 20);
            }
            tail.closePath();
            g2d.fill(tail);
        }
        // 边框（轻量）
        g2d.setColor(new Color(150, 150, 150, 50));
        g2d.draw(new RoundRectangle2D.Float(x, y, bubbleW, bubbleH, arcSize, arcSize));

        // 文字
        g2d.setColor(textColor);
        int textX = x + padding;
        int textY = y + padding + fm.getAscent();
        for (String line : lines) {
            g2d.drawString(line, textX, textY);
            textY += lineH;
        }
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        String[] lines = wrapText(message, fm);
        int lineH = fm.getHeight();
        int textH = lineH * lines.length;
        int maxTextW = 0;
        for (String line : lines) maxTextW = Math.max(maxTextW, fm.stringWidth(line));
        int bubbleW = maxTextW + padding * 2;
        if (showTail) bubbleW += 10;
        int bubbleH = textH + padding * 2;
        int totalW = 2 + bubbleW + 2;          // 左右各留2px
        int totalH = 2 + (showShadow ? 2 : 0) + bubbleH + 2;
        return new Dimension(totalW, totalH);
    }

    public void setMaxWidth(int w) { this.maxWidth = w; revalidate(); repaint(); }
    public void setArcSize(int a) { this.arcSize = a; repaint(); }
    public void setShowTail(boolean b) { this.showTail = b; repaint(); }
    public void setShowShadow(boolean b) { this.showShadow = b; repaint(); }
}




