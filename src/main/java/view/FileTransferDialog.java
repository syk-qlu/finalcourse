package view;

import util.FileUtil;

import javax.swing.*;
import java.io.File;

/**
 * 文件转移对话框
 */
public class FileTransferDialog {

    /**
     * 选择要发送的文件
     */
    public static File selectFileToSend(JFrame parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择要发送的文件");
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int result = fileChooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile.length() > 100 * 1024 * 1024) {
                JOptionPane.showMessageDialog(parent, "文件过大，最大支持100MB");
                return null;
            }
            return selectedFile;
        }
        return null;
    }

    /**
     * 选择要发送的图片
     */
    public static File selectImageToSend(JFrame parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择要发送的图片");
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // 添加图片文件过滤
        javax.swing.filechooser.FileNameExtensionFilter imageFilter =
                new javax.swing.filechooser.FileNameExtensionFilter(
                        "图片文件 (*.jpg;*.jpeg;*.png;*.gif;*.bmp;*.webp)",
                        "jpg", "jpeg", "png", "gif", "bmp", "webp");
        fileChooser.setFileFilter(imageFilter);

        int result = fileChooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (!FileUtil.isImage(selectedFile.getName())) {
                JOptionPane.showMessageDialog(parent, "请选择图片文件");
                return null;
            }
            if (selectedFile.length() > 50 * 1024 * 1024) {
                JOptionPane.showMessageDialog(parent, "图片过大，最大支持50MB");
                return null;
            }
            return selectedFile;
        }
        return null;
    }

    /**
     * 选择保存文件的位置
     */
    public static File selectSaveLocation(JFrame parent, String fileName) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("保存文件");
        fileChooser.setSelectedFile(new File(fileName));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int result = fileChooser.showSaveDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }
}