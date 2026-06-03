package view;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SmoothScroll {

    public static void enable(JScrollPane scrollPane) {
        JScrollBar bar = scrollPane.getVerticalScrollBar();
        Animation animation = new Animation(bar);

        // 关闭原生滚轮
        scrollPane.setWheelScrollingEnabled(false);

        // 滚轮事件
        scrollPane.addMouseWheelListener(e -> {
            int current = bar.getValue();
            int step = e.getWheelRotation() > 0 ? 45 : -45; // 一次滚多少
            animation.target = current + step; // 不叠加！直接设置目标
            animation.start();
        });
    }

    private static class Animation implements ActionListener {
        JScrollBar bar;
        int target;
        Timer timer;
        boolean isRunning = false;

        public Animation(JScrollBar bar) {
            this.bar = bar;
            timer = new Timer(8, this); // 流畅帧率
        }

        public void start() {
            if (!isRunning) {
                isRunning = true;
                timer.start();
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int current = bar.getValue();
            if (current == target) {
                timer.stop();
                isRunning = false;
                return;
            }

            // 每次直接靠近目标，不累积、不惯性
            int diff = target - current;
            int step = (int) Math.signum(diff) * Math.min(Math.abs(diff), 8);
            bar.setValue(current + step);
        }
    }
}
