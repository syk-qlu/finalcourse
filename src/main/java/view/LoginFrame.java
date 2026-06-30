package view;

import model.User;
import service.UserService;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel messageLabel;
    private UserService userService;

    public LoginFrame() {
        this.userService = new UserService();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("聊天应用 - 登录");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);

        // 标题
        JLabel titleLabel = new JLabel("聊天应用");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setBounds(100, 30, 200, 40);
        panel.add(titleLabel);

        // 用户名标签和输入框
        JLabel usernameLabel = new JLabel("用户名:");
        usernameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        usernameLabel.setBounds(50, 100, 80, 30);
        panel.add(usernameLabel);

        usernameField = new JTextField();
        usernameField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        usernameField.setBounds(150, 100, 200, 30);
        panel.add(usernameField);

        // 密码标签和输入框
        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        passwordLabel.setBounds(50, 150, 80, 30);
        panel.add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        passwordField.setBounds(150, 150, 200, 30);
        panel.add(passwordField);

        // 消息标签
        messageLabel = new JLabel("");
        messageLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        messageLabel.setForeground(Color.RED);
        messageLabel.setBounds(50, 190, 300, 20);
        panel.add(messageLabel);

        // 登录按钮
        loginButton = new JButton("登录");
        loginButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        loginButton.setBounds(100, 220, 80, 30);
        loginButton.setBackground(new Color(79, 183, 245));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder());
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> login());
        panel.add(loginButton);

        // 注册按钮
        registerButton = new JButton("注册");
        registerButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        registerButton.setBounds(220, 220, 80, 30);
        registerButton.setBackground(new Color(100, 200, 100));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setBorder(BorderFactory.createEmptyBorder());
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.addActionListener(e -> register());
        panel.add(registerButton);

        add(panel);
        setVisible(true);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("用户名和密码不能为空");
            return;
        }

        User user = userService.login(username, password);
        if (user != null) {
            messageLabel.setText("");
            new ChatFrame(user);
            dispose();
        } else {
            messageLabel.setText("用户名或密码错误");
            passwordField.setText("");
        }
    }

    private void register() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("用户名和密码不能为空");
            return;
        }

        if (userService.register(username, password, null)) {
            messageLabel.setText("");
            messageLabel.setForeground(new Color(100, 200, 100));
            messageLabel.setText("注册成功，请登录");
            usernameField.setText("");
            passwordField.setText("");
        } else {
            messageLabel.setText("用户名已存在");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame());
    }
}
