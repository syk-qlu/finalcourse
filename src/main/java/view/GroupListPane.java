package view;

import model.Group;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

import static javax.swing.BoxLayout.Y_AXIS;

public class GroupListPane extends JPanel {
    private JPanel listPanel;
    private List<Group> groups;
    private Consumer<Group> onSelectCallback;
    private GroupItem selectedItem;

    public GroupListPane(List<Group> groups, Consumer<Group> onSelectCallback) {
        this.groups = groups;
        this.onSelectCallback = onSelectCallback;

        setLayout(null);
        setBackground(new Color(239, 239, 239));

        // 新建群组按钮
        JButton createGroupBtn = new JButton("+ 创建群组");
        createGroupBtn.setBounds(10, 10, 190, 30);
        createGroupBtn.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        createGroupBtn.setBackground(new Color(79, 183, 245));
        createGroupBtn.setForeground(Color.WHITE);
        createGroupBtn.setFocusPainted(false);
        createGroupBtn.setBorder(BorderFactory.createEmptyBorder());
        createGroupBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        add(createGroupBtn);

        // 群组列表面板
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
        listScroller.setBounds(0, 50, 250, 600);
        SmoothScroll.enable(listScroller);
        add(listScroller);

        // 添加群组
        updateGroups(groups);
    }

    public void updateGroups(List<Group> newGroups) {
        this.groups = newGroups;
        listPanel.removeAll();

        for (Group group : groups) {
            GroupItem item = new GroupItem(group, this::selectItem);
            listPanel.add(item);
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    public void selectItem(Group group) {
        // 取消之前的选中状态
        for (Component comp : listPanel.getComponents()) {
            if (comp instanceof GroupItem) {
                GroupItem item = (GroupItem) comp;
                item.setSelected(false);
            }
        }

        // 设置当前选中
        for (Component comp : listPanel.getComponents()) {
            if (comp instanceof GroupItem) {
                GroupItem item = (GroupItem) comp;
                if (item.getGroup().equals(group)) {
                    item.setSelected(true);
                    selectedItem = item;
                    break;
                }
            }
        }

        // 调用回调
        if (onSelectCallback != null) {
            onSelectCallback.accept(group);
        }
    }
}
