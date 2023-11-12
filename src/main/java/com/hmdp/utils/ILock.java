package com.hmdp.utils;

/**
 * @Author Husp
 * @Date 2023/11/12 21:14
 */
public interface ILock {

    /**
     * 获取锁
     */
    boolean tryLock (Long timeoutSec);

    /**
     * 释放锁
     */
    void delLock();
}
