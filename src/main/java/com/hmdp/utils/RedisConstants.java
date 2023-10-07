package com.hmdp.utils;

public class RedisConstants {
    /**
     * 发送验证码 redis key
     */
    public static final String LOGIN_CODE_KEY = "login:code:";

    /**
     * 验证码失效时间 redis key
     */
    public static final Long LOGIN_CODE_TTL = 2L;

    /**
     * 登录token redis key
     */
    public static final String LOGIN_USER_KEY = "login:token:";

    /**
     * token失效时间 redis key
     */
    public static final Long LOGIN_USER_TTL = 30L;

    /**
     * 缓存空值 null redis key
     */
    public static final Long CACHE_NULL_TTL = 2L;

    /**
     * 商铺数据失效时间 redis key
     */
    public static final Long CACHE_SHOP_TTL = 30L;

    /**
     * 商铺数据 redis key
     */
    public static final String CACHE_SHOP_KEY = "cache:shop:";

    /**
     * 店铺类型 redis key
     */
    public static final String CACHE_SHOP_TYPE_KEY = "cache:shop:type:";

    /**
     * 店铺类型失效时间 redis key
     */
    public static final Long CACHE_SHOP_TYPE_TTL = 30L;

    /**
     * 互斥锁 redis key
     */
    public static final String LOCK_SHOP_KEY = "lock:shop:";

    /**
     * 互斥锁 过期时间 redis key
     */
    public static final Long LOCK_SHOP_TTL = 10L;

    /**
     * 秒杀券 redis key
     */
    public static final String SECKILL_STOCK_KEY = "seckill:stock:";

    /**
     * 秒杀券过期时间 redis key
     */
    public static final Long SECKILL_STOCK_TTL = 30L;

    public static final String BLOG_LIKED_KEY = "blog:liked:";
    public static final String FEED_KEY = "feed:";

    /**
     * geo 搜索路径 redis key
     */
    public static final String SHOP_GEO_KEY = "shop:geo:";

    /**
     * 用户签到 redis key
     */
    public static final String USER_SIGN_KEY = "sign:";
}
