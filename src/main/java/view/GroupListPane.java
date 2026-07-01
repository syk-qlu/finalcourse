package view;

import model.Group;
import service.ChatService;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import model.User;

public class GroupListPane extends JPanel {
    private JPanel listPanel;
    private List<Group> groups;
    private Consumer<Group> onSelectCallback;
    private ChatService chatService;
    private int userId;
    private ChatFrame chatFrame;
    private int selectedGroupId = -1;

    public GroupListPane(List<Group> groups, Consumer<Group> callback, ChatService chatService, int userId, ChatFrame frame) {
        this.groups = groups;
        this.onSelectCallback = callback;
        this.chatService = chatService;
        this.userId = userId;
        this.chatFrame = frame;
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));

        // 顶部按钮
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
        topPanel.setBackground(new Color(245, 245, 245));
        JButton createBtn = new JButton("+ 创建群聊");
        createBtn.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        createBtn.setBackground(new Color(18, 183, 245));
        createBtn.setForeground(Color.WHITE);
        createBtn.setFocusPainted(false);
        createBtn.addActionListener(e -> showCreateGroupDialog());
        topPanel.add(createBtn);
        add(topPanel, BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(new Color(245, 245, 245));

        JScrollPane scroller = new JScrollPane(listPanel);
        scroller.setBorder(null);
        add(scroller, BorderLayout.CENTER);

        updateGroups(groups);
    }

    public void updateGroups(List<Group> gs) {
        this.groups = gs;
        listPanel.removeAll();
        for (Group g : groups) {
            GroupItem item = new GroupItem(g, this::selectGroup);
            if (g.getGroupId() == selectedGroupId) {
                item.setSelected(true);
            }

            // ---- 右键菜单 ----
            item.setOnRightClick(e -> {
                JPopupMenu popup = new JPopupMenu();

                // 查看群成员
                JMenuItem membersItem = new JMenuItem("查看群成员");
                membersItem.addActionListener(ev -> {
                    List<User> members = chatService.getGroupMembers(g.getGroupId());
                    StringBuilder sb = new StringBuilder();
                    sb.append("群名称: ").append(g.getGroupName()).append("\n");
                    sb.append("成员数量: ").append(members.size()).append("\n\n");
                    for (User u : members) {
                        sb.append("· ").append(u.getUsername())
                                .append(" (ID: ").append(u.getUserId()).append(")\n");
                    }
                    JTextArea textArea = new JTextArea(sb.toString());
                    textArea.setFont(new Font("微软雅黑", Font.PLAIN, 13));
                    textArea.setEditable(false);
                    JScrollPane scroll = new JScrollPane(textArea);
                    scroll.setPreferredSize(new Dimension(280, 220));
                    JOptionPane.showMessageDialog(chatFrame, scroll,
                            "群成员列表", JOptionPane.INFORMATION_MESSAGE);
                });
                popup.add(membersItem);

                // 查看群 ID
                JMenuItem idItem = new JMenuItem("群ID: " + g.getGroupId());
                idItem.addActionListener(ev -> {
                    StringSelection selection = new StringSelection(String.valueOf(g.getGroupId()));
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
                    JOptionPane.showMessageDialog(chatFrame, "群ID已复制到剪贴板");
                });
                popup.add(idItem);

                popup.show(item, e.getX(), e.getY());
            });

            listPanel.add(item);
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    public void selectGroup(Group g) {
        selectedGroupId = g.getGroupId();
        // 取消所有选中
        for (Component c : listPanel.getComponents()) {
            if (c instanceof GroupItem) {
                ((GroupItem) c).setSelected(false);
            }
        }
        // 设置当前选中
        for (Component c : listPanel.getComponents()) {
            if (c instanceof GroupItem) {
                GroupItem item = (GroupItem) c;
                if (item.getGroup().getGroupId() == selectedGroupId) {
                    item.setSelected(true);
                    break;
                }
            }
        }
        if (onSelectCallback != null) onSelectCallback.accept(g);
    }

    private void showCreateGroupDialog() {
        String name = JOptionPane.showInputDialog(chatFrame, "群聊名称：");
        if (name == null || name.trim().isEmpty()) return;
        int gid = chatService.createGroup(name.trim(), userId, "");
        if (gid > 0) {
            JOptionPane.showMessageDialog(chatFrame, "群聊创建成功");
            chatFrame.loadData();
        } else {
            JOptionPane.showMessageDialog(chatFrame, "创建失败");
        }
    }
}
