package com.hmdp.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "用户登录参数")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginFormDTO {

    /**
     * 手机号
     */
    @ApiModelProperty(value = "手机号")
    private String phone;

    /**
     * 验证码
     */
    @ApiModelProperty(value = "验证码")
    private String code;

    /**
     * 密码
     */
    @ApiModelProperty(value = "密码")
    private String password;
}
