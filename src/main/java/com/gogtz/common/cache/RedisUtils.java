
/**
 * Description: Redis缓存工具类
 * <p>需要配置文件支持：properties/redis.properties</li>
 * <p>最大分配的对象数：redis.pool.maxActive</li>
 * <p>最大能够保持状态的对象数：redis.pool.maxIdle</li>
 * <p>当池内没有对象时，最大等待时间：redis.pool.maxWait</li>
 * <p>当调用borrow Object方法时，是否进行有效检查：redis.pool.testOnBorrow</li>
 * <p>当调用return Object方法时，是否进行有效检查：redis.pool.testOnReturn</li>
 * <p>密码：redis.pool.password</li>
 * <p>redis服务器Ip地址：redis.ip</li>
 * <p>redis服务器端口：redis.port</li>
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

import com.gogtz.common.properties.PropertyUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * <h1>Redis缓存工具类</h1>
 * <ol><strong>
 * <li>需要配置文件支持：properties/redis.properties</li> 
 * <li>最大分配的对象数：redis.pool.maxActive</li> 
 * <li>最大能够保持状态的对象数：redis.pool.maxIdle</li> 
 * <li>当池内没有对象时，最大等待时间：redis.pool.maxWait</li> 
 * <li>当调用borrow Object方法时，是否进行有效检查：redis.pool.testOnBorrow</li> 
 * <li>当调用return Object方法时，是否进行有效检查：redis.pool.testOnReturn</li> 
 * <li>密码：redis.pool.password</li> 
 * <li>redis服务器Ip地址：redis.ip</li> 
 * <li>redis服务器端口：redis.port</li>
 * </strong>
 * </ol>
 *
 * @author: t
 * @version: 1.0
 */
public class RedisUtils {
    private static JedisPool pool = null;

    private static ThreadLocal<JedisPool> poolThreadLocal = new ThreadLocal<JedisPool>();

    public static final int signExpireTime = 86400;

    /**
     * 构建redis连接池
     *
     * @return JedisPool
     */
    public static JedisPool getPool() {
        if (pool == null) {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxActive(Integer.valueOf(PropertyUtil.getRedisValue("redis.pool.maxActive")));
            config.setMaxIdle(Integer.valueOf(PropertyUtil.getRedisValue("redis.pool.maxIdle")));
            config.setMaxWait(Long.valueOf(PropertyUtil.getRedisValue("redis.pool.maxWait")));
            config.setTestOnBorrow(Boolean.valueOf(PropertyUtil.getRedisValue("redis.pool.testOnBorrow")));
            config.setTestOnReturn(Boolean.valueOf(PropertyUtil.getRedisValue("redis.pool.testOnReturn")));
            // 测试环境
            // pool = new JedisPool(config, bundle.getString("redis.ip"),
            // Integer.valueOf(bundle.getString("redis.port")));
            // 线上环境
            pool =
                    new JedisPool(config, PropertyUtil.getRedisValue("redis.ip"), Integer.valueOf(PropertyUtil
                            .getRedisValue("redis.port")), 100000, PropertyUtil.getRedisValue("redis.pool.password"));
        }
        return pool;
    }

    /**
     * 取得链接
     *
     * @return
     */
    public static JedisPool getConnection() {
        // ②如果poolThreadLocal没有本线程对应的JedisPool创建一个新的JedisPool，将其保存到线程本地变量中。
        if (poolThreadLocal.get() == null) {
            pool = RedisUtils.getPool();
            poolThreadLocal.set(pool);
            return pool;
        } else {
            return poolThreadLocal.get();// ③直接返回线程本地变量
        }
    }

    /**
     * 返还到连接池
     *
     * @param pool  pool
     * @param redis redis
     */
    public static void returnResource(JedisPool pool, Jedis redis) {
        if (redis != null) {
            pool.returnResource(redis);
        }
    }

    /**
     * 累加值
     *
     * @param key key
     * @return
     */
    public static Long incr(String key) {
        Long value = null;

        JedisPool pool = null;
        Jedis jedis = null;
        try {
            pool = getPool();
            jedis = pool.getResource();
            value = jedis.incr(key);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放redis对象
            pool.returnBrokenResource(jedis);
            // 返还到连接池
            returnResource(pool, jedis);
        }

        return value;
    }

    /**
     * 获取数据
     *
     * @param key key
     * @return
     */
    public static String get(String key) {
        String value = null;

        JedisPool pool = null;
        Jedis jedis = null;
        try {
            pool = getPool();
            jedis = pool.getResource();
            value = jedis.get(key);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放redis对象
            pool.returnBrokenResource(jedis);
            // 返还到连接池
            returnResource(pool, jedis);
        }

        return value;
    }

    /**
     * 获取数据
     *
     * @param key key
     * @return
     */
    public static byte[] get(byte[] key) {
        byte[] value = null;

        JedisPool pool = null;
        Jedis jedis = null;
        try {
            pool = getPool();
            jedis = pool.getResource();
            value = jedis.get(key);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放redis对象
            pool.returnBrokenResource(jedis);
            // 返还到连接池
            returnResource(pool, jedis);
        }

        return value;
    }

    /**
     * 删除数据
     *
     * @param key key
     * @return
     */
    public static Long del(String key) {
        Long value = null;

        JedisPool pool = null;
        Jedis jedis = null;
        try {
            pool = getPool();
            jedis = pool.getResource();
            value = jedis.del(key);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放redis对象
            pool.returnBrokenResource(jedis);
            // 返还到连接池
            returnResource(pool, jedis);
        }

        return value;
    }

    /**
     * 删除数据
     *
     * @param key key
     * @return
     */
    public static Long del(byte[] key) {
        Long value = null;

        JedisPool pool = null;
        Jedis jedis = null;
        try {
            pool = getPool();
            jedis = pool.getResource();
            value = jedis.del(key);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放redis对象
            pool.returnBrokenResource(jedis);
            // 返还到连接池
            returnResource(pool, jedis);
        }

        return value;
    }

    /**
     * 判断是否存在
     *
     * @param key key
     * @return
     */
    public static Boolean exists(String key) {
        Boolean value = null;

        JedisPool pool = null;
        Jedis jedis = null;
        try {
            pool = getPool();
            jedis = pool.getResource();
            value = jedis.exists(key);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放redis对象
            pool.returnBrokenResource(jedis);
            // 返还到连接池
            returnResource(pool, jedis);
        }

        return value;
    }

    /**
     * 赋值数据
     *
     * @param key           key
     * @param value         value
     * @param expireSeconds (过期时间，秒)
     * @return value
     */
    public static Long set(String key, String value, int expireSeconds) {
        Long result = null;
        JedisPool pool = null;
        Jedis jedis = null;
        try {
            pool = getPool();
            jedis = pool.getResource();
            jedis.set(key, value);
            result = jedis.expire(key, expireSeconds);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放redis对象
            pool.returnBrokenResource(jedis);
            // 返还到连接池
            returnResource(pool, jedis);
        }

        return result;
    }

    /**
     * 设置过期时间
     *
     * @param key           key
     * @param expireSeconds (过期时间，秒)
     * @return value
     */
    public static Long expire(String key, int expireSeconds) {
        Long result = null;
        JedisPool pool = null;
        Jedis jedis = null;
        try {
            pool = getPool();
            jedis = pool.getResource();
            result = jedis.expire(key, expireSeconds);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放redis对象
            pool.returnBrokenResource(jedis);
            // 返还到连接池
            returnResource(pool, jedis);
        }

        return result;
    }

    /**
     * 赋值数据
     *
     * @param key key
     * @return
     */
    public static String set(String key, String value) {
        String result = null;
        JedisPool pool = null;
        Jedis jedis = null;
        try {
            pool = getPool();
            jedis = pool.getResource();
            result = jedis.set(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放redis对象
            pool.returnBrokenResource(jedis);
            // 返还到连接池
            returnResource(pool, jedis);
        }

        return result;
    }

    /**
     * 赋值数据
     *
     * @param key key
     * @return
     */
    public static Long sadd(String key, String value) {
        Long result = null;
        JedisPool pool = null;
        Jedis jedis = null;
        try {
            pool = getPool();
            jedis = pool.getResource();
            result = jedis.sadd(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放redis对象
            pool.returnBrokenResource(jedis);
            // 返还到连接池
            returnResource(pool, jedis);
        }

        return result;
    }

    /**
     * 判断set中是否有值
     *
     * @param key key
     * @return
     */
    public static Boolean sismember(String key, String member) {
        Boolean result = null;
        JedisPool pool = null;
        Jedis jedis = null;
        try {
            pool = getPool();
            jedis = pool.getResource();
            result = jedis.sismember(key, member);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放redis对象
            pool.returnBrokenResource(jedis);
            // 返还到连接池
            returnResource(pool, jedis);
        }

        return result;
    }

    /**
     * redis链表左侧头部压栈
     *
     * @param key    key
     * @param values values
     * @return
     * @author renxingchen
     */
    public static Long lpush(String key, String... values) {
        Long result = null;
        JedisPool pool = null;
        Jedis jedis = null;
        try {
            pool = getPool();
            jedis = pool.getResource();
            result = jedis.rpush(key, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放redis对象
            pool.returnBrokenResource(jedis);
            // 返还到连接池
            returnResource(pool, jedis);
        }
        return result;
    }

    /**
     * redis链表右侧尾部出栈，入临时队列，如果没有值则阻塞
     *
     * @param source      source
     * @param destination destination
     * @param timeout     timeout
     * @return
     * @author renxingchen
     */
    public static String brpoplpush(String source, String destination, int timeout) {
        String result = null;
        JedisPool pool = null;
        Jedis jedis = null;
        try {
            pool = getPool();
            jedis = pool.getResource();
            result = jedis.brpoplpush(source, destination, timeout);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放redis对象
            pool.returnBrokenResource(jedis);
            // 返还到连接池
            returnResource(pool, jedis);
        }
        return result;
    }

    /**
     * redis链表右侧尾部出栈
     *
     * @param key key
     * @return
     * @author renxingchen
     */
    public static String rpop(String key) {
        String result = null;
        JedisPool pool = null;
        Jedis jedis = null;
        try {
            pool = getPool();
            jedis = pool.getResource();
            result = jedis.rpop(key);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放redis对象
            pool.returnBrokenResource(jedis);
            // 返还到连接池
            returnResource(pool, jedis);
        }
        return result;
    }

    /**
     * 移除特定元素
     *
     * @param key   key
     * @param count count
     * @param value value
     * @return
     * @author renxingchen
     */
    public static Long lrem(String key, long count, String value) {
        Long result = null;
        JedisPool pool = null;
        Jedis jedis = null;
        try {
            pool = getPool();
            jedis = pool.getResource();
            result = jedis.lrem(key, count, value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放redis对象
            pool.returnBrokenResource(jedis);
            // 返还到连接池
            returnResource(pool, jedis);
        }
        return result;
    }

    /**
     * 返回列表长度
     *
     * @param key key
     * @return
     * @author renxingchen
     */
    public static Long llen(String key) {
        Long result = null;
        JedisPool pool = null;
        Jedis jedis = null;
        try {
            pool = getPool();
            jedis = pool.getResource();
            result = jedis.llen(key);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放redis对象
            pool.returnBrokenResource(jedis);
            // 返还到连接池
            returnResource(pool, jedis);
        }
        return result;
    }

}
