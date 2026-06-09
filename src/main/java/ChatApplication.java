
import view.LoginFrame;

import javax.swing.*;

public class ChatApplication {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame());
    }
}