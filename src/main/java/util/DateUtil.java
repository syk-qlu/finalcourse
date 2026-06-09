package util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    /**
     * 格式化时间戳
     */
    public static String formatTime(long timestamp) {
        try {
            SimpleDateFormat sdf;
            long now = System.currentTimeMillis();
            long diff = now - timestamp;

            // 一分钟内
            if (diff < 60 * 1000) {
                return "刚刚";
            }
            // 一小时内
            else if (diff < 60 * 60 * 1000) {
                return (diff / (60 * 1000)) + "分钟前";
            }
            // 今天
            else if (diff < 24 * 60 * 60 * 1000) {
                sdf = new SimpleDateFormat("HH:mm");
                return sdf.format(new Date(timestamp));
            }
            // 昨天
            else if (diff < 2 * 24 * 60 * 60 * 1000) {
                sdf = new SimpleDateFormat("昨天 HH:mm");
                return sdf.format(new Date(timestamp));
            }
            // 一周内
            else if (diff < 7 * 24 * 60 * 60 * 1000) {
                sdf = new SimpleDateFormat("EEEE HH:mm");
                return sdf.format(new Date(timestamp));
            }
            // 其他
            else {
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                return sdf.format(new Date(timestamp));
            }
        } catch (Exception e) {
            return "未知时间";
        }
    }

    /**
     * 获取消息列表展示的时间
     */
    public static String formatListTime(long timestamp) {
        try {
            SimpleDateFormat sdf;
            long now = System.currentTimeMillis();
            long diff = now - timestamp;

            // 今天
            if (diff < 24 * 60 * 60 * 1000) {
                sdf = new SimpleDateFormat("HH:mm");
            }
            // 昨天
            else if (diff < 2 * 24 * 60 * 60 * 1000) {
                return "昨天";
            }
            // 一周内
            else if (diff < 7 * 24 * 60 * 60 * 1000) {
                sdf = new SimpleDateFormat("EEEE");
                return sdf.format(new Date(timestamp));
            }
            // 其他
            else {
                sdf = new SimpleDateFormat("MM-dd");
            }
            return sdf.format(new Date(timestamp));
        } catch (Exception e) {
            return "";
        }
    }
}
