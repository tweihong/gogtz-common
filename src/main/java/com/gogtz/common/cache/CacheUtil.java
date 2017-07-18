/**
 * Description: 缓存工具类
 * <p>服务器缓存，重启后丢失，不支持集群</p>
 * Copyright: Copyright (Gogtz Corporation)2017
 * Company: Gogtz Corporation
 *
 * @author: t
 * @version: 1.0
 * Created at: 2017年7月17日15:26:21
 * Modification History:
 * Modified by :
 */
package com.gogtz.common.cache;

import java.util.HashMap;

public class CacheUtil {
    private static HashMap<String, Object> cache;

    /**
     * 从缓存中取得数据
     *
     * @param key
     * @return
     */
    public static Object get(String key) {
        if (cache == null) {
            return null;
        }
        return cache.get(key);
    }

    /**
     * 从缓存中取得String类型的数据
     *
     * @param key
     * @return
     */
    public static String getString(String key) {
        Object obj = get(key);
        if (obj != null) {
            return obj.toString();
        }
        return null;
    }

    /**
     * 添加缓存
     *
     * @param key
     * @param value
     */
    public static void put(String key, Object value) {
        if (cache == null) {
            cache = new HashMap<String, Object>();
        }
        cache.put(key, value);
    }

    /**
     * 从缓存中移除内容
     *
     * @param key
     */
    public static void remove(String key) {
        if (cache == null) {
            cache = new HashMap<String, Object>();
        }
        if (cache.containsKey(key)) {
            cache.remove(key);
        }
    }
}
