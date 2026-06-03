package view;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

public class ScrollBarUI extends BasicScrollBarUI {

    private Color trackColor = new Color(240, 240, 240, 0); // 透明轨道
    private Color thumbColor = new Color(170, 170, 170);    // 正常滑块
    private final Color thumbHover = new Color(130, 130, 130);    // 悬浮滑块

    public ScrollBarUI() {}

    @Override
    protected void configureScrollBarColors() {
        thumbColor = null;
        trackColor = null;
    }

    // 绘制滑块（圆角）
    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        JScrollBar sb = (JScrollBar) c;
        if (!sb.isEnabled()) return;

        // 鼠标悬浮时颜色加深
        if (isThumbRollover()) {
            g2.setColor(thumbHover);
        } else {
            g2.setColor(thumbColor);
        }

        // 圆角矩形（更美观）
        g2.fillRoundRect(thumbBounds.x + 3, thumbBounds.y + 2,
                6, thumbBounds.height - 4, 6, 6);
    }

    // 绘制轨道（透明）
    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        // 不画轨道 = 更干净
    }

    // 去掉箭头按钮
    @Override
    protected JButton createDecreaseButton(int orientation) {
        return createEmptyButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return createEmptyButton();
    }

    private JButton createEmptyButton() {
        JButton btn = new JButton();
        btn.setPreferredSize(new Dimension(0, 0));
        btn.setMinimumSize(new Dimension(0, 0));
        btn.setMaximumSize(new Dimension(0, 0));
        return btn;
    }
}