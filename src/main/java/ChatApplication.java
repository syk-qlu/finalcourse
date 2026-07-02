
import view.LoginFrame;

import javax.swing.*;
import java.io.IOException;

public class ChatApplication {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame());
    }
}