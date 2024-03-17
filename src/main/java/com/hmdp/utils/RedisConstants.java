package com.hmdp.utils;

public class RedisConstants {
    /**
     * 发送验证码 redis key
     */
    public static final String LOGIN_CODE_KEY = "login:code:";

    /**
     * 验证码失效时间 redis key
     */
    public static final Long LOGIN_CODE_TTL = 5L;

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

    /**
     * 笔记缓存 redis key
     */
    public static final String CACHE_BLOG_KEY = "cache:blog:";

    /**
     * 笔记缓存过期时间 redis key
     */
    public static final Long CACHE_BLOG_TTL = 30L;

    /**
     * 笔记点赞 redis key
     */
    public static final String BLOG_LIKED_KEY = "blog:liked:";

    /**
     * 笔记点赞过期时间 redis key
     */
    public static final Long BLOG_LIKED_TTL = 30L;

    /**
     * 关注 缓存 redis key
     */
    public static final String CACHE_FOLLOW_KEY = "cache:follow:";

    /**
     * 推送笔记缓存 redis key
     */
    public static final String CACHE_FEED_KEY = "cache:feed:";

    /**
     * geo 路径查询 redis key
     */
    public static final String SHOP_GEO_KEY = "shop:geo:";

    /**
     * geo 路径查询过期时间 redis key
     */
    public static final Long SHOP_GEO_TTL = 30L;

    /**
     * 用户签到 redis key
     */
    public static final String USER_SIGN_KEY = "sign:";

    public static final Long USER_SIGN_TTL = 30L;

    /**
     * 异步下单锁对象 redis key
     */
    public static final String LOCK_ORDER_KEY = "lock:order:";
}
