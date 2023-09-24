package com.hmdp.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author: Husp
 * @date: 2023/9/24 22:17
 */
@Data
public class RedisData {

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 商铺信息数据
     */
    private Object data;

}
