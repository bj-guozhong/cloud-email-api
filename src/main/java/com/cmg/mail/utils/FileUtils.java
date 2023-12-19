package com.cmg.mail.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.UUID;

import org.apache.poi.util.DefaultTempFileCreationStrategy;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.TempFile;
import org.springframework.web.multipart.MultipartFile;


/**
 *
 * @author jipengfei
 */
public class FileUtils {
    public static final String POI_FILES = "poifiles";
    public static final String EX_CACHE = "excache";
    /**
     * If a server has multiple projects in use at the same time, a directory with the same name will be created under
     * the temporary directory, but each project is run by a different user, so there is a permission problem, so each
     * project creates a unique UUID as a separate Temporary Files.
     */
    private static String tempFilePrefix =
        System.getProperty(TempFile.JAVA_IO_TMPDIR) + File.separator + UUID.randomUUID().toString() + File.separator;
    /**
     * Used to store poi temporary files.
     */
    private static String poiFilesPath = tempFilePrefix + POI_FILES + File.separator;
    /**
     * Used to store easy excel temporary files.
     */
    private static String cachePath = tempFilePrefix + EX_CACHE + File.separator;

    private static final int WRITE_BUFF_SIZE = 8192;

    private FileUtils() {}

    static {
        // Create a temporary directory in advance
        File tempFile = new File(tempFilePrefix);
        createDirectory(tempFile);
        tempFile.deleteOnExit();
        // Initialize the cache directory
        File cacheFile = new File(cachePath);
        createDirectory(cacheFile);
        cacheFile.deleteOnExit();
    }

    /**
     * Reads the contents of a file into a byte array. * The file is always closed.
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static byte[] readFileToByteArray(final File file) throws IOException {
        InputStream in = openInputStream(file);
        try {
            final long fileLength = file.length();
            return fileLength > 0 ? IOUtils.toByteArray(in, (int)fileLength) : IOUtils.toByteArray(in);
        } finally {
            in.close();
        }
    }

    /**
     * Opens a {@link FileInputStream} for the specified file, providing better error messages than simply calling
     * <code>new FileInputStream(file)</code>.
     * <p>
     * At the end of the method either the stream will be successfully opened, or an exception will have been thrown.
     * <p>
     * An exception is thrown if the file does not exist. An exception is thrown if the file object exists but is a
     * directory. An exception is thrown if the file exists but cannot be read.
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static FileInputStream openInputStream(final File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (file.canRead() == false) {
                throw new IOException("File '" + file + "' cannot be read");
            }
        } else {
            throw new FileNotFoundException("File '" + file + "' does not exist");
        }
        return new FileInputStream(file);
    }

    /**
     * Write inputStream to file
     *
     * @param file
     * @param inputStream
     */
    public static void writeToFile(File file, InputStream inputStream) {
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            int bytesRead;
            byte[] buffer = new byte[WRITE_BUFF_SIZE];
            while ((bytesRead = inputStream.read(buffer, 0, WRITE_BUFF_SIZE)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void createPoiFilesDirectory() {
        File poiFilesPathFile = new File(poiFilesPath);
        createDirectory(poiFilesPathFile);
        TempFile.setTempFileCreationStrategy(new DefaultTempFileCreationStrategy(poiFilesPathFile));
        poiFilesPathFile.deleteOnExit();
    }

    public static File createCacheTmpFile() {
        return createDirectory(new File(cachePath + UUID.randomUUID().toString()));
    }

    public static File createTmpFile(String fileName) {
        File directory = createDirectory(new File(tempFilePrefix));
        return new File(directory, fileName);
    }

    /**
     *
     * @param directory
     */
    private static File createDirectory(File directory) {
        if (!directory.exists() && !directory.mkdirs()) {
            //throw new ExcelCommonException("Cannot create directory:" + directory.getAbsolutePath());
        }
        return directory;
    }

    /**
     * delete file
     *
     * @param file
     */
    public static void delete(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }
            for (int i = 0; i < childFiles.length; i++) {
                delete(childFiles[i]);
            }
            file.delete();
        }
    }
    public static void fileChannelCopy(File s, File t) {
        FileInputStream fi = null;
        FileOutputStream fo = null;
        FileChannel in = null;
        FileChannel out = null;
        try {
            fi = new FileInputStream(s);
            fo = new FileOutputStream(t);
            in = fi.getChannel();// 得到对应的文件通道
            out = fo.getChannel();// 得到对应的文件通道
            in.transferTo(0, in.size(), out);// 连接两个通道，并且从in通道读取，然后写入out通道
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fi.close();
                in.close();
                fo.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static File transferToFile(MultipartFile multipartFile) {
        //选择用缓冲区来实现这个转换即使用java 创建的临时文件 使用 MultipartFile.transferto()方法 。
        File file = null;
        try {
            String originalFilename = multipartFile.getOriginalFilename();
            String[] filename = originalFilename.split("\\.");
            if(filename[0]!=null && filename[0]!=""){
                if(filename[0].length()<=2){
                    //System.out.println("==================filename is short,ready add to long");
                    filename[0]=filename[0]+"12345678";
                    //System.out.println("======================filename:"+filename[0]);
                }
            }
            file=File.createTempFile(filename[0], filename[1]);
            multipartFile.transferTo(file);
            file.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
    public static String getTempFilePrefix() {
        return tempFilePrefix;
    }

    public static void setTempFilePrefix(String tempFilePrefix) {
        FileUtils.tempFilePrefix = tempFilePrefix;
    }

    public static String getPoiFilesPath() {
        return poiFilesPath;
    }

    public static void setPoiFilesPath(String poiFilesPath) {
        FileUtils.poiFilesPath = poiFilesPath;
    }

    public static String getCachePath() {
        return cachePath;
    }

    public static void setCachePath(String cachePath) {
        FileUtils.cachePath = cachePath;
    }
}
