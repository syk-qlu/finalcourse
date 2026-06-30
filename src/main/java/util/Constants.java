package util;

import java.awt.*;

public class Constants {
    // 消息类型
    public static final String MESSAGE_TYPE_TEXT = "text";
    public static final String MESSAGE_TYPE_IMAGE = "image";
    public static final String MESSAGE_TYPE_FILE = "file";

    // 用户状态
    public static final String USER_STATUS_ONLINE = "online";
    public static final String USER_STATUS_OFFLINE = "offline";
    public static final String USER_STATUS_AWAY = "away";

    // UI常量
    public static final int AVATAR_SIZE = 40;
    public static final int MESSAGE_MAX_WIDTH = 400;
    public static final int WINDOW_WIDTH = 1050;
    public static final int WINDOW_HEIGHT = 700;

    // 颜色常量 - QQ风格
    public static final Color COLOR_PRIMARY = new Color(18, 183, 245); // QQ蓝
    public static final Color COLOR_NAV_BG = new Color(42, 46, 54);
    public static final Color COLOR_LIST_BG = new Color(245, 245, 245);
    public static final Color COLOR_CHAT_BG = new Color(240, 240, 240);
    public static final Color COLOR_SENDER = new Color(18, 183, 245);
    public static final Color COLOR_RECEIVER = Color.WHITE;
    public static final Color COLOR_TEXT_SENDER = Color.WHITE;
    public static final Color COLOR_TEXT_RECEIVER = Color.BLACK;
}