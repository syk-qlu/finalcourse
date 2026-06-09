package util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageUtil {

    /**
     * 创建默认头像
     */
    public static ImageIcon createDefaultAvatar(String username) {
        BufferedImage image = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 根据用户名生成颜色
        int hashCode = username.hashCode();
        int r = (hashCode & 0xFF0000) >> 16;
        int g = (hashCode & 0x00FF00) >> 8;
        int b = (hashCode & 0x0000FF);
        Color color = new Color(Math.abs(r), Math.abs(g), Math.abs(b));

        g2d.setColor(color);
        g2d.fillOval(0, 0, 40, 40);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 18));
        String initial = username.length() > 0 ? username.substring(0, 1) : "?";
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (40 - fm.stringWidth(initial)) / 2;
        int textY = (40 - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(initial, textX, textY);

        g2d.dispose();
        return new ImageIcon(image);
    }

    /**
     * 创建指定大小的头像
     */
    public static ImageIcon createDefaultAvatar(String username, int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int hashCode = username.hashCode();
        int r = (hashCode & 0xFF0000) >> 16;
        int g = (hashCode & 0x00FF00) >> 8;
        int b = (hashCode & 0x0000FF);
        Color color = new Color(Math.abs(r), Math.abs(g), Math.abs(b));

        g2d.setColor(color);
        g2d.fillOval(0, 0, size, size);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("微软雅黑", Font.BOLD, size / 2));
        String initial = username.length() > 0 ? username.substring(0, 1) : "?";
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (size - fm.stringWidth(initial)) / 2;
        int textY = (size - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(initial, textX, textY);

        g2d.dispose();
        return new ImageIcon(image);
    }

    /**
     * 字节数组转ImageIcon
     */
    public static ImageIcon bytesToImageIcon(byte[] imageData) {
        try {
            Image image = ImageIO.read(new java.io.ByteArrayInputStream(imageData));
            if (image != null) {
                return new ImageIcon(image);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * ImageIcon转字节数组
     */
    public static byte[] imageIconToBytes(ImageIcon icon) {
        try {
            BufferedImage bufferedImage = new BufferedImage(
                    icon.getIconWidth(),
                    icon.getIconHeight(),
                    BufferedImage.TYPE_INT_RGB
            );
            Graphics2D g2d = bufferedImage.createGraphics();
            icon.paintIcon(null, g2d, 0, 0);
            g2d.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
