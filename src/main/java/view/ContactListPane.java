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
                // 设置预览、未读等（原有代码）
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
                int unread = chatFrame.unreadCountMap.getOrDefault(user.getUserId(), 0);
                item.setUnreadCount(unread);
                // 预览忽略
                listPanel.add(item);
            }
        }
        listPanel.revalidate();
        listPanel.repaint();
    }
}
