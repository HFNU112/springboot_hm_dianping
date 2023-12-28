package com.hmdp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@ApiModel(description = "优惠券对象请求数据")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_voucher")
public class Voucher implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商铺id
     */
    @ApiModelProperty("商铺id")
    private Long shopId;

    /**
     * 代金券标题
     */
    @ApiModelProperty("代金券标题")
    private String title;

    /**
     * 副标题
     */
    @ApiModelProperty("副标题")
    private String subTitle;

    /**
     * 使用规则
     */
    @ApiModelProperty("使用规则")
    private String rules;

    /**
     * 支付金额
     */
    @ApiModelProperty("支付金额")
    private Long payValue;

    /**
     * 抵扣金额
     */
    @ApiModelProperty("抵扣金额")
    private Long actualValue;

    /**
     * 优惠券类型
     */
    @ApiModelProperty("优惠券类型")
    private Integer type;

    /**
     * 优惠券状态
     */
    @ApiModelProperty("优惠券状态")
    private Integer status;
    /**
     * 库存
     */
    @TableField(exist = false)
    private Integer stock;

    /**
     * 生效时间
     */
    @ApiModelProperty("生效时间")
    @TableField(exist = false)
    private LocalDateTime beginTime;

    /**
     * 失效时间
     */
    @ApiModelProperty("失效时间")
    @TableField(exist = false)
    private LocalDateTime endTime;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;


    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;


}
