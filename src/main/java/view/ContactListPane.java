package view;

import model.User;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static javax.swing.BoxLayout.Y_AXIS;

public class ContactListPane extends JPanel {
    JPanel listPanel;
    public ContactListPane(List<User> users ) {

        setLayout(null);
        setBackground(new Color(239,239,239));

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, Y_AXIS));
        listPanel.setBackground(new Color(239,239,239));


        JTextField searchField = new JTextField();
        searchField.setBounds(10,10,190,30);
        //遍历已有联系人
        for(User user : users){
            ContactItem item = new ContactItem(user);
            listPanel.add(item);
        }
        JScrollPane listScroller = new JScrollPane(listPanel);
        //美化
        listScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        listScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JScrollBar verticalBar = listScroller.getVerticalScrollBar();
        verticalBar.setUI(new ScrollBarUI());
        verticalBar.setPreferredSize(new Dimension(12, 0)); // 滚动条宽度
        listScroller.setBorder(null);
        listScroller.setViewportBorder(null);
        listScroller.getViewport().setBorder(null);

        listScroller.setBounds(0,50,250,590);
        listScroller.setBorder(null);

        JScrollBar bar = listScroller.getVerticalScrollBar();
        bar.setUnitIncrement(20);
        SmoothScroll.enable(listScroller);

        add(listScroller);
        add(searchField);
    }
    public void selectItem(ContactItem selectedItem){
        System.out.println("进入选中方法");
        for(Component comp:listPanel.getComponents()){
            if(comp instanceof ContactItem){
                ((ContactItem) comp).setSelected(false);
            }
        }
        selectedItem.setSelected(true);
    }
}
