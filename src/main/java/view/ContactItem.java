package view;

import model.User;
import util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class ContactItem extends JPanel {
    private JLabel headLabel, nameLabel, lastMsgLabel, timeLabel, badgeLabel;
    private User user;
    private boolean selected;
    private Consumer<User> onSelectCallback;
    private Consumer<ContactItem> onDeleteCallback, onTopCallback;
    private int unreadCount = 0;
    private String lastMessage;

    public ContactItem(User user, Consumer<User> onSelectCallback) {
        this.user = user;
        this.onSelectCallback = onSelectCallback;
        setLayout(null);
        setPreferredSize(new Dimension(250, 64));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        setMinimumSize(new Dimension(150, 64));
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        initComponents();
        addMouseListeners();
    }

    private void initComponents() {
        headLabel = new JLabel(new ImageIcon(user.getHeadIcon().getImage().getScaledInstance(44, 44, Image.SCALE_SMOOTH)));
        headLabel.setBounds(8, 10, 44, 44);
        add(headLabel);

        nameLabel = new JLabel(user.getUsername());
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 13));
        nameLabel.setBounds(60, 8, 140, 18);
        add(nameLabel);

        lastMsgLabel = new JLabel("");
        lastMsgLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        lastMsgLabel.setForeground(new Color(130, 130, 130));
        lastMsgLabel.setBounds(60, 30, 150, 16);
        add(lastMsgLabel);

        timeLabel = new JLabel("");
        timeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        timeLabel.setForeground(new Color(160, 160, 160));
        timeLabel.setBounds(180, 8, 60, 14);
        timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        add(timeLabel);

        // 红点
        badgeLabel = new JLabel("", SwingConstants.CENTER);
        badgeLabel.setFont(new Font("微软雅黑", Font.BOLD, 10));
        badgeLabel.setForeground(Color.WHITE);
        badgeLabel.setBackground(Color.RED);
        badgeLabel.setOpaque(true);
        badgeLabel.setBounds(180, 30, 20, 20);
        badgeLabel.setVisible(false);
        add(badgeLabel);
    }

    private void addMouseListeners() {
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    setSelected(true);
                    if (onSelectCallback != null) onSelectCallback.accept(user);
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    showContextMenu(e);
                }
            }
            public void mouseEntered(MouseEvent e) { if (!selected) setBackground(new Color(230, 230, 230)); }
            public void mouseExited(MouseEvent e) { if (!selected) setBackground(new Color(245, 245, 245)); }
        });
    }

    private void showContextMenu(MouseEvent e) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem topItem = new JMenuItem("置顶");
        topItem.addActionListener(ae -> { if (onTopCallback != null) onTopCallback.accept(this); });
        menu.add(topItem);
        JMenuItem delItem = new JMenuItem("删除好友");
        delItem.addActionListener(ae -> { if (onDeleteCallback != null) onDeleteCallback.accept(this); });
        menu.add(delItem);
        menu.show(this, e.getX(), e.getY());
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        setBackground(selected ? new Color(200, 220, 240) : new Color(245, 245, 245));
    }

    public void setUnreadCount(int count) {
        this.unreadCount = count;
        if (count > 0) {
            badgeLabel.setText(count > 99 ? "99+" : String.valueOf(count));
            badgeLabel.setVisible(true);
        } else {
            badgeLabel.setVisible(false);
        }
    }

    public void setLastMessage(String msg) { this.lastMessage = msg; lastMsgLabel.setText(msg); }
    public User getUser() { return user; }
    public void setOnDeleteCallback(Consumer<ContactItem> cb) { this.onDeleteCallback = cb; }
    public void setOnTopCallback(Consumer<ContactItem> cb) { this.onTopCallback = cb; }
}
