package view;

import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class ContactItem extends JPanel {
    private JLabel headLabel;
    private JLabel nameLabel;
    private JLabel statusLabel;
    private User user;
    private boolean isSelected = false;
    private Consumer<User> onSelectCallback;
    private Consumer<ContactItem> onDeleteCallback;
    private Consumer<ContactItem> onTopCallback;

    public ContactItem(User user, Consumer<User> onSelectCallback) {
        this.user = user;
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

        // 头像
        headLabel = new JLabel(new ImageIcon(
                user.getHeadIcon().getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH)
        ));
        headLabel.setBounds(6, 6, 48, 48);
        add(headLabel);

        // 用户名
        nameLabel = new JLabel(user.getUsername());
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        nameLabel.setBounds(60, 8, 170, 20);
        add(nameLabel);

        // 状态指示器
        statusLabel = new JLabel();
        statusLabel.setBounds(230, 8, 15, 15);
        statusLabel.setOpaque(true);
        statusLabel.setBackground("online".equals(user.getStatus())
                ? new Color(52, 211, 153) : new Color(160, 160, 160));
        add(statusLabel);
    }

    private void addMouseListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    setSelected(true);
                    if (onSelectCallback != null) {
                        onSelectCallback.accept(user);
                    }
                }
                if (SwingUtilities.isRightMouseButton(e)) {
                    showContextMenu(e);
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

    /**
     * 显示右键菜单
     */
    private void showContextMenu(MouseEvent e) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem topItem = new JMenuItem("置顶聊天");
        topItem.addActionListener(action -> {
            if (onTopCallback != null) {
                onTopCallback.accept(this);
            }
        });
        menu.add(topItem);

        menu.addSeparator();

        JMenuItem deleteItem = new JMenuItem("删除好友");
        deleteItem.addActionListener(action -> {
            int result = JOptionPane.showConfirmDialog(this,
                    "确定要删除 " + user.getUsername() + " 吗？",
                    "删除好友",
                    JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION && onDeleteCallback != null) {
                onDeleteCallback.accept(this);
            }
        });
        menu.add(deleteItem);

        menu.show(e.getComponent(), e.getX(), e.getY());
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

    public User getUser() {
        return user;
    }

    public void updateStatus(String status) {
        user.setStatus(status);
        statusLabel.setBackground("online".equals(status)
                ? new Color(52, 211, 153) : new Color(160, 160, 160));
    }

    public void setOnDeleteCallback(Consumer<ContactItem> callback) {
        this.onDeleteCallback = callback;
    }

    public void setOnTopCallback(Consumer<ContactItem> callback) {
        this.onTopCallback = callback;
    }
}
