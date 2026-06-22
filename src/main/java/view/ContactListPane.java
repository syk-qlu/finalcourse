package view;

import model.User;
import service.ChatService;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

import static javax.swing.BoxLayout.Y_AXIS;

public class ContactListPane extends JPanel {
    private JPanel listPanel;
    private JTextField searchField;
    private List<User> contacts;
    private Consumer<User> onSelectCallback;
    private ContactItem selectedItem;
    private ChatService chatService;
    private int currentUserId;
    JScrollPane listScroller = new JScrollPane();

    public ContactListPane(List<User> contacts, Consumer<User> onSelectCallback,
                           ChatService chatService, int currentUserId) {
        this.contacts = contacts;
        this.onSelectCallback = onSelectCallback;
        this.chatService = chatService;
        this.currentUserId = currentUserId;

        setLayout(null);
        setBackground(new Color(239, 239, 239));

        // 搜索框
        searchField = new JTextField();
        searchField.setBounds(10, 10, 190, 30);
        searchField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        searchField.setMargin(new Insets(5, 5, 5, 5));
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                filterContacts(searchField.getText());
            }
        });
        add(searchField);

        // 联系人列表面板
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, Y_AXIS));
        listPanel.setBackground(new Color(239, 239, 239));

        // 滚动面板
        JScrollPane listScroller = new JScrollPane(listPanel);
        listScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        listScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JScrollBar verticalBar = listScroller.getVerticalScrollBar();
        verticalBar.setUI(new ScrollBarUI());
        verticalBar.setPreferredSize(new Dimension(12, 0));
        listScroller.setBorder(null);
        listScroller.setViewportBorder(null);
        listScroller.getViewport().setBorder(null);
        listScroller.setBounds(0, 50, 250, 600);
        SmoothScroll.enable(listScroller);
        add(listScroller);

        // 添加联系人
        updateContacts(contacts);
    }

    public void updateContacts(List<User> newContacts) {
        this.contacts = newContacts;
        listPanel.removeAll();

        for (User user : contacts) {
            ContactItem item = new ContactItem(user, this::selectItem);

            // 设置删除好友回调
            item.setOnDeleteCallback(contactItem -> {
                boolean success = chatService.removeFriend(currentUserId, contactItem.getUser().getUserId());
                if (success) {
                    contacts.remove(contactItem.getUser());
                    updateContacts(contacts);
                    JOptionPane.showMessageDialog(this, "好友已删除");
                } else {
                    JOptionPane.showMessageDialog(this, "删除好友失败");
                }
            });

            // 设置置顶回调
            item.setOnTopCallback(contactItem -> {
                boolean isTopped = chatService.isTopped(currentUserId, contactItem.getUser().getUserId());
                boolean success;
                if (isTopped) {
                    success = chatService.unTopFriend(currentUserId, contactItem.getUser().getUserId());
                    JOptionPane.showMessageDialog(this, "已取消置顶");
                } else {
                    success = chatService.topFriend(currentUserId, contactItem.getUser().getUserId());
                    JOptionPane.showMessageDialog(this, "已置顶");
                }
                if (success) {
                    // 重新加载列表
                    List<User> updatedFriends = chatService.getFriendList(currentUserId);
                    updateContacts(updatedFriends);
                }
            });

            listPanel.add(item);
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    public void selectItem(User user) {
        // 取消之前的选中状态
        for (Component comp : listPanel.getComponents()) {
            if (comp instanceof ContactItem) {
                ContactItem item = (ContactItem) comp;
                item.setSelected(false);
            }
        }

        // 设置当前选中
        for (Component comp : listPanel.getComponents()) {
            if (comp instanceof ContactItem) {
                ContactItem item = (ContactItem) comp;
                if (item.getUser().equals(user)) {
                    item.setSelected(true);
                    selectedItem = item;
                    break;
                }
            }
        }

        // 调用回调
        if (onSelectCallback != null) {
            onSelectCallback.accept(user);
        }
    }
    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        if (listScroller != null) {
            listScroller.setBounds(0, 50, width, height - 50);
        }
        if (searchField != null) {
            searchField.setBounds(10, 10, width - 20, 30);
        }
    }

    private void filterContacts(String keyword) {
        listPanel.removeAll();

        for (User user : contacts) {
            if (user.getUsername().contains(keyword)) {
                ContactItem item = new ContactItem(user, this::selectItem);

                item.setOnDeleteCallback(contactItem -> {
                    chatService.removeFriend(currentUserId, contactItem.getUser().getUserId());
                    contacts.remove(contactItem.getUser());
                    updateContacts(contacts);
                });

                item.setOnTopCallback(contactItem -> {
                    boolean isTopped = chatService.isTopped(currentUserId, contactItem.getUser().getUserId());
                    if (isTopped) {
                        chatService.unTopFriend(currentUserId, contactItem.getUser().getUserId());
                    } else {
                        chatService.topFriend(currentUserId, contactItem.getUser().getUserId());
                    }
                    List<User> updatedFriends = chatService.getFriendList(currentUserId);
                    updateContacts(updatedFriends);
                });

                if (selectedItem != null && selectedItem.getUser().equals(user)) {
                    item.setSelected(true);
                }
                listPanel.add(item);
            }
        }

        listPanel.revalidate();
        listPanel.repaint();
    }
}
