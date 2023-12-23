package com.hmdp.service;

/**
 * @Author Husp
 * @Date 2023/12/17 17:19
 */
public interface AbstractLoginBuilder<T> {

    void setPhone(String phone);

    void setCode(String code);

    /**
     * 建造者创建登录接口
     */
    T build();
}
