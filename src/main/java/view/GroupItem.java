package view;

import model.Group;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class GroupItem extends JPanel {
    private JLabel groupNameLabel;
    private JLabel memberCountLabel;
    private Group group;
    private boolean isSelected = false;
    private Consumer<Group> onSelectCallback;
    private Consumer<MouseEvent> onRightClick;

    public void setOnRightClick(Consumer<MouseEvent> callback) {
        this.onRightClick = callback;
    }
    public GroupItem(Group group, Consumer<Group> onSelectCallback) {
        this.group = group;
        this.onSelectCallback = onSelectCallback;

        initializeUI();
        addMouseListener();
    }

    private void initializeUI() {
        setLayout(null);
        setPreferredSize(new Dimension(250, 60));
        setMaximumSize(new Dimension(250, 60));
        setMinimumSize(new Dimension(250, 60));
        setBackground(new Color(239, 239, 239));
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        // 群组头像（用首字母代替）
        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(new Color(79, 183, 245));
                g2d.fillOval(0, 0, 48, 48);

                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("微软雅黑", Font.BOLD, 18));
                String initial = group.getGroupName().substring(0, 1);
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (48 - fm.stringWidth(initial)) / 2;
                int textY = (48 - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(initial, textX, textY);
            }
        };
        avatarPanel.setBounds(6, 6, 48, 48);
        avatarPanel.setOpaque(false);
        add(avatarPanel);

        // 群组名称
        groupNameLabel = new JLabel(group.getGroupName());
        groupNameLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        groupNameLabel.setBounds(60, 8, 170, 20);
        add(groupNameLabel);

        // 成员数量
        /*memberCountLabel = new JLabel(group.getMemberCount() + " 人");
        memberCountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        memberCountLabel.setForeground(new Color(120, 120, 120));
        memberCountLabel.setBounds(60, 30, 170, 20);
        add(memberCountLabel);*/
    }

    private void addMouseListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    setSelected(true);
                    if (onSelectCallback != null) {
                        onSelectCallback.accept(group);
                    }
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    if (onRightClick != null) {
                        onRightClick.accept(e);
                    }
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isSelected) {
                    setBackground(new Color(227, 227, 227));
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (!isSelected) {
                    setBackground(new Color(239, 239, 239));
                }
            }
        });
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
        if (selected) {
            setBackground(new Color(217, 217, 217));
        } else {
            setBackground(new Color(239, 239, 239));
        }
    }

    public boolean isSelected() {
        return isSelected;
    }

    public Group getGroup() {
        return group;
    }
}
