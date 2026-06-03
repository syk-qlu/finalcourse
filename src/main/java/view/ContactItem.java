package view;

import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ContactItem extends JPanel {
    private JLabel headLabel;
    private JLabel nameLabel;
    private JLabel latestMessage;
    boolean isClicked = false;
    public ContactItem(User user) {
        //设置联系人名片大小，颜色
        setLayout(null);
        setPreferredSize(new Dimension(250,60));
        setMaximumSize(new Dimension(250,60));
        setMinimumSize(new Dimension(250,60));
        setBackground(new Color(239,239,239));
        setBorder(BorderFactory.createMatteBorder(0,0,1,0,new Color(230,230,230)));
        //设置联系人名片联系人头像
        ImageIcon headIcon = user.getHeadIcon();
        headLabel = new JLabel(new ImageIcon(
                headIcon.getImage().getScaledInstance(48,48,Image.SCALE_SMOOTH)
        ));
        add(headLabel);
        //设置联系人名片联系人名称
        headLabel.setBounds(6,6,48,48);
        nameLabel = new JLabel(user.getName());
        nameLabel.setFont(new Font("微软雅黑",Font.BOLD,14));
        nameLabel.setBounds(60,8,170,20);
        add(nameLabel);
        //设置联系人名片最近消息
        latestMessage = new JLabel("111");
        latestMessage.setBounds(60,30,170,20);
        latestMessage.setFont(new Font("微软雅黑",Font.BOLD,12));
        latestMessage.setForeground(new Color(120,120,120));
        add(latestMessage);

        //添加鼠标监听
        addMouseListener(new MouseAdapter() {
            //点击后改变颜色并锁定
            @Override
            public void mouseClicked(MouseEvent e) {
                //监听鼠标左键
                if(SwingUtilities.isLeftMouseButton(e)){
                    //setBackground(new Color(217,217,217));
                    //isClicked = true;
                    System.out.println("点击条目");

                    System.out.println("调整");
                    Container parent = getParent();
                    while(parent!=null&&!(parent instanceof  ContactListPane)){
                        parent = parent.getParent();
                    }
                    if(parent!=null){
                        ((ContactListPane)parent).selectItem(ContactItem.this);
                    }

                }

                //监听鼠标右键
                if(SwingUtilities.isRightMouseButton(e)){
                    //弹出菜单
                    JPopupMenu menu= new JPopupMenu();
                    menu.add(new JMenuItem("置顶聊天"));
                    menu.add(new JMenuItem("删除聊天"));
                    menu.show(e.getComponent(),e.getX(),e.getY());
                }



            }

            //划入改变背景颜色
            @Override
            public void mouseEntered(MouseEvent e) {
                if(!isClicked){
                    setBackground(new Color(227,227,227));
                }

            }
            //划出时恢复颜色
            @Override
            public void mouseExited(MouseEvent e) {
                if (!isClicked) {
                    setBackground(new Color(239, 239, 239));
                }
            }

        });
    }
    //外部控制选中状态
    public void setSelected(boolean selected) {
        isClicked = selected;
        if(selected){
            setBackground(new Color(217,217,217));
        }else {
            setBackground(new Color(239,239,239));
        }
    }
    public boolean isSelected() {
        return isClicked;
    }

}
