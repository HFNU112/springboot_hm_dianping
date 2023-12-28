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
@AllArgsConstructor
@NoArgsConstructor
@Data
@ApiModel(value="接口返回对象", description="接口返回对象")
public class Result {

    @ApiModelProperty(value = "状态码")
    private Integer code;

    @ApiModelProperty(value = "成功标志")
    private Boolean success;

    @ApiModelProperty(value = "返回消息提示")
    private String message;

    @ApiModelProperty(value = "返回数据对象")
    private Object data;

    @ApiModelProperty(value = "返回数量")
    private Long total;

//
//    public Result(Integer code, String message) {
//        this.code = code;
//        this.message = message;
//    }
//
//    public Result success(String message) {
//        this.message = message;
//        this.code = SC_OK_200;
//        this.success = true;
//        return this;
//    }
//
//    public static Result ok() {
//        Result r = new Result();
//        r.setSuccess(true);
//        r.setCode(SC_OK_200);
//        return r;
//    }
//
//    public static Result ok(String msg) {
//        Result r = new Result();
//        r.setSuccess(true);
//        r.setCode(SC_OK_200);
//        //Result OK(String msg)方法会造成兼容性问题 issues/I4IP3D
//        r.setData((Object) msg);
//        r.setMessage(msg);
//        return r;
//    }
//
//    public static Result ok(Object data) {
//        Result r = new Result();
//        r.setSuccess(true);
//        r.setCode(SC_OK_200);
//        r.setData(data);
//        return r;
//    }
//
//    public static Result OK() {
//        Result r = new Result();
//        r.setSuccess(true);
//        r.setCode(SC_OK_200);
//        return r;
//    }
//
//    public static Result OK(String msg) {
//        Result r = new Result();
//        r.setSuccess(true);
//        r.setCode(SC_OK_200);
//        r.setMessage(msg);
//        //Result OK(String msg)方法会造成兼容性问题 issues/I4IP3D
//        r.setData((Object) msg);
//        return r;
//    }
//
//    public static Result OK(Object data) {
//        Result r = new Result();
//        r.setSuccess(true);
//        r.setCode(SC_OK_200);
//        r.setData(data);
//        return r;
//    }
//
//    public static Result OK(String msg, Object data) {
//        Result r = new Result();
//        r.setSuccess(true);
//        r.setCode(SC_OK_200);
//        r.setMessage(msg);
//        r.setData(data);
//        return r;
//    }
//
//    public static Result fail(String errorMsg) {
//        return fail(errorMsg);
//    }

    public static Result ok(){
        return new Result(200,true, null, null, null);
    }
    public static Result ok(Integer code, String message){
        return new Result(code, true, message, null, null);
    }
    public static Result ok(Object data){
        return new Result(200,true, null, data, null);
    }
    public static Result ok(List<?> data, Long total){
        return new Result(200,true, null, data, total);
    }
    public static Result fail(String message){
        return new Result(500,false, message, null, null);
    }
}
