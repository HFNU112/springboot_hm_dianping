package com.hmdp.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

import static com.hmdp.utils.SystemConstants.SC_OK_200;

/**
 * 接口统一返回数据格式
 * @author Husp
 */
@Data
@ApiModel(value="接口返回对象", description="接口返回对象")
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 返回状态码
     */
    @ApiModelProperty(value = "返回状态码")
    private Integer code = 0;

    /** 成功标志 */
    @ApiModelProperty(value = "成功标志")
    private Boolean success;

    /** 错误提示*/
    @ApiModelProperty(value = "返回错误提示消息")
    private String errorMsg;

    /** 返回处理消息 */
    @ApiModelProperty(value = "返回处理消息")
    private String message;

    /** 返回数据对象 */
    @ApiModelProperty(value = "返回数据对象")
    private T data;

    /** 返回数量 */
    @ApiModelProperty(value = "返回数量")
    private Long total;

    public Result() {
    }

    public Result(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Result<T> success(String message) {
        this.message = message;
        this.code = SC_OK_200;
        this.success = true;
        return this;
    }

    public static<T> Result<T> ok() {
        Result<T> r = new Result<T>();
        r.setSuccess(true);
        r.setCode(SC_OK_200);
        return r;
    }

    public static<T> Result<T> ok(String msg) {
        Result<T> r = new Result<T>();
        r.setSuccess(true);
        r.setCode(SC_OK_200);
        //Result OK(String msg)方法会造成兼容性问题 issues/I4IP3D
        r.setData((T) msg);
        r.setMessage(msg);
        return r;
    }

    public static<T> Result<T> ok(T data) {
        Result<T> r = new Result<T>();
        r.setSuccess(true);
        r.setCode(SC_OK_200);
        r.setData(data);
        return r;
    }

    public static<T> Result<T> OK() {
        Result<T> r = new Result<T>();
        r.setSuccess(true);
        r.setCode(SC_OK_200);
        return r;
    }

    public static<T> Result<T> OK(String msg) {
        Result<T> r = new Result<T>();
        r.setSuccess(true);
        r.setCode(SC_OK_200);
        r.setMessage(msg);
        //Result OK(String msg)方法会造成兼容性问题 issues/I4IP3D
        r.setData((T) msg);
        return r;
    }

    public static<T> Result<T> OK(T data) {
        Result<T> r = new Result<T>();
        r.setSuccess(true);
        r.setCode(SC_OK_200);
        r.setData(data);
        return r;
    }

    public static<T> Result<T> OK(String msg, T data) {
        Result<T> r = new Result<T>();
        r.setSuccess(true);
        r.setCode(SC_OK_200);
        r.setMessage(msg);
        r.setData(data);
        return r;
    }

    public static<T> Result<T> fail(String errorMsg) {
        return fail(errorMsg);
    }

//    public static Result ok(){
//        return new Result(true, null, null, null);
//    }
//    public static Result ok(Object data){
//        return new Result(true, null, data, null);
//    }
//    public static Result ok(List<?> data, Long total){
//        return new Result(true, null, data, total);
//    }
//    public static Result fail(String errorMsg){
//        return new Result(false, errorMsg, null, null);
//    }
}
