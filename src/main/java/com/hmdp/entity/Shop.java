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
@ApiModel(value = "店铺对象请求数据", description = "店铺对象请求数据")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_shop")
public class Shop implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商铺名称
     */
    @ApiModelProperty("商铺名称")
    private String name;

    /**
     * 商铺类型的id
     */
    @ApiModelProperty("商铺类型的id")
    private Long typeId;

    /**
     * 商铺图片，多个图片以','隔开
     */
    @ApiModelProperty("商铺图片，多个图片以','隔开")
    private String images;

    /**
     * 商圈，例如陆家嘴
     */
    @ApiModelProperty("商圈，例如陆家嘴")
    private String area;

    /**
     * 地址
     */
    @ApiModelProperty("地址")
    private String address;

    /**
     * 经度
     */
    @ApiModelProperty("经度")
    private Double x;

    /**
     * 维度
     */
    @ApiModelProperty("维度")
    private Double y;

    /**
     * 均价，取整数
     */
    @ApiModelProperty("均价，取整数")
    private Long avgPrice;

    /**
     * 销量
     */
    @ApiModelProperty("销量")
    private Integer sold;

    /**
     * 评论数量
     */
    @ApiModelProperty("评论数量")
    private Integer comments;

    /**
     * 评分，1~5分，乘10保存，避免小数
     */
    @ApiModelProperty("评分，1~5分，乘10保存，避免小数")
    private Integer score;

    /**
     * 营业时间，例如 10:00-22:00
     */
    @ApiModelProperty("营业时间，例如 10:00-22:00")
    private String openHours;

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


    @TableField(exist = false)
    private Double distance;
}
