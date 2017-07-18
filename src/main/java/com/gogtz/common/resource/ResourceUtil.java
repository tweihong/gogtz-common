package com.gogtz.common.resource;

import com.gogtz.common.collection.CollectionUtil;
import com.gogtz.common.file.IORuntimeException;
import com.gogtz.common.lang.ClassUtil;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

/**
 * ClassPath资源工具类
 *
 * @author Looly
 */
public class ResourceUtil {
    /**
     * 获得资源的URL<br>
     * 路径用/分隔，例如:
     * <pre>
     * config/a/db.config
     * spring/xml/test.xml
     * </pre>
     *
     * @param resource 资源（相对Classpath的路径）
     * @return 资源URL
     */
    public static URL getResource(String resource) throws IORuntimeException {
        return getResource(resource, null);
    }

    /**
     * 获取指定路径下的资源列表<br>
     * 路径格式必须为目录格式,用/分隔，例如:
     * <pre>
     * config/a
     * spring/xml
     * </pre>
     *
     * @param resource 资源路径
     * @return 资源列表
     */
    public static List<URL> getResources(String resource) {
        final Enumeration<URL> resources;
        try {
            resources = ClassUtil.getClassLoader().getResources(resource);
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
        return CollectionUtil.newArrayList(resources);
    }

    /**
     * 获得资源相对路径对应的URL
     *
     * @param resource  资源相对路径
     * @param baseClass 基准Class，获得的相对路径相对于此Class所在路径，如果为{@code null}则相对ClassPath
     * @return {@link URL}
     */
    public static URL getResource(String resource, Class<?> baseClass) {
        return (null != baseClass) ? baseClass.getResource(resource) : ClassUtil.getClassLoader().getResource(resource);
    }
}
