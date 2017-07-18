package com.gogtz.common.file;

import com.alibaba.fastjson.JSONObject;
import com.gogtz.common.collection.CollectionUtil;
import com.gogtz.common.http.URLUtil;
import com.gogtz.common.lang.Assert;
import com.gogtz.common.lang.ClassUtil;
import com.gogtz.common.resource.ResourceUtil;
import com.gogtz.common.string.CharPool;
import com.gogtz.common.string.StringPool;
import com.sun.xml.internal.ws.util.UtilException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 文件工具类
 *
 * @author t
 */
@Slf4j
public class FileUtils {

    /**
     * The Unix separator character.
     */
    private static final char UNIX_SEPARATOR = '/';
    /**
     * The Windows separator character.
     */
    private static final char WINDOWS_SEPARATOR = '\\';

    /**
     * Class文件扩展名
     */
    public static final String CLASS_EXT = ".class";
    /**
     * Jar文件扩展名
     */
    public static final String JAR_FILE_EXT = ".jar";
    /**
     * 在Jar中的路径jar的扩展名形式
     */
    public static final String JAR_PATH_EXT = ".jar!";
    /**
     * 当Path为文件形式时, path会加入一个表示文件的前缀
     */
    public static final String PATH_FILE_PRE = "file:";

    /**
     * 列出目录文件<br>
     * <p>
     * 给定的绝对路径不能是压缩包中的路径
     *
     * @param path 目录绝对路径或者相对路径
     * @return 文件列表（包含目录）
     */
    public static File[] ls(String path) {
        if (path == null) {
            return null;
        }
        path = getAbsolutePath(path);

        File file = file(path);
        if (file.isDirectory()) {
            return file.listFiles();
        }
        throw new IORuntimeException(String.format("Path [{}] is not directory!", path));
    }

    /**
     * 文件是否为空<br>
     * <p>
     * 目录：里面没有文件时为空 文件：文件大小为0时为空
     *
     * @param file 文件
     * @return 是否为空，当提供非目录时，返回false
     */
    public static boolean isEmpty(File file) {
        if (null == file) {
            return true;
        }

        if (file.isDirectory()) {
            String[] subFiles = file.list();
            if (ArrayUtils.isEmpty(subFiles)) {
                return true;
            }
        } else if (file.isFile()) {
            return file.length() <= 0;
        }

        return false;
    }

    /**
     * 目录是否为空
     *
     * @param file 目录
     * @return 是否为空，当提供非目录时，返回false
     */
    public static boolean isNotEmpty(File file) {
        return false == isEmpty(file);
    }

    /**
     * 目录是否为空
     *
     * @param dirPath 目录
     * @return 是否为空
     * @throws IORuntimeException IOException
     */
    public static boolean isDirEmpty(Path dirPath) {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dirPath)) {
            return false == dirStream.iterator().hasNext();
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

    /**
     * 目录是否为空
     *
     * @param dir 目录
     * @return 是否为空
     */
    public static boolean isDirEmpty(File dir) {
        return isDirEmpty(dir.toPath());
    }

    /**
     * 递归遍历目录以及子目录中的所有文件<br>
     * <p>
     * 如果提供file为文件，直接返回过滤结果
     *
     * @param file       当前遍历文件或目录
     * @param fileFilter 文件过滤规则对象，选择要保留的文件，只对文件有效，不过滤目录
     * @return 文件列表
     */
    public static List<File> loopFiles(File file, FileFilter fileFilter) {
        List<File> fileList = new ArrayList<File>();
        if (file == null) {
            return fileList;
        } else if (file.exists() == false) {
            return fileList;
        }

        if (file.isDirectory()) {
            for (File tmp : file.listFiles()) {
                fileList.addAll(loopFiles(tmp, fileFilter));
            }
        } else {
            if (null == fileFilter || fileFilter.accept(file)) {
                fileList.add(file);
            }
        }

        return fileList;
    }

    /**
     * 递归遍历目录以及子目录中的所有文件
     *
     * @param file 当前遍历文件
     * @return 文件列表
     */
    public static List<File> loopFiles(File file) {
        return loopFiles(file, null);
    }

    /**
     * 获得指定目录下所有文件<br>
     * <p>
     * 不会扫描子目录
     *
     * @param path 相对ClassPath的目录或者绝对路径目录
     * @return 文件路径列表（如果是jar中的文件，则给定类似.jar!/xxx/xxx的路径）
     * @throws IORuntimeException IO异常
     */
    public static List<String> listFileNames(String path) throws IORuntimeException {
        if (path == null) {
            return null;
        }
        path = getAbsolutePath(path);
        if (false == path.endsWith(String.valueOf(UNIX_SEPARATOR))) {
            path = path + UNIX_SEPARATOR;
        }

        List<String> paths = new ArrayList<String>();
        int index = path.lastIndexOf(FileUtils.JAR_PATH_EXT);
        if (index == -1) {
            // 普通目录路径

            File[] files = ls(path);
            for (File file : files) {
                if (file.isFile()) {
                    paths.add(file.getName());
                }
            }
        } else {
            // jar文件中的路径
            index = index + FileUtils.JAR_FILE_EXT.length();
            JarFile jarFile = null;
            try {
                jarFile = new JarFile(path.substring(0, index));
                final String subPath = path.substring(index + 2);
                for (JarEntry entry : Collections.list(jarFile.entries())) {
                    final String name = entry.getName();
                    if (name.startsWith(subPath)) {
                        final String nameSuffix = removePrefix(name, subPath);
                        if (nameSuffix.contains(String.valueOf(UNIX_SEPARATOR)) == false) {
                            paths.add(nameSuffix);
                        }
                    }
                }
            } catch (IOException e) {
                throw new IORuntimeException(String.format("Can not read file path of [{}]", path), e);
            } finally {
                IoUtil.close(jarFile);
            }
        }
        return paths;
    }

    /**
     * 创建File对象，自动识别相对或绝对路径，相对路径将自动从ClassPath下寻找
     *
     * @param path 文件路径
     * @return File
     */
    public static File file(String path) {
        if (StringUtils.isBlank(path)) {
            throw new NullPointerException("File path is blank!");
        }
        return new File(getAbsolutePath(path));
    }

    /**
     * 创建File对象
     *
     * @param parent 父目录
     * @param path   文件路径
     * @return File
     */
    public static File file(String parent, String path) {
        if (StringUtils.isBlank(path)) {
            throw new NullPointerException("File path is blank!");
        }
        return new File(parent, path);
    }

    /**
     * 创建File对象
     *
     * @param parent 父文件对象
     * @param path   文件路径
     * @return File
     */
    public static File file(File parent, String path) {
        if (StringUtils.isBlank(path)) {
            throw new NullPointerException("File path is blank!");
        }
        return new File(parent, path);
    }

    /**
     * 创建File对象
     *
     * @param uri 文件URI
     * @return File
     */
    public static File file(URI uri) {
        if (uri == null) {
            throw new NullPointerException("File uri is null!");
        }
        return new File(uri);
    }

    /**
     * 创建File对象
     *
     * @param url 文件URL
     * @return File
     */
    public static File file(URL url) {
        return new File(URLUtil.toURI(url));
    }

    /**
     * 判断文件是否存在，如果path为null，则返回false
     *
     * @param path 文件路径
     * @return 如果存在返回true
     */
    public static boolean exist(String path) {
        return (path == null) ? false : file(path).exists();
    }

    /**
     * 判断文件是否存在，如果file为null，则返回false
     *
     * @param file 文件
     * @return 如果存在返回true
     */
    public static boolean exist(File file) {
        return (file == null) ? false : file.exists();
    }

    /**
     * 是否存在匹配文件
     *
     * @param directory 文件夹路径
     * @param regexp    文件夹中所包含文件名的正则表达式
     * @return 如果存在匹配文件返回true
     */
    public static boolean exist(String directory, String regexp) {
        File file = new File(directory);
        if (!file.exists()) {
            return false;
        }

        String[] fileList = file.list();
        if (fileList == null) {
            return false;
        }

        for (String fileName : fileList) {
            if (fileName.matches(regexp)) {
                return true;
            }

        }
        return false;
    }

    /**
     * 指定文件最后修改时间
     *
     * @param file 文件
     * @return 最后修改时间
     */
    public static Date lastModifiedTime(File file) {
        if (!exist(file)) {
            return null;
        }

        return new Date(file.lastModified());
    }

    /**
     * 指定路径文件最后修改时间
     *
     * @param path 绝对路径
     * @return 最后修改时间
     */
    public static Date lastModifiedTime(String path) {
        return lastModifiedTime(new File(path));
    }

    /**
     * 计算目录或文件的总大小<br>
     * <p>
     * 当给定对象为文件时，直接调用 {@link File#length()}<br>
     * <p>
     * 当给定对象为目录时，遍历目录下的所有文件和目录，递归计算其大小，求和返回
     *
     * @param file 目录或文件
     * @return 总大小
     */
    public static long size(File file) {
        if (file == null) {
            throw new UtilException("file argument is null !");
        }
        if (false == file.exists()) {
            throw new IllegalArgumentException(String.format("File [{}] not exist !", file.getAbsolutePath()));
        }

        if (file.isDirectory()) {
            long size = 0L;
            File[] subFiles = file.listFiles();
            if (ArrayUtils.isEmpty(subFiles)) {
                return 0L;// empty directory

            }
            for (int i = 0; i < subFiles.length; i++) {
                size += size(subFiles[i]);
            }
            return size;
        } else {
            return file.length();
        }
    }

    /**
     * 给定文件或目录的最后修改时间是否晚于给定时间
     *
     * @param file      文件或目录
     * @param reference 参照文件
     * @return 是否晚于给定时间
     */
    public static boolean newerThan(File file, File reference) {
        if (null == file || false == reference.exists()) {
            return true;// 文件一定比一个不存在的文件新

        }
        return newerThan(file, reference.lastModified());
    }

    /**
     * 给定文件或目录的最后修改时间是否晚于给定时间
     *
     * @param file       文件或目录
     * @param timeMillis 做为对比的时间
     * @return 是否晚于给定时间
     */
    public static boolean newerThan(File file, long timeMillis) {
        if (null == file || false == file.exists()) {
            return false;// 不存在的文件一定比任何时间旧

        }
        return file.lastModified() > timeMillis;
    }

    /**
     * 创建文件及其父目录，如果这个文件存在，直接返回这个文件<br>
     * <p>
     * 此方法不对File对象类型做判断，如果File不存在，无法判断其类型
     *
     * @param fullFilePath 文件的全路径，使用POSIX风格
     * @return 文件，若路径为null，返回null
     * @throws IORuntimeException IO异常
     */
    public static File touch(String fullFilePath) throws IORuntimeException {
        if (fullFilePath == null) {
            return null;
        }
        return touch(file(fullFilePath));
    }

    /**
     * 创建文件及其父目录，如果这个文件存在，直接返回这个文件<br>
     * <p>
     * 此方法不对File对象类型做判断，如果File不存在，无法判断其类型
     *
     * @param file 文件对象
     * @return 文件，若路径为null，返回null
     * @throws IORuntimeException IO异常
     */
    public static File touch(File file) throws IORuntimeException {
        if (null == file) {
            return null;
        }
        if (false == file.exists()) {
            mkParentDirs(file);
            try {
                file.createNewFile();
            } catch (Exception e) {
                throw new IORuntimeException(e);
            }
        }
        return file;
    }

    /**
     * 创建文件及其父目录，如果这个文件存在，直接返回这个文件<br>
     * <p>
     * 此方法不对File对象类型做判断，如果File不存在，无法判断其类型
     *
     * @param parent 父文件对象
     * @param path   文件路径
     * @return File
     * @throws IORuntimeException IO异常
     */
    public static File touch(File parent, String path) throws IORuntimeException {
        return touch(file(parent, path));
    }

    /**
     * 创建文件及其父目录，如果这个文件存在，直接返回这个文件<br>
     * <p>
     * 此方法不对File对象类型做判断，如果File不存在，无法判断其类型
     *
     * @param parent 父文件对象
     * @param path   文件路径
     * @return File
     * @throws IORuntimeException IO异常
     */
    public static File touch(String parent, String path) throws IORuntimeException {
        return touch(file(parent, path));
    }

    /**
     * 创建所给文件或目录的父目录
     *
     * @param file 文件或目录
     * @return 父目录
     */
    public static File mkParentDirs(File file) {
        final File parentFile = file.getParentFile();
        if (null != parentFile && false == parentFile.exists()) {
            parentFile.mkdirs();
        }
        return parentFile;
    }

    /**
     * 创建父文件夹，如果存在直接返回此文件夹
     *
     * @param path 文件夹路径，使用POSIX格式，无论哪个平台
     * @return 创建的目录
     */
    public static File mkParentDirs(String path) {
        if (path == null) {
            return null;
        }
        return mkParentDirs(file(path));
    }

    /**
     * 删除文件或者文件夹<br>
     * <p>
     * 路径如果为相对路径，会转换为ClassPath路径！ 注意：删除文件夹时不会判断文件夹是否为空，如果不空则递归删除子文件或文件夹<br>
     * <p>
     * 某个文件删除失败会终止删除操作
     *
     * @param fullFileOrDirPath 文件或者目录的路径
     * @return 成功与否
     * @throws IORuntimeException IO异常
     */
    public static boolean del(String fullFileOrDirPath) throws IORuntimeException {
        return del(file(fullFileOrDirPath));
    }

    /**
     * 删除文件或者文件夹<br>
     * <p>
     * 注意：删除文件夹时不会判断文件夹是否为空，如果不空则递归删除子文件或文件夹<br>
     * <p>
     * 某个文件删除失败会终止删除操作
     *
     * @param file 文件对象
     * @return 成功与否
     * @throws IORuntimeException IO异常
     */
    public static boolean del(File file) throws IORuntimeException {
        if (file == null || file.exists() == false) {
            return true;
        }

        if (file.isDirectory()) {
            clean(file);
        }
        return file.delete();
    }

    /**
     * 清空文件夹<br>
     * <p>
     * 注意：清空文件夹时不会判断文件夹是否为空，如果不空则递归删除子文件或文件夹<br>
     * <p>
     * 某个文件删除失败会终止删除操作
     *
     * @param directory 文件夹
     * @return 成功与否
     * @throws IORuntimeException IO异常
     * @since 3.0.6
     */
    public static boolean clean(File directory) throws IORuntimeException {
        if (directory == null || directory.exists() == false || false == directory.isDirectory()) {
            return true;
        }

        final File[] files = directory.listFiles();
        for (File childFile : files) {
            boolean isOk = del(childFile);
            if (isOk == false) {
                // 删除一个出错则本次删除任务失败

                return false;
            }
        }
        return true;
    }

    /**
     * 创建文件夹，如果存在直接返回此文件夹<br>
     * <p>
     * 此方法不对File对象类型做判断，如果File不存在，无法判断其类型
     *
     * @param dirPath 文件夹路径，使用POSIX格式，无论哪个平台
     * @return 创建的目录
     */
    public static File mkdir(String dirPath) {
        if (dirPath == null) {
            return null;
        }
        final File dir = file(dirPath);
        return mkdir(dir);
    }

    /**
     * 创建文件夹，会递归自动创建其不存在的父文件夹，如果存在直接返回此文件夹<br>
     * <p>
     * 此方法不对File对象类型做判断，如果File不存在，无法判断其类型
     *
     * @param dir 目录
     * @return 创建的目录
     */
    public static File mkdir(File dir) {
        if (dir == null) {
            return null;
        }
        if (false == dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * 创建临时文件<br>
     * <p>
     * 创建后的文件名为 prefix[Randon].tmp
     *
     * @param dir 临时文件创建的所在目录
     * @return 临时文件
     * @throws IORuntimeException IO异常
     */
    public static File createTempFile(File dir) throws IORuntimeException {
        return createTempFile("hutool", null, dir, true);
    }

    /**
     * 创建临时文件<br>
     * <p>
     * 创建后的文件名为 prefix[Randon].tmp
     *
     * @param dir       临时文件创建的所在目录
     * @param isReCreat 是否重新创建文件（删掉原来的，创建新的）
     * @return 临时文件
     * @throws IORuntimeException IO异常
     */
    public static File createTempFile(File dir, boolean isReCreat) throws IORuntimeException {
        return createTempFile("hutool", null, dir, isReCreat);
    }

    /**
     * 创建临时文件<br>
     * <p>
     * 创建后的文件名为 prefix[Randon].suffix From com.jodd.io.FileUtil
     *
     * @param prefix    前缀，至少3个字符
     * @param suffix    后缀，如果null则使用默认.tmp
     * @param dir       临时文件创建的所在目录
     * @param isReCreat 是否重新创建文件（删掉原来的，创建新的）
     * @return 临时文件
     * @throws IORuntimeException IO异常
     */
    public static File createTempFile(String prefix, String suffix, File dir, boolean isReCreat) throws IORuntimeException {
        int exceptionsCount = 0;
        while (true) {
            try {
                File file = File.createTempFile(prefix, suffix, dir).getCanonicalFile();
                if (isReCreat) {
                    file.delete();
                    file.createNewFile();
                }
                return file;
            } catch (IOException ioex) { // fixes java.io.WinNTFileSystem.createFileExclusively access denied

                if (++exceptionsCount >= 50) {
                    throw new IORuntimeException(ioex);
                }
            }
        }
    }

    /**
     * 通过JDK7+的 {@link Files#copy(Path, Path, CopyOption...)} 方法拷贝文件
     *
     * @param src     源文件路径
     * @param dest    目标文件或目录路径，如果为目录使用与源文件相同的文件名
     * @param options {@link StandardCopyOption}
     * @return File
     * @throws IORuntimeException IO异常
     */
    public static File copyFile(String src, String dest, StandardCopyOption... options) throws IORuntimeException {
        Assert.notBlank(src, "Source File path is blank !");
        Assert.notNull(src, "Destination File path is null !");
        return copyFile(Paths.get(src), Paths.get(dest), options).toFile();
    }

    /**
     * 通过JDK7+的 {@link Files#copy(Path, Path, CopyOption...)} 方法拷贝文件
     *
     * @param src     源文件
     * @param dest    目标文件或目录，如果为目录使用与源文件相同的文件名
     * @param options {@link StandardCopyOption}
     * @return File
     * @throws IORuntimeException IO异常
     */
    public static File copyFile(File src, File dest, StandardCopyOption... options) throws IORuntimeException {
        // check

        Assert.notNull(src, "Source File is null !");
        if (false == src.exists()) {
            throw new IORuntimeException("File not exist: " + src);
        }
        Assert.notNull(dest, "Destination File or directiory is null !");
        if (equals(src, dest)) {
            throw new IORuntimeException("Files '" + src + "' and '" + dest + "' are equal");
        }

        Path srcPath = src.toPath();
        Path destPath = dest.isDirectory() ? dest.toPath().resolve(srcPath.getFileName()) : dest.toPath();
        try {
            return Files.copy(srcPath, destPath, options).toFile();
        } catch (Exception e) {
            throw new IORuntimeException(e);
        }
    }

    /**
     * 通过JDK7+的 {@link Files#copy(Path, Path, CopyOption...)} 方法拷贝文件
     *
     * @param src     源文件路径
     * @param dest    目标文件或目录，如果为目录使用与源文件相同的文件名
     * @param options {@link StandardCopyOption}
     * @return Path
     * @throws IORuntimeException IO异常
     */
    public static Path copyFile(Path src, Path dest, StandardCopyOption... options) throws IORuntimeException {
        Assert.notNull(src, "Source File is null !");
        Assert.notNull(dest, "Destination File or directiory is null !");

        Path destPath = dest.toFile().isDirectory() ? dest.resolve(src.getFileName()) : dest;
        try {
            return Files.copy(src, destPath, options);
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

    /**
     * 复制文件或目录<br>
     * <p>
     * 如果目标文件为目录，则将源文件以相同文件名拷贝到目标目录
     *
     * @param srcPath    源文件或目录
     * @param destPath   目标文件或目录，目标不存在会自动创建（目录、文件都创建）
     * @param isOverride 是否覆盖目标文件
     * @return 目标目录或文件
     * @throws IORuntimeException IO异常
     */
    public static File copy(String srcPath, String destPath, boolean isOverride) throws IORuntimeException {
        return copy(file(srcPath), file(destPath), isOverride);
    }

    /**
     * 复制文件或目录<br>
     * <p>
     * 情况如下：<br>
     * <p>
     * 1、src和dest都为目录，则讲src下所有文件目录拷贝到dest下<br>
     * <p>
     * 2、src和dest都为文件，直接复制，名字为dest<br>
     * <p>
     * 3、src为文件，dest为目录，将src拷贝到dest目录下<br>
     *
     * @param src        源文件
     * @param dest       目标文件或目录，目标不存在会自动创建（目录、文件都创建）
     * @param isOverride 是否覆盖目标文件
     * @return 目标目录或文件
     * @throws IORuntimeException IO异常
     */
    public static File copy(File src, File dest, boolean isOverride) throws IORuntimeException {
        // check

        Assert.notNull(src, "Source File is null !");
        if (false == src.exists()) {
            throw new IORuntimeException("File not exist: " + src);
        }
        Assert.notNull(dest, "Destination File or directiory is null !");
        if (equals(src, dest)) {
            throw new IORuntimeException("Files '" + src + "' and '" + dest + "' are equal");
        }

        if (src.isDirectory()) {// 复制目录

            internalCopyDir(src, dest, isOverride);
        } else {// 复制文件

            internalCopyFile(src, dest, isOverride);
        }
        return dest;
    }

    /**
     * 拷贝目录，只用于内部，不做任何安全检查
     *
     * @param src        源目录
     * @param dest       目标目录
     * @param isOverride 是否覆盖
     * @throws IORuntimeException IO异常
     */
    private static void internalCopyDir(File src, File dest, boolean isOverride) throws IORuntimeException {
        if (false == dest.exists()) {
            dest.mkdirs();
        } else if (dest.isFile()) {
            throw new IORuntimeException(String.format("Src [{}] is a directory but dest [{}] is a file!", src.getPath(), dest.getPath()));
        }

        final String files[] = src.list();
        for (String file : files) {
            File srcFile = new File(src, file);
            File destFile = new File(dest, file);
            // 递归复制

            if (srcFile.isDirectory()) {
                internalCopyDir(srcFile, destFile, isOverride);
            } else {
                internalCopyFile(srcFile, destFile, isOverride);
            }
        }
    }

    /**
     * 拷贝文件，只用于内部，不做任何安全检查
     *
     * @param src        源文件，必须为文件
     * @param dest       目标文件，必须为文件
     * @param isOverride 是否覆盖已有文件
     * @throws IORuntimeException IO异常
     */
    private static void internalCopyFile(File src, File dest, boolean isOverride) throws IORuntimeException {
        // copy

        if (false == dest.exists()) {// 目标不存在，默认做为文件创建

            touch(dest);
        } else if (dest.isDirectory()) {// 目标为目录，则在这个目录下创建同名文件

            dest = new File(dest, src.getName());
        } else if (false == isOverride) {// 如果已经存在目标文件，切为不覆盖模式，跳过之

            // StaticLog.debug("File [{}] already exist, ignore it.", dest);

            return;
        }

        // do copy file

        FileInputStream input = null;
        FileOutputStream output = null;
        try {
            input = new FileInputStream(src);
            output = new FileOutputStream(dest);
            IoUtil.copy(input, output);
        } catch (IOException e) {
            throw new IORuntimeException(e);
        } finally {
            IoUtil.close(output);
            IoUtil.close(input);
        }

        // 验证

        if (src.length() != dest.length()) {
            throw new IORuntimeException("Copy file failed of '" + src + "' to '" + dest + "' due to different sizes");
        }
    }

    /**
     * 移动文件或者目录
     *
     * @param src        源文件或者目录
     * @param dest       目标文件或者目录
     * @param isOverride 是否覆盖目标，只有目标为文件才覆盖
     * @throws IORuntimeException IO异常
     */
    public static void move(File src, File dest, boolean isOverride) throws IORuntimeException {
        // check

        if (!src.exists()) {
            throw new IORuntimeException("File already exist: " + src);
        }

        // 来源为文件夹，目标为文件

        if (src.isDirectory() && dest.isFile()) {
            throw new IORuntimeException(String.format("Can not move directory [{}] to file [{}]", src, dest));
        }

        if (isOverride && dest.isFile()) {// 只有目标为文件的情况下覆盖之

            dest.delete();
        }

        // 来源为文件，目标为文件夹

        if (src.isFile() && dest.isDirectory()) {
            dest = new File(dest, src.getName());
        }

        if (src.renameTo(dest) == false) {
            // 在文件系统不同的情况下，renameTo会失败，此时使用copy，然后删除原文件

            try {
                copy(src, dest, isOverride);
                src.delete();
            } catch (Exception e) {
                throw new IORuntimeException(String.format("Move [{}] to [{}] failed!", src, dest), e);
            }

        }
    }

    /**
     * 获取绝对路径<br>
     * <p>
     * 此方法不会判定给定路径是否有效（文件或目录存在）
     *
     * @param path      相对路径
     * @param baseClass 相对路径所相对的类
     * @return 绝对路径
     */
    public static String getAbsolutePath(String path, Class<?> baseClass) {
        if (path == null) {
            path = StringUtils.EMPTY;
        } else {
            path = normalize(path);

            if (CharPool.SLASH == path.charAt(0) || path.matches("^[a-zA-Z]:/.*")) {
                // 给定的路径已经是绝对路径了

                return path;
            }
        }

        // 兼容Spring风格的ClassPath路径，去除前缀，不区分大小写
        path = StringUtils.replaceOnceIgnoreCase(path, "classpath:", "");
        path = StringUtils.replaceOnceIgnoreCase(path, StringPool.SLASH, "");

        // 相对于ClassPath路径
        final URL url = ResourceUtil.getResource(path, baseClass);
        if (null != url) {
            //since 3.0.8 解决中文或空格路径被编码的问题
            return URLUtil.getDecodedPath(url);
        } else {
            //如果资源不存在，则返回一个拼接的资源绝对路径
            final String classPath = ClassUtil.getClassPath();
            if (null == classPath) {
                throw new NullPointerException("ClassPath is null !");
            }
            return classPath.concat(path);
        }
    }

    /**
     * 获取绝对路径，相对于ClassPath的目录<br>
     * <p>
     * 如果给定就是绝对路径，则返回原路径，原路径把所有\替换为/<br>
     * <p>
     * 兼容Spring风格的路径表示，例如：classpath:config/example.setting也会被识别后转换
     *
     * @param path 相对路径
     * @return 绝对路径
     */
    public static String getAbsolutePath(String path) {
        return getAbsolutePath(path, null);
    }

    /**
     * 获取标准的绝对路径
     *
     * @param file 文件
     * @return 绝对路径
     */
    public static String getAbsolutePath(File file) {
        if (file == null) {
            return null;
        }

        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            return file.getAbsolutePath();
        }
    }

    /**
     * 给定路径已经是绝对路径<br>
     * <p>
     * 此方法并没有针对路径做标准化，建议先执行{@link #normalize(String)}方法标准化路径后判断
     *
     * @param path 需要检查的Path
     * @return 是否已经是绝对路径
     */
    public static boolean isAbsolutePath(String path) {
        if (CharPool.SLASH == path.charAt(0) || path.matches("^[a-zA-Z]:/.*")) {
            // 给定的路径已经是绝对路径了
            return true;
        }
        return false;
    }

    /**
     * 判断是否为目录，如果path为null，则返回false
     *
     * @param path 文件路径
     * @return 如果为目录true
     */
    public static boolean isDirectory(String path) {
        return (path == null) ? false : file(path).isDirectory();
    }

    /**
     * 判断是否为目录，如果file为null，则返回false
     *
     * @param file 文件
     * @return 如果为目录true
     */
    public static boolean isDirectory(File file) {
        return (file == null) ? false : file.isDirectory();
    }

    /**
     * 判断是否为文件，如果path为null，则返回false
     *
     * @param path 文件路径
     * @return 如果为文件true
     */
    public static boolean isFile(String path) {
        return (path == null) ? false : file(path).isFile();
    }

    /**
     * 判断是否为文件，如果file为null，则返回false
     *
     * @param file 文件
     * @return 如果为文件true
     */
    public static boolean isFile(File file) {
        return (file == null) ? false : file.isFile();
    }

    /**
     * 检查两个文件是否是同一个文件<br>
     * <p>
     * 所谓文件相同，是指File对象是否指向同一个文件或文件夹
     *
     * @param file1 文件1
     * @param file2 文件2
     * @return 是否相同
     * @throws IORuntimeException IO异常
     * @see Files#isSameFile(Path, Path)
     */
    public static boolean equals(File file1, File file2) throws IORuntimeException {
        Assert.notNull(file1);
        Assert.notNull(file2);
        try {
            return Files.isSameFile(file1.toPath(), file2.toPath());
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

    /**
     * 获得最后一个文件路径分隔符的位置
     *
     * @param filePath 文件路径
     * @return 最后一个文件路径分隔符的位置
     */
    public static int indexOfLastSeparator(String filePath) {
        if (filePath == null) {
            return -1;
        }
        int lastUnixPos = filePath.lastIndexOf(UNIX_SEPARATOR);
        int lastWindowsPos = filePath.lastIndexOf(WINDOWS_SEPARATOR);
        return (lastUnixPos >= lastWindowsPos) ? lastUnixPos : lastWindowsPos;
    }

    /**
     * 判断文件是否被改动<br>
     * <p>
     * 如果文件对象为 null 或者文件不存在，被视为改动
     *
     * @param file           文件对象
     * @param lastModifyTime 上次的改动时间
     * @return 是否被改动
     */
    public static boolean isModifed(File file, long lastModifyTime) {
        if (null == file || false == file.exists()) {
            return true;
        }
        return file.lastModified() != lastModifyTime;
    }

    /**
     * 修复路径<br>
     * <ol>
     * <li>1. 统一用 /</li>
     * <li>2. 多个 / 转换为一个 /</li>
     * <li>3. 去除两边空格</li>
     * <li>4. .. 和 . 转换为绝对路径</li>
     * <li>5. 去掉前缀，例如file:</li>
     * </ol>
     *
     * @param path 原路径
     * @return 修复后的路径
     */
    public static String normalize(String path) {
        if (path == null) {
            return null;
        }
        String pathToUse = path.replaceAll("[/\\\\]{1,}", "/").trim();

        int prefixIndex = pathToUse.indexOf(StringPool.COLON);
        String prefix = "";
        if (prefixIndex != -1) {
            prefix = pathToUse.substring(0, prefixIndex + 1);
            if (prefix.contains("/")) {
                prefix = "";
            } else {
                pathToUse = pathToUse.substring(prefixIndex + 1);
            }
        }
        if (pathToUse.startsWith(StringPool.SLASH)) {
            prefix = prefix + StringPool.SLASH;
            pathToUse = pathToUse.substring(1);
        }

        String[] pathList = StringUtils.split(pathToUse, CharPool.SLASH);
        List<String> pathElements = new LinkedList<String>();
        int tops = 0;

        for (int i = pathList.length - 1; i >= 0; i--) {
            String element = pathList[i];
            if (StringPool.PERIOD.equals(element)) {
                // 当前目录，丢弃

            } else if (StringPool.DOUBLE_PERIOD.equals(element)) {
                tops++;
            } else {
                if (tops > 0) {
                    // Merging path element with element corresponding to top path.

                    tops--;
                } else {
                    // Normal path element found.

                    pathElements.add(0, element);
                }
            }
        }

        // Remaining top paths need to be retained.

        for (int i = 0; i < tops; i++) {
            pathElements.add(0, StringPool.DOUBLE_PERIOD);
        }

        return prefix + CollectionUtil.join(pathElements, StringPool.SLASH);
    }

    /**
     * 获得相对子路径
     *
     * @param rootDir  绝对父路径
     * @param filePath 文件路径
     * @return 相对子路径
     */
    public static String subPath(String rootDir, String filePath) {
        return subPath(rootDir, file(filePath));
    }

    /**
     * 获得相对子路径
     *
     * @param rootDir 绝对父路径
     * @param file    文件
     * @return 相对子路径
     */
    public static String subPath(String rootDir, File file) {
        if (StringUtils.isEmpty(rootDir)) {
        }

        String subPath = null;
        try {
            subPath = file.getCanonicalPath();
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }

        if (StringUtils.isNotEmpty(rootDir) && StringUtils.isNotEmpty(subPath)) {
            rootDir = normalize(rootDir);
            subPath = normalize(subPath);

            if (subPath != null && subPath.toLowerCase().startsWith(subPath.toLowerCase())) {
                subPath = subPath.substring(rootDir.length() + 1);
            }
        }
        return subPath;
    }

    // -------------------------------------------------------------------------------------------- name start

    /**
     * 返回主文件名
     *
     * @param file 文件
     * @return 主文件名
     */
    public static String mainName(File file) {
        if (file.isDirectory()) {
            return file.getName();
        }
        return mainName(file.getName());
    }

    /**
     * 返回主文件名
     *
     * @param fileName 完整文件名
     * @return 主文件名
     */
    public static String mainName(String fileName) {
        if (StringUtils.isBlank(fileName) || false == fileName.contains(StringPool.PERIOD)) {
            return fileName;
        }
        return StringUtils.substring(fileName, fileName.lastIndexOf(StringPool.PERIOD));
    }

    /**
     * 获取文件扩展名，扩展名不带“.”
     *
     * @param file 文件
     * @return 扩展名
     */
    public static String extName(File file) {
        if (null == file) {
            return null;
        }
        if (file.isDirectory()) {
            return null;
        }
        return extName(file.getName());
    }

    /**
     * 获得文件的扩展名，扩展名不带“.”
     *
     * @param fileName 文件名
     * @return 扩展名
     */
    public static String extName(String fileName) {
        if (fileName == null) {
            return null;
        }
        int index = fileName.lastIndexOf(StringPool.PERIOD);
        if (index == -1) {
            return StringUtils.EMPTY;
        } else {
            String ext = fileName.substring(index + 1);
            // 扩展名中不能包含路径相关的符号

            return (ext.contains(String.valueOf(UNIX_SEPARATOR)) || ext.contains(String.valueOf(WINDOWS_SEPARATOR))) ? StringUtils.EMPTY : ext;
        }
    }

    /**
     * 判断文件路径是否有指定后缀，忽略大小写<br>
     * <p>
     * 常用语判断扩展名
     *
     * @param file   文件或目录
     * @param suffix 后缀
     * @return 是否有指定后缀
     */
    public static boolean pathEndsWith(File file, String suffix) {
        return file.getPath().toLowerCase().endsWith(suffix);
    }
    // -------------------------------------------------------------------------------------------- name end


    // -------------------------------------------------------------------------------------------- in start

    /**
     * 获得输入流
     *
     * @param file 文件
     * @return 输入流
     * @throws IORuntimeException 文件未找到
     */
    public static BufferedInputStream getInputStream(File file) throws IORuntimeException {
        try (FileInputStream fis = new FileInputStream(file);) {
            return new BufferedInputStream(fis);
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

    /**
     * 获得输入流
     *
     * @param path 文件路径
     * @return 输入流
     * @throws IORuntimeException 文件未找到
     */
    public static BufferedInputStream getInputStream(String path) throws IORuntimeException {
        return getInputStream(file(path));
    }

    /**
     * 获得一个文件读取器
     *
     * @param file 文件
     * @return BufferedReader对象
     * @throws IORuntimeException IO异常
     */
    public static BufferedReader getUtf8Reader(File file) throws IORuntimeException {
        return getReader(file, Charset.forName(StringPool.UTF8));
    }

    /**
     * 获得一个文件读取器
     *
     * @param path 文件路径
     * @return BufferedReader对象
     * @throws IORuntimeException IO异常
     */
    public static BufferedReader getUtf8Reader(String path) throws IORuntimeException {
        return getReader(path, Charset.forName(StringPool.UTF8));
    }

    /**
     * 获得一个文件读取器
     *
     * @param file        文件
     * @param charsetName 字符集
     * @return BufferedReader对象
     * @throws IORuntimeException IO异常
     */
    public static BufferedReader getReader(File file, String charsetName) throws IORuntimeException {
        return IoUtil.getReader(getInputStream(file), charsetName);
    }

    /**
     * 获得一个文件读取器
     *
     * @param file    文件
     * @param charset 字符集
     * @return BufferedReader对象
     * @throws IORuntimeException IO异常
     */
    public static BufferedReader getReader(File file, Charset charset) throws IORuntimeException {
        return IoUtil.getReader(getInputStream(file), charset);
    }

    /**
     * 获得一个文件读取器
     *
     * @param path        绝对路径
     * @param charsetName 字符集
     * @return BufferedReader对象
     * @throws IORuntimeException IO异常
     */
    public static BufferedReader getReader(String path, String charsetName) throws IORuntimeException {
        return getReader(file(path), charsetName);
    }

    /**
     * 获得一个文件读取器
     *
     * @param path    绝对路径
     * @param charset 字符集
     * @return BufferedReader对象
     * @throws IORuntimeException IO异常
     */
    public static BufferedReader getReader(String path, Charset charset) throws IORuntimeException {
        return getReader(file(path), charset);
    }

    // -------------------------------------------------------------------------------------------- in end


    /**
     * 读取文件所有数据<br>
     * <p>
     * 文件的长度不能超过Integer.MAX_VALUE
     *
     * @param file 文件
     * @return 字节码
     * @throws IORuntimeException IO异常
     */
    public static byte[] readBytes(File file) throws IORuntimeException {
        return FileReader.create(file).readBytes();
    }

    /**
     * 读取文件内容
     *
     * @param file 文件
     * @return 内容
     * @throws IORuntimeException IO异常
     */
    public static String readUtf8String(File file) throws IORuntimeException {
        return readString(file, Charset.forName(StringPool.UTF8));
    }

    /**
     * 读取文件内容
     *
     * @param path 文件路径
     * @return 内容
     * @throws IORuntimeException IO异常
     */
    public static String readUtf8String(String path) throws IORuntimeException {
        return readString(path, Charset.forName(StringPool.UTF8));
    }

    /**
     * 读取文件内容
     *
     * @param file        文件
     * @param charsetName 字符集
     * @return 内容
     * @throws IORuntimeException IO异常
     */
    public static String readString(File file, String charsetName) throws IORuntimeException {
        return readString(file, Charset.forName(charsetName));
    }

    /**
     * 读取文件内容
     *
     * @param file    文件
     * @param charset 字符集
     * @return 内容
     * @throws IORuntimeException IO异常
     */
    public static String readString(File file, Charset charset) throws IORuntimeException {
        return FileReader.create(file, charset).readString();
    }

    /**
     * 读取文件内容
     *
     * @param path        文件路径
     * @param charsetName 字符集
     * @return 内容
     * @throws IORuntimeException IO异常
     */
    public static String readString(String path, String charsetName) throws IORuntimeException {
        return readString(file(path), charsetName);
    }

    /**
     * 读取文件内容
     *
     * @param path    文件路径
     * @param charset 字符集
     * @return 内容
     * @throws IORuntimeException IO异常
     */
    public static String readString(String path, Charset charset) throws IORuntimeException {
        return readString(file(path), charset);
    }

    /**
     * 读取文件内容
     *
     * @param url     文件URL
     * @param charset 字符集
     * @return 内容
     * @throws IORuntimeException IO异常
     */
    public static String readString(URL url, String charset) throws IORuntimeException {
        if (url == null) {
            throw new NullPointerException("Empty url provided!");
        }

        InputStream in = null;
        try {
            in = url.openStream();
            return IoUtil.read(in, charset);
        } catch (IOException e) {
            throw new IORuntimeException(e);
        } finally {
            IoUtil.close(in);
        }
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param <T>        集合类型
     * @param path       文件路径
     * @param charset    字符集
     * @param collection 集合
     * @return 文件中的每行内容的集合
     * @throws IORuntimeException IO异常
     */
    public static <T extends Collection<String>> T readLines(String path, String charset, T collection) throws IORuntimeException {
        return readLines(file(path), charset, collection);
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param <T>        集合类型
     * @param file       文件路径
     * @param charset    字符集
     * @param collection 集合
     * @return 文件中的每行内容的集合
     * @throws IORuntimeException IO异常
     */
    public static <T extends Collection<String>> T readLines(File file, String charset, T collection) throws IORuntimeException {
        return FileReader.create(file, Charset.forName(charset)).readLines(collection);
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param <T>        集合类型
     * @param url        文件的URL
     * @param charset    字符集
     * @param collection 集合
     * @return 文件中的每行内容的集合
     * @throws IORuntimeException IO异常
     */
    public static <T extends Collection<String>> T readLines(URL url, String charset, T collection) throws IORuntimeException {
        InputStream in = null;
        try {
            in = url.openStream();
            return IoUtil.readLines(in, charset, collection);
        } catch (IOException e) {
            throw new IORuntimeException(e);
        } finally {
            IoUtil.close(in);
        }
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param url     文件的URL
     * @param charset 字符集
     * @return 文件中的每行内容的集合List
     * @throws IORuntimeException IO异常
     */
    public static List<String> readLines(URL url, String charset) throws IORuntimeException {
        return readLines(url, charset, new ArrayList<String>());
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param path    文件路径
     * @param charset 字符集
     * @return 文件中的每行内容的集合List
     * @throws IORuntimeException IO异常
     */
    public static List<String> readLines(String path, String charset) throws IORuntimeException {
        return readLines(path, charset, new ArrayList<String>());
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param file    文件
     * @param charset 字符集
     * @return 文件中的每行内容的集合List
     * @throws IORuntimeException IO异常
     */
    public static List<String> readLines(File file, String charset) throws IORuntimeException {
        return readLines(file, charset, new ArrayList<String>());
    }

    // -------------------------------------------------------------------------------------------- out start

    /**
     * 获得一个输出流对象
     *
     * @param file 文件
     * @return 输出流对象
     * @throws IORuntimeException IO异常
     */
    public static BufferedOutputStream getOutputStream(File file) throws IORuntimeException {
        try (FileOutputStream fos = new FileOutputStream(touch(file))) {
            return new BufferedOutputStream(fos);
        } catch (Exception e) {
            throw new IORuntimeException(e);
        }
    }

    /**
     * 获得一个输出流对象
     *
     * @param path 输出到的文件路径，绝对路径
     * @return 输出流对象
     * @throws IORuntimeException IO异常
     */
    public static BufferedOutputStream getOutputStream(String path) throws IORuntimeException {
        return getOutputStream(touch(path));
    }

    /**
     * 获得一个带缓存的写入对象
     *
     * @param path        输出路径，绝对路径
     * @param charsetName 字符集
     * @param isAppend    是否追加
     * @return BufferedReader对象
     * @throws IORuntimeException IO异常
     */
    public static BufferedWriter getWriter(String path, String charsetName, boolean isAppend) throws IORuntimeException {
        return getWriter(touch(path), Charset.forName(charsetName), isAppend);
    }

    /**
     * 获得一个带缓存的写入对象
     *
     * @param path     输出路径，绝对路径
     * @param charset  字符集
     * @param isAppend 是否追加
     * @return BufferedReader对象
     * @throws IORuntimeException IO异常
     */
    public static BufferedWriter getWriter(String path, Charset charset, boolean isAppend) throws IORuntimeException {
        return getWriter(touch(path), charset, isAppend);
    }

    /**
     * 获得一个带缓存的写入对象
     *
     * @param file        输出文件
     * @param charsetName 字符集
     * @param isAppend    是否追加
     * @return BufferedReader对象
     * @throws IORuntimeException IO异常
     */
    public static BufferedWriter getWriter(File file, String charsetName, boolean isAppend) throws IORuntimeException {
        return getWriter(file, Charset.forName(charsetName), isAppend);
    }

    /**
     * 获得一个带缓存的写入对象
     *
     * @param file     输出文件
     * @param charset  字符集
     * @param isAppend 是否追加
     * @return BufferedReader对象
     * @throws IORuntimeException IO异常
     */
    public static BufferedWriter getWriter(File file, Charset charset, boolean isAppend) throws IORuntimeException {
        return FileWriter.create(file, charset).getWriter(isAppend);
    }

    /**
     * 获得一个打印写入对象，可以有print
     *
     * @param path     输出路径，绝对路径
     * @param charset  字符集
     * @param isAppend 是否追加
     * @return 打印对象
     * @throws IORuntimeException IO异常
     */
    public static PrintWriter getPrintWriter(String path, String charset, boolean isAppend) throws IORuntimeException {
        return new PrintWriter(getWriter(path, charset, isAppend));
    }

    /**
     * 获得一个打印写入对象，可以有print
     *
     * @param file     文件
     * @param charset  字符集
     * @param isAppend 是否追加
     * @return 打印对象
     * @throws IORuntimeException IO异常
     */
    public static PrintWriter getPrintWriter(File file, String charset, boolean isAppend) throws IORuntimeException {
        return new PrintWriter(getWriter(file, charset, isAppend));
    }

    // -------------------------------------------------------------------------------------------- out end


    /**
     * 将String写入文件，覆盖模式，字符集为UTF-8
     *
     * @param content 写入的内容
     * @param path    文件路径
     * @return 写入的文件
     * @throws IORuntimeException IO异常
     */
    public static File writeUtf8String(String content, String path) throws IORuntimeException {
        return writeString(content, path, StringPool.UTF8);
    }

    /**
     * 将String写入文件，覆盖模式，字符集为UTF-8
     *
     * @param content 写入的内容
     * @param file    文件
     * @return 写入的文件
     * @throws IORuntimeException IO异常
     */
    public static File writeUtf8String(String content, File file) throws IORuntimeException {
        return writeString(content, file, StringPool.UTF8);
    }

    /**
     * 将String写入文件，覆盖模式
     *
     * @param content 写入的内容
     * @param path    文件路径
     * @param charset 字符集
     * @return 写入的文件
     * @throws IORuntimeException IO异常
     */
    public static File writeString(String content, String path, String charset) throws IORuntimeException {
        return writeString(content, touch(path), charset);
    }

    /**
     * 将String写入文件，覆盖模式
     *
     * @param content 写入的内容
     * @param file    文件
     * @param charset 字符集
     * @return 被写入的文件
     * @throws IORuntimeException IO异常
     */
    public static File writeString(String content, File file, String charset) throws IORuntimeException {
        return FileWriter.create(file, Charset.forName(charset)).write(content);
    }

    /**
     * 将String写入文件，追加模式
     *
     * @param content 写入的内容
     * @param path    文件路径
     * @param charset 字符集
     * @return 写入的文件
     * @throws IORuntimeException IO异常
     */
    public static File appendString(String content, String path, String charset) throws IORuntimeException {
        return appendString(content, touch(path), charset);
    }

    /**
     * 将String写入文件，追加模式
     *
     * @param content 写入的内容
     * @param file    文件
     * @param charset 字符集
     * @return 写入的文件
     * @throws IORuntimeException IO异常
     */
    public static File appendString(String content, File file, String charset) throws IORuntimeException {
        return FileWriter.create(file, Charset.forName(charset)).append(content);
    }

    /**
     * 将列表写入文件，覆盖模式
     *
     * @param <T>     集合元素类型
     * @param list    列表
     * @param path    绝对路径
     * @param charset 字符集
     * @throws IORuntimeException IO异常
     */
    public static <T> void writeLines(Collection<T> list, String path, String charset) throws IORuntimeException {
        writeLines(list, path, charset, false);
    }

    /**
     * 将列表写入文件，追加模式
     *
     * @param <T>     集合元素类型
     * @param list    列表
     * @param path    绝对路径
     * @param charset 字符集
     * @throws IORuntimeException IO异常
     */
    public static <T> void appendLines(Collection<T> list, String path, String charset) throws IORuntimeException {
        writeLines(list, path, charset, true);
    }

    /**
     * 将列表写入文件
     *
     * @param <T>      集合元素类型
     * @param list     列表
     * @param path     文件路径
     * @param charset  字符集
     * @param isAppend 是否追加
     * @return 文件
     * @throws IORuntimeException IO异常
     */
    public static <T> File writeLines(Collection<T> list, String path, String charset, boolean isAppend) throws IORuntimeException {
        return writeLines(list, file(path), charset, isAppend);
    }

    /**
     * 将列表写入文件
     *
     * @param <T>      集合元素类型
     * @param list     列表
     * @param file     文件
     * @param charset  字符集
     * @param isAppend 是否追加
     * @return 文件
     * @throws IORuntimeException IO异常
     */
    public static <T> File writeLines(Collection<T> list, File file, String charset, boolean isAppend) throws IORuntimeException {
        return FileWriter.create(file, Charset.forName(charset)).writeLines(list, isAppend);
    }

    /**
     * 写数据到文件中
     *
     * @param data 数据
     * @param path 目标文件
     * @return 文件
     * @throws IORuntimeException IO异常
     */
    public static File writeBytes(byte[] data, String path) throws IORuntimeException {
        return writeBytes(data, touch(path));
    }

    /**
     * 写数据到文件中
     *
     * @param dest 目标文件
     * @param data 数据
     * @return dest
     * @throws IORuntimeException IO异常
     */
    public static File writeBytes(byte[] data, File dest) throws IORuntimeException {
        return writeBytes(data, dest, 0, data.length, false);
    }

    /**
     * 写入数据到文件
     *
     * @param data     数据
     * @param dest     目标文件
     * @param off      数据开始位置
     * @param len      数据长度
     * @param isAppend 是否追加模式
     * @return File
     * @throws IORuntimeException IO异常
     */
    public static File writeBytes(byte[] data, File dest, int off, int len, boolean isAppend) throws IORuntimeException {
        return FileWriter.create(dest).write(data, off, len, isAppend);
    }

    /**
     * 将流的内容写入文件<br>
     *
     * @param dest 目标文件
     * @param in   输入流
     * @return dest
     * @throws IORuntimeException IO异常
     */
    public static File writeFromStream(InputStream in, File dest) throws IORuntimeException {
        return FileWriter.create(dest).writeFromStream(in);
    }

    /**
     * 将流的内容写入文件<br>
     *
     * @param in           输入流
     * @param fullFilePath 文件绝对路径
     * @return dest
     * @throws IORuntimeException IO异常
     */
    public static File writeFromStream(InputStream in, String fullFilePath) throws IORuntimeException {
        return writeFromStream(in, touch(fullFilePath));
    }

    /**
     * 将文件写入流中
     *
     * @param file 文件
     * @param out  流
     * @return File
     * @throws IORuntimeException IO异常
     */
    public static File writeToStream(File file, OutputStream out) throws IORuntimeException {
        return FileReader.create(file).writeToStream(out);
    }

    /**
     * 将流的内容写入文件<br>
     *
     * @param fullFilePath 文件绝对路径
     * @param out          输出流
     * @throws IORuntimeException IO异常
     */
    public static void writeToStream(String fullFilePath, OutputStream out) throws IORuntimeException {
        writeToStream(touch(fullFilePath), out);
    }

    /**
     * 可读的文件大小
     *
     * @param file 文件
     * @return 大小
     */
    public static String readableFileSize(File file) {
        return readableFileSize(file.length());
    }

    /**
     * 可读的文件大小<br>
     * <p>
     * 参考 http://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc
     *
     * @param size Long类型大小
     * @return 大小
     */
    public static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB", "EB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.##").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * 去掉指定前缀
     *
     * @param str    字符串
     * @param prefix 前缀
     * @return 切掉后的字符串，若前缀不是 preffix， 返回原字符串
     */
    public static String removePrefix(CharSequence str, CharSequence prefix) {
        if (StringUtils.isEmpty(str) || StringUtils.isEmpty(prefix)) {
            return str.toString();
        }

        final String str2 = str.toString();
        if (str2.startsWith(prefix.toString())) {
            return StringUtils.substring(str2, prefix.length());// 截取后半段
        }
        return str2;
    }

    /**
     * 断点续传文件
     *
     * @param response
     * @param request
     * @param location
     * @return
     */
    public static boolean downFile(HttpServletResponse response, HttpServletRequest request, String location) {
        try {
            File file = new File(location);
            if (file.exists()) {
                long p = 0L;
                long toLength = 0L;
                long contentLength = 0L;
                int rangeSwitch = 0; // 0,从头开始的全文下载；1,从某字节开始的下载（bytes=27000-）；2,从某字节开始到某字节结束的下载（bytes=27000-39000）
                long fileLength;
                String rangBytes = "";
                fileLength = file.length();

                // get file content
                try (InputStream ins = new FileInputStream(file);
                     BufferedInputStream bis = new BufferedInputStream(ins);
                     OutputStream out = response.getOutputStream();) {

                    // tell the client to allow accept-ranges
                    response.reset();
                    response.setHeader("Accept-Ranges", "bytes");

                    // client requests a file block download start byte
                    String range = request.getHeader("Range");
                    if (range != null && range.trim().length() > 0 && !"null".equals(range)) {
                        response.setStatus(javax.servlet.http.HttpServletResponse.SC_PARTIAL_CONTENT);
                        rangBytes = range.replaceAll("bytes=", "");
                        if (rangBytes.endsWith("-")) {  // bytes=270000-
                            rangeSwitch = 1;
                            p = Long.parseLong(rangBytes.substring(0, rangBytes.indexOf("-")));
                            contentLength = fileLength - p;  // 客户端请求的是270000之后的字节（包括bytes下标索引为270000的字节）
                        } else { // bytes=270000-320000
                            rangeSwitch = 2;
                            String temp1 = rangBytes.substring(0, rangBytes.indexOf("-"));
                            String temp2 = rangBytes.substring(rangBytes.indexOf("-") + 1, rangBytes.length());
                            p = Long.parseLong(temp1);
                            toLength = Long.parseLong(temp2);
                            contentLength = toLength - p + 1; // 客户端请求的是 270000-320000 之间的字节
                        }
                    } else {
                        contentLength = fileLength;
                    }

                    // 如果设设置了Content-Length，则客户端会自动进行多线程下载。如果不希望支持多线程，则不要设置这个参数。
                    // Content-Length: [文件的总大小] - [客户端请求的下载的文件块的开始字节]
                    response.setHeader("Content-Length", new Long(contentLength).toString());

                    // 断点开始
                    // 响应的格式是:
                    // Content-Range: bytes [文件块的开始字节]-[文件的总大小 - 1]/[文件的总大小]
                    if (rangeSwitch == 1) {
                        String contentRange = new StringBuffer("bytes ").append(new Long(p).toString()).append("-")
                                .append(new Long(fileLength - 1).toString()).append("/")
                                .append(new Long(fileLength).toString()).toString();
                        response.setHeader("Content-Range", contentRange);
                        bis.skip(p);
                    } else if (rangeSwitch == 2) {
                        String contentRange = range.replace("=", " ") + "/" + new Long(fileLength).toString();
                        response.setHeader("Content-Range", contentRange);
                        bis.skip(p);
                    } else {
                        String contentRange = new StringBuffer("bytes ").append("0-")
                                .append(fileLength - 1).append("/")
                                .append(fileLength).toString();
                        response.setHeader("Content-Range", contentRange);
                    }

                    String fileName = file.getName();
                    response.reset();
                    //response.setHeader("content-disposition", "attachment;filename=" + new String((fileName).getBytes("UTF-8"), "ISO8859-1"));
                    String userAgent = request.getHeader("user-agent");
                    if (userAgent != null && userAgent.indexOf("Edge") >= 0) {
                        fileName = URLEncoder.encode(fileName, "UTF8");
                    } else if (userAgent.indexOf("Firefox") >= 0 || userAgent.indexOf("Chrome") >= 0
                            || userAgent.indexOf("Safari") >= 0) {
                        fileName = new String((fileName).getBytes("UTF-8"), "ISO8859-1");
                    } else {
                        fileName = URLEncoder.encode(fileName, "UTF8"); //其他浏览器
                    }
                    response.setContentType("application/octet-stream");
                    response.addHeader("Content-Disposition", "attachment;filename=" + fileName);

                    int n = 0;
                    long readLength = 0;
                    int bsize = 1024;
                    byte[] bytes = new byte[bsize];
                    if (rangeSwitch == 2) {
                        // 针对 bytes=27000-39000 的请求，从27000开始写数据
                        while (readLength <= contentLength - bsize) {
                            n = bis.read(bytes);
                            readLength += n;
                            out.write(bytes, 0, n);
                        }
                        if (readLength <= contentLength) {
                            n = bis.read(bytes, 0, (int) (contentLength - readLength));
                            out.write(bytes, 0, n);
                        }
                    } else {
                        while ((n = bis.read(bytes)) != -1) {
                            out.write(bytes, 0, n);
                        }
                    }
                }
            } else {
                throw new Exception("file not found");
            }
        } catch (IOException ie) {
            // 忽略 ClientAbortException 之类的异常
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 将ShiroHttpServletRequest转换成MultipartHttpServletRequest用于上传
     *
     * @param request
     * @return
     */
    public static MultipartHttpServletRequest getMultipartHttpServletRequest(HttpServletRequest request) {
        ShiroHttpServletRequest shiroRequest = (ShiroHttpServletRequest) request;
        CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
        MultipartHttpServletRequest multipartRequest = commonsMultipartResolver.resolveMultipart((HttpServletRequest) shiroRequest.getRequest());
        return multipartRequest;
    }

    /**
     * 将上传的文件写入磁盘
     *
     * @param fileMap
     * @param filePath
     * @param newFileName
     * @throws IOException
     */
    public static String uploadFile(Map<String, MultipartFile> fileMap, String filePath, String newFileName) throws IOException {
        String originalFileName = null;
        String uploadFileLocation = "";
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            MultipartFile mf = entity.getValue();
            originalFileName = mf.getOriginalFilename();
            String fileExt = originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();

            // 创建文件夹
            File imageFile = new File(filePath);
            if (!imageFile.exists()) {
                imageFile.mkdirs();
            }
            uploadFileLocation = filePath + "\\" + newFileName + "." + fileExt;
            if (uploadInputStream(mf.getInputStream(), uploadFileLocation)) {
                System.out.println(" ------- image upload success");
            }
        }
        return uploadFileLocation;
    }

    /**
     * 将上传的文件写入磁盘
     *
     * @param mf
     * @param filePath
     * @param newFileName
     * @throws IOException
     */
    public static JSONObject uploadFile(MultipartFile mf, String filePath, String newFileName) throws IOException {
        String originalFileName = null;
        String uploadFileLocation = "";
        originalFileName = mf.getOriginalFilename();
        String fileExt = originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();

        // 创建文件夹
        File imageFile = new File(filePath);
        if (!imageFile.exists()) {
            imageFile.mkdirs();
        }
        uploadFileLocation = filePath + File.separator + newFileName + "." + fileExt;
        if (uploadInputStream(mf.getInputStream(), uploadFileLocation)) {
            System.out.println(" ------- image upload success");
        }
        JSONObject obj = new JSONObject();
        obj.put("realFileName", newFileName + "." + fileExt);
        return obj;
    }

    /**
     * @Title: htmlUpload @param @param inputStream 传进一个流 @param @param
     * uploadFile 服务端输出的路径和文件名 @return boolean 返回类型 @throws
     */
    public static boolean uploadInputStream(InputStream inputStream, String uploadFile) {
        try (FileOutputStream output = new FileOutputStream(uploadFile)) {

            byte[] buff = new byte[4096]; // 缓冲区
            int bytecount = 1;
            while ((bytecount = inputStream.read(buff, 0, 4096)) != -1) { // 当input.read()方法，不能读取到字节流的时候，返回-1
                output.write(buff, 0, bytecount); // 写入字节到文件
            }
            output.flush();
            output.close();

            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    /**
     * <b>function:</b>处理后的系统文件路径
     *
     * @param path 文件路径
     * @return 返回处理后的路径
     * @author hoojo
     * @createDate Oct 10, 2010 12:49:31 AM
     */
    public static String getDoPath(String path) {
        path = path.replace("\\", "/");
        String lastChar = path.substring(path.length() - 1);
        if (!"/".equals(lastChar)) {
            path += "/";
        }
        return path;
    }

    /**
     * <b>function:</b>处理后的系统文件路径
     *
     * @param path 文件路径
     * @return 返回处理后的路径
     * @author hoojo
     * @createDate Oct 10, 2010 12:49:31 AM
     */
    public static String replaceFirstPath(String path) {
        path = path.replace("\\", "/");
        String lastChar = path.substring(0, 1);
        if ("/".equals(lastChar)) {
            return path.substring(1, path.length());
        }
        return path;
    }

    /**
     * <b>function:</b> 传入一个文件名，得到这个文件名称的后缀
     *
     * @param fileName 文件名
     * @return 后缀名
     * @author hoojo
     * @createDate Oct 9, 2010 11:30:46 PM
     */
    public static String getSuffix(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index != -1) {
            String suffix = fileName.substring(index);// 后缀
            return suffix;
        } else {
            return null;
        }
    }
}
