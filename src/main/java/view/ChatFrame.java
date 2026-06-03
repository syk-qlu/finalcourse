package view;

import model.My;
import model.User;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ChatFrame extends JFrame {

    User currentChatUser;

    //左侧导航栏
    private JPanel navPane;

    //会话列表
    private JPanel contactPanel;
    private JTextField searchField;
    private JList<String> contactList;

    //聊天区
    private JPanel chatPanel;
    private  JLabel chatTitle;
    private  JScrollPane chatScroller;
    private  JTextArea chatArea;
    //表情图片文件等的工具栏
    private JPanel toolPanel;
    private  JButton sendButton;
    private  JTextArea inputArea;
    private  JScrollPane inputScorll;

    //信息面板
    private  JPanel infoPanel;

    public ChatFrame() {
        List<User> users = new ArrayList<User>();
        users.add(new User("1", "张三", My.getHeadIcon()));
        users.add(new User("2", "李四", My.getHeadIcon()));
        users.add(new User("3", "王五", My.getHeadIcon()));
        users.add(new User("3", "王五", My.getHeadIcon()));
        users.add(new User("3", "王五", My.getHeadIcon()));
        users.add(new User("3", "王五", My.getHeadIcon()));
        users.add(new User("3", "王五", My.getHeadIcon()));
        users.add(new User("3", "王五", My.getHeadIcon()));
        users.add(new User("3", "王五", My.getHeadIcon()));
        users.add(new User("3", "王五", My.getHeadIcon()));
        users.add(new User("3", "王五", My.getHeadIcon()));
        users.add(new User("3", "王五", My.getHeadIcon()));
        users.add(new User("3", "王五", My.getHeadIcon()));
        users.add(new User("3", "王五", My.getHeadIcon()));
        users.add(new User("3", "王五", My.getHeadIcon()));
        users.add(new User("3", "王五", My.getHeadIcon()));

        setTitle("聊天室");
        setSize(1010,675);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        ImageIcon icon = new ImageIcon("src/img/LS20260531222929.png");
        setIconImage(icon.getImage());

        //初始化导航栏
        initLeftNav();
        //初始化会话列表
        ContactListPane contactListPane = new ContactListPane(users);
        contactListPane.setBounds(60,0,250,675);

        //初始化聊天区
        initChatPanel();



        JPanel leftAll = new JPanel(null);
        leftAll.setBounds(0,0,310,675);
        leftAll.add(navPane);
        leftAll.add(contactListPane);

        // 主分割：左边整体 | 聊天区
        JSplitPane mainSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, leftAll, chatPanel
        );
        mainSplit.setDividerSize(0);
        mainSplit.setDividerLocation(310); // 60+250
        mainSplit.setEnabled(false); // 禁止拖动

        setContentPane(mainSplit); // 把分割面板当作窗口内容

    }

    private void initChatPanel() {
        chatPanel = new JPanel(null);
        chatPanel.setBounds(0,0,690,675);
        chatPanel.setBackground(new Color(244,244,244));

        //标题栏
        chatTitle = new JLabel("请选择联系人", SwingConstants.LEADING);
        chatTitle.setBounds(0,0,500,50);
        chatTitle.setFont(new Font("微软雅黑",Font.BOLD,16));
        chatTitle.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        chatPanel.add(chatTitle);

        //聊天内容区
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setBackground(new Color(244,244,244));
        chatScroller = new JScrollPane(chatArea);
        chatScroller.setBounds(0,50,690,400);
        chatScroller.setBorder(new LineBorder(new Color(220,220,220),1));
        chatPanel.add(chatScroller);

        //工具栏
        toolPanel = new JPanel();
        toolPanel.setBounds(0,450,690,30);
        toolPanel.setBackground(new Color(244,244,244));
        toolPanel.add(new JButton("表情"));
        toolPanel.add(new JButton("图片"));
        toolPanel.add(new JButton("文件"));
        chatPanel.add(toolPanel);

        //打字区
        inputArea = new JTextArea();
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setBackground(new Color(244,244,244));
        inputArea.setFont(new Font("微软雅黑",Font.BOLD,14));
        inputScorll = new JScrollPane(inputArea);
        inputScorll.setBounds(0,480,690,120);
        inputScorll.setBorder(new LineBorder(new Color(220,220,220),1));
        chatPanel.add(inputScorll);

        //发送按钮
        sendButton = new JButton("发送");
        sendButton.setBounds(605,600,60,30);
        chatPanel.add(sendButton);

        add(chatPanel);
        add(new ChatBubble("你好",true));
    }


    private void initLeftNav() {
        navPane = new JPanel(null);
        navPane.setBounds(0,0,60,680);
        navPane.setBackground(new Color(29,35,42));
        JLabel headLabel;
        ImageIcon headIcon = My.getHeadIcon();
        headLabel = new JLabel(new ImageIcon(
                My.getHeadIcon().getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH)
        ));
        headLabel.setBounds(6,6,48,48);
        navPane.add(headLabel);
        add(navPane);
    }
    //切换联系对象
    private void switchChat(User user){
        this.currentChatUser = user;

        chatTitle.setText(user.getName());
        //清空聊天记录
        chatArea.setText("");
        /*
        后续扩展
         */
 //       chatArea.append();

    }


}
