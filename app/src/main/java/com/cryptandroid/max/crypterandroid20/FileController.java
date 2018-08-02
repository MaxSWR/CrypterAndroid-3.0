package com.cryptandroid.max.crypterandroid20;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.nio.ByteBuffer;

/**
 * Created by MAX on 30.05.2018.
 * Класс для работы с файлами
 */

public class FileController {
    private static File[] files = null;

    public static File[] getFiles() {
        return files;

    }

    public static void setFiles(File[] files) {
        FileController.files = files;
    }

    /**
     * Получить папку для работы
     * @param src - папка родитель
     * @return - папка для работы
     */
    public static File getNewActionFolder(String src, String action) {
        File [] items = new File(src).listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                }

                return false;
            }
        });

        int count = 0;

        if (items != null) {
            count = items.length + 1;
        }

        String folderName = src + "/" + action + count;
        return new File(folderName);
    }

    /**
     * Получить уникальный файл на основе оригинального имени
     * @param file - оригинальный файл
     * @return новое имя
     */
    public static String getUniqueFileName (File file) {
        String name = getNameFile(file.getAbsolutePath());
        String ext = getExtensionFile(file.getAbsolutePath());
        return name + "1." + ext;
    }

    /**
     * Получить расширение файла
     * @param fileName - полное имя файла
     * @return - расширение
     */
    public static String getExtensionFile(String fileName) {
        int i = fileName.lastIndexOf(".");
        return (i > 0 && i < fileName.length()) ? fileName.substring(i+1) : null;
    }

    /**
     * Получить имя файла без расширения
     * @param fileName - полное имя файла
     * @return - имя
     */
    public static String getNameFile(String fileName) {
        int i = fileName.lastIndexOf(".");
        return i > 0  ? fileName.substring(0, i) : null;
    }

    /**
     * Вернуть long из массива
     * @param b - массив байт
     * @return число
     */
    public static long byteArrayToLong(byte[] b) {
        return ((b[0]&0xff) << 56) | ((b[1]&0xff) << 48) |
                ((b[2]&0xff) << 40) | ((b[3]&0xff) << 32) |
                ((b[4]&0xff) << 24) | ((b[5]&0xff) << 16) |
                ((b[6]&0xff) << 8) | (b[7]&0xff );
    }

    /**
     * Вернуть массив байт из long
     * @param b - число
     * @return массив байт
     */
    public static byte[] LongToByteArray(long b) {
        return ByteBuffer.allocate(8).putLong(b).array();
    }

}
