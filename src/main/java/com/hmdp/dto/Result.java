package com.hmdp.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 接口统一返回数据格式
 * @author Husp
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "接口返回对象", description = "接口返回对象")
public class Result {

    /**
     * 成功标志
     */
    @ApiModelProperty(value = "成功标志", example = "true")
    private Boolean success;

    /**
     * 返回消息提示
     */
    @ApiModelProperty(value = "返回消息提示", example = "")
    private String errorMsg;

    /**
     * 返回数据对象 data
     */
    @ApiModelProperty(value = "返回数据对象")
    private Object data;

    /**
     * 返回统计数量
     */
    @ApiModelProperty(value = "返回统计数量")
    private Long total;

    public static Result ok(){
        return new Result(true, null, null, null);
    }
    public static Result ok(Object data){
        return new Result(true, null, data, null);
    }
    public static Result ok(List<?> data, Long total){
        return new Result(true, null, data, total);
    }
    public static Result fail(String errorMsg){
        return new Result(false, errorMsg, null, null);
    }
}
