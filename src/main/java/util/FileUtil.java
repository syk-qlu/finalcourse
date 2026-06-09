package util;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件工具类 - 处理文件上传、下载等
 */
public class FileUtil {
    private static final String DOWNLOAD_DIR = "files/downloads/";
    private static final String TEMP_DIR = "files/temp/";
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB

    static {
        // 创建目录
        new File(DOWNLOAD_DIR).mkdirs();
        new File(TEMP_DIR).mkdirs();
    }

    /**
     * 读取文件为字节数组
     */
    public static byte[] readFileToBytes(File file) throws IOException {
        if (file.length() > MAX_FILE_SIZE) {
            throw new IOException("文件太大，超过" + (MAX_FILE_SIZE / 1024 / 1024) + "MB限制");
        }
        return Files.readAllBytes(file.toPath());
    }

    /**
     * 保存接收到的文件
     */
    public static String saveReceivedFile(String fileName, byte[] fileData) throws IOException {
        String uniqueName = UUID.randomUUID().toString() + "_" + fileName;
        String filePath = DOWNLOAD_DIR + uniqueName;

        File file = new File(filePath);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(fileData);
            fos.flush();
        }

        return filePath;
    }

    /**
     * 检查文件是否是图片
     */
    public static boolean isImage(String fileName) {
        String lowerName = fileName.toLowerCase();
        return lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") ||
                lowerName.endsWith(".png") || lowerName.endsWith(".gif") ||
                lowerName.endsWith(".bmp") || lowerName.endsWith(".webp");
    }

    /**
     * 格式化文件大小
     */
    public static String formatFileSize(long size) {
        if (size <= 0) return "0B";
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.2f%s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf(".");
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }

    /**
     * 获取下载文件夹路径
     */
    public static String getDownloadDir() {
        return DOWNLOAD_DIR;
    }

    /**
     * 删除临时文件
     */
    public static void deleteTempFile(String filePath) {
        try {
            Files.delete(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 打开文件
     */
    public static void openFile(String filePath) {
        try {
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("win")) {
                Runtime.getRuntime().exec("cmd /c start " + filePath);
            } else if (osName.contains("mac")) {
                Runtime.getRuntime().exec("open " + filePath);
            } else if (osName.contains("linux")) {
                Runtime.getRuntime().exec("xdg-open " + filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取文件下载目录
     */
    public static File getDownloadFolder() {
        File folder = new File(DOWNLOAD_DIR);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }
}
