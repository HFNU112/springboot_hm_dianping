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
@ApiModel(value = "优惠券对象请求参数", description = "优惠券对象请求参数")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_voucher")
public class Voucher implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商铺id
     */
    @ApiModelProperty(value = "关联的店铺id", required = true)
    private Long shopId;

    /**
     * 代金券标题
     */
    @ApiModelProperty(value = "代金券标题", required = true)
    private String title;

    /**
     * 副标题
     */
    @ApiModelProperty(value = "副标题", required = true)
    private String subTitle;

    /**
     * 使用规则
     */
    @ApiModelProperty(value = "使用规则", required = true)
    private String rules;

    /**
     * 支付金额
     */
    @ApiModelProperty(value = "支付金额", required = true)
    private Long payValue;

    /**
     * 抵扣金额
     */
    @ApiModelProperty(value = "实际要抵扣的金额", required = true)
    private Long actualValue;

    /**
     * 优惠券类型
     */
    @ApiModelProperty(value = "优惠券类型", required = true, example = "1")
    private Integer type;

    /**
     * 优惠券状态
     */
    @ApiModelProperty(value = "优惠券状态", example = "1")
    private Integer status;
    /**
     * 库存
     */
    @ApiModelProperty(value = "库存", required = true)
    @TableField(exist = false)
    private Integer stock;

    /**
     * 生效时间
     */
    @ApiModelProperty(value = "生效时间", required = true)
    @TableField(exist = false)
    private LocalDateTime beginTime;

    /**
     * 失效时间
     */
    @ApiModelProperty(value = "失效时间", required = true)
    @TableField(exist = false)
    private LocalDateTime endTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


}
