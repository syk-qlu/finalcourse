package view;

import model.Group;
import service.ChatService;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class GroupListPane extends JPanel {
    private JPanel listPanel;
    private List<Group> groups;
    private Consumer<Group> onSelectCallback;
    private ChatService chatService;
    private int userId;
    private ChatFrame chatFrame;

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
            listPanel.add(item);
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private void selectGroup(Group g) {
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
