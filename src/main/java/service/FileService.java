package service;

import util.FileUtil;

import java.io.File;
import java.io.IOException;

/**
 * 文件服务
 */
public class FileService {

    /**
     * 上传文件
     */
    public static byte[] uploadFile(File file) throws IOException {
        if (file.length() > 100 * 1024 * 1024) {
            throw new IOException("文件过大");
        }
        return FileUtil.readFileToBytes(file);
    }

    /**
     * 保存接收的文件
     */
    public static String saveFile(String fileName, byte[] fileData) throws IOException {
        return FileUtil.saveReceivedFile(fileName, fileData);
    }

    /**
     * 检查是否为图片
     */
    public static boolean isImageFile(String fileName) {
        return FileUtil.isImage(fileName);
    }

    /**
     * 获取文件大小字符串
     */
    public static String getFileSizeString(long size) {
        return FileUtil.formatFileSize(size);
    }

    /**
     * 打开文件
     */
    public static void openFile(String filePath) {
        FileUtil.openFile(filePath);
    }

    /**
     * 获取下载目录
     */
    public static File getDownloadDir() {
        return FileUtil.getDownloadFolder();
    }
}