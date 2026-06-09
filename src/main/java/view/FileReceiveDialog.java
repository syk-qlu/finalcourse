package view;

import util.FileUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 文件接收对话框
 */
public class FileReceiveDialog extends JDialog {
    private String fileName;
    private String filePath;
    private long fileSize;
    private boolean isImage;
    private Runnable onDownloadCallback;
    private Runnable onOpenCallback;

    public FileReceiveDialog(JFrame parent, String fileName, String filePath, long fileSize) {
        super(parent, "接收文件", true);
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.isImage = FileUtil.isImage(fileName);

        initializeUI();
    }

    private void initializeUI() {
        setSize(400, 500);
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 显示文件预览（如果是图片）
        if (isImage) {
            try {
                BufferedImage image = ImageIO.read(new File(filePath));
                if (image != null) {
                    int maxWidth = 370;
                    int maxHeight = 250;
                    int width = image.getWidth();
                    int height = image.getHeight();

                    if (width > maxWidth || height > maxHeight) {
                        double scale = Math.min((double) maxWidth / width, (double) maxHeight / height);
                        width = (int) (width * scale);
                        height = (int) (height * scale);
                        Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                        Graphics2D g2d = image.createGraphics();
                        g2d.drawImage(scaledImage, 0, 0, null);
                        g2d.dispose();
                    }

                    JLabel imageLabel = new JLabel(new ImageIcon(image));
                    imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    mainPanel.add(imageLabel, BorderLayout.CENTER);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // 显示文件信息
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setBorder(BorderFactory.createTitledBorder("文件信息"));

            JLabel fileNameLabel = new JLabel("文件名: " + fileName);
            JLabel fileSizeLabel = new JLabel("文件大小: " + FileUtil.formatFileSize(fileSize));
            JLabel filePathLabel = new JLabel("保存路径: " + filePath);
            filePathLabel.setFont(new Font("微软雅黑", Font.PLAIN, 10));

            infoPanel.add(fileNameLabel);
            infoPanel.add(Box.createVerticalStrut(5));
            infoPanel.add(fileSizeLabel);
            infoPanel.add(Box.createVerticalStrut(5));
            infoPanel.add(filePathLabel);
            infoPanel.add(Box.createVerticalGlue());

            mainPanel.add(infoPanel, BorderLayout.CENTER);
        }

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        JButton openButton = new JButton("打开文件");
        openButton.addActionListener(e -> {
            FileUtil.openFile(filePath);
            if (onOpenCallback != null) {
                onOpenCallback.run();
            }
            dispose();
        });
        buttonPanel.add(openButton);

        JButton openFolderButton = new JButton("打开文件夹");
        openFolderButton.addActionListener(e -> {
            try {
                File file = new File(filePath);
                File folder = file.getParentFile();
                if (folder != null && folder.exists()) {
                    FileUtil.openFile(folder.getAbsolutePath());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (onDownloadCallback != null) {
                onDownloadCallback.run();
            }
            dispose();
        });
        buttonPanel.add(openFolderButton);

        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    public void setOnDownloadCallback(Runnable callback) {
        this.onDownloadCallback = callback;
    }

    public void setOnOpenCallback(Runnable callback) {
        this.onOpenCallback = callback;
    }
}