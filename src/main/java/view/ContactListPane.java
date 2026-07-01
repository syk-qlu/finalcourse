package view;
import model.User;
import service.ChatService;
import util.Constants;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
public class ContactListPane extends JPanel {
    private JPanel listPanel;
    private JTextField searchField;
    private List<User> contacts;
    private Consumer<User> onSelectCallback;
    private ChatService chatService;
    private int currentUserId;
    private ChatFrame chatFrame;
    private JLabel emptyLabel;
    private int selectedUserId = -1;
    public ContactListPane(List<User> contacts, Consumer<User> callback,
                           ChatService chatService, int userId, ChatFrame frame) {
        this.contacts = contacts;
        this.onSelectCallback = callback;
        this.chatService = chatService;
        this.currentUserId = userId;
        this.chatFrame = frame;
        setLayout(new BorderLayout());
        setBackground(Constants.COLOR_LIST_BG);
        // 搜索框（外部传入或内部创建，这里假设外部已移除，我们自行创建）
        searchField = new JTextField();
        searchField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 10, 5, 10),
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true)
        ));
        searchField.setBackground(Color.WHITE);
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(Constants.COLOR_LIST_BG);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        searchPanel.add(searchField, BorderLayout.CENTER);
        add(searchPanel, BorderLayout.NORTH);
        // 列表
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Constants.COLOR_LIST_BG);
        JScrollPane listScroller = new JScrollPane(listPanel);
        listScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        listScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        listScroller.getVerticalScrollBar().setUI(new ScrollBarUI());
        listScroller.setBorder(null);
        add(listScroller, BorderLayout.CENTER);
        // 空状态提示
        emptyLabel = new JLabel("暂无好友", SwingConstants.CENTER);
        emptyLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        emptyLabel.setForeground(Color.GRAY);
        add(emptyLabel, BorderLayout.SOUTH); // 临时放底部，实际逻辑中会切换显隐
        emptyLabel.setVisible(false);
        updateContacts(contacts);
    }
    public void updateContacts(List<User> newContacts) {
        this.contacts = newContacts;
        listPanel.removeAll();
        if (contacts.isEmpty()) {
            emptyLabel.setVisible(true);
        } else {
            emptyLabel.setVisible(false);
            for (User user : contacts) {
                ContactItem item = new ContactItem(user, this::selectItem);
                // 恢复之前选中的用户
                if (user.getUserId() == selectedUserId) {
                    item.setSelected(true);
                }
                // ★ 设置最后一条消息预览
                String preview = chatService.getLastMessagePreview(currentUserId, user.getUserId());
                item.setLastMessage(preview != null ? preview : "");
                // ★ 同步未读计数
                int unread = chatFrame.unreadCountMap.getOrDefault(user.getUserId(), 0);
                item.setUnreadCount(unread);
                // ★ 设置删除好友回调
                item.setOnDeleteCallback(contactItem -> {
                    int result = JOptionPane.showConfirmDialog(this,
                            "确定要删除好友 " + contactItem.getUser().getUsername() + " 吗？",
                            "删除好友", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        boolean success = chatService.removeFriend(currentUserId,
                                contactItem.getUser().getUserId());
                        if (success) {
                            contacts.remove(contactItem.getUser());
                            updateContacts(contacts);
                            JOptionPane.showMessageDialog(this, "好友已删除");
                        } else {
                            JOptionPane.showMessageDialog(this, "删除好友失败");
                        }
                    }
                });
                // ★ 设置置顶回调
                item.setOnTopCallback(contactItem -> {
                    boolean isTopped = chatService.isTopped(currentUserId,
                            contactItem.getUser().getUserId());
                    boolean success;
                    if (isTopped) {
                        success = chatService.unTopFriend(currentUserId,
                                contactItem.getUser().getUserId());
                        if (success) {
                            JOptionPane.showMessageDialog(this, "已取消置顶");
                        }
                    } else {
                        success = chatService.topFriend(currentUserId,
                                contactItem.getUser().getUserId());
                        if (success) {
                            JOptionPane.showMessageDialog(this, "已置顶");
                        }
                    }
                    if (success) {
                        // 重新加载好友列表（按置顶排序）
                        List<User> updatedFriends = chatService.getFriendList(currentUserId);
                        updateContacts(updatedFriends);
                    }
                });
                listPanel.add(item);
            }
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    public void updateUnreadCount(int userId, int count) {
        for (Component c : listPanel.getComponents()) {
            if (c instanceof ContactItem) {
                ContactItem item = (ContactItem) c;
                if (item.getUser().getUserId() == userId) {
                    item.setUnreadCount(count);
                    break;
                }
            }
        }
    }

    public void selectItem(User user) {
        selectedUserId = user.getUserId();
        // 取消所有选中
        for (Component comp : listPanel.getComponents()) {
            if (comp instanceof ContactItem) {
                ((ContactItem) comp).setSelected(false);
            }
        }
        // 设置当前选中
        for (Component comp : listPanel.getComponents()) {
            if (comp instanceof ContactItem) {
                ContactItem item = (ContactItem) comp;
                if (item.getUser().getUserId() == selectedUserId) {
                    item.setSelected(true);
                    break;
                }
            }
        }
        if (onSelectCallback != null) onSelectCallback.accept(user);
    }

    public void filterContacts(String keyword) {
        listPanel.removeAll();
        for (User user : contacts) {
            if (keyword.isEmpty() || user.getUsername().contains(keyword)) {
                ContactItem item = new ContactItem(user, this::selectItem);
                // 恢复选中状态
                if (user.getUserId() == selectedUserId) {
                    item.setSelected(true);
                }
                // ★ 同步未读计数
                int unread = chatFrame.unreadCountMap.getOrDefault(user.getUserId(), 0);
                item.setUnreadCount(unread);
                // ★ 设置删除好友回调
                item.setOnDeleteCallback(contactItem -> {
                    int result = JOptionPane.showConfirmDialog(this,
                            "确定要删除好友 " + contactItem.getUser().getUsername() + " 吗？",
                            "删除好友", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        boolean success = chatService.removeFriend(currentUserId,
                                contactItem.getUser().getUserId());
                        if (success) {
                            contacts.remove(contactItem.getUser());
                            updateContacts(contacts);
                            JOptionPane.showMessageDialog(this, "好友已删除");
                        } else {
                            JOptionPane.showMessageDialog(this, "删除好友失败");
                        }
                    }
                });
                // ★ 设置置顶回调
                item.setOnTopCallback(contactItem -> {
                    boolean isTopped = chatService.isTopped(currentUserId,
                            contactItem.getUser().getUserId());
                    boolean success;
                    if (isTopped) {
                        success = chatService.unTopFriend(currentUserId,
                                contactItem.getUser().getUserId());
                        if (success) {
                            JOptionPane.showMessageDialog(this, "已取消置顶");
                        }
                    } else {
                        success = chatService.topFriend(currentUserId,
                                contactItem.getUser().getUserId());
                        if (success) {
                            JOptionPane.showMessageDialog(this, "已置顶");
                        }
                    }
                    if (success) {
                        List<User> updatedFriends = chatService.getFriendList(currentUserId);
                        updateContacts(updatedFriends);
                    }
                });
                listPanel.add(item);
            }
        }
        listPanel.revalidate();
        listPanel.repaint();
    }
}
