package com.hmdp.service.impl;

import com.hmdp.dto.LoginFormDTO;
import com.hmdp.service.AbstractLoginBuilder;

/**
 * @Author Husp
 * @Date 2023/12/17 17:29
 */
public class LoginFormBuilder implements AbstractLoginBuilder<LoginFormDTO> {

    private LoginFormDTO loginFormDTO;

    public LoginFormBuilder(LoginFormDTO loginFormDTO) {
        this.loginFormDTO = loginFormDTO;
    }

    @Override
    public void setPhone(String phone) {
        loginFormDTO.setPhone(phone);
    }

    @Override
    public void setCode(String code) {
        loginFormDTO.setCode(code);
    }

    @Override
    public LoginFormDTO build() {
        return loginFormDTO;
    }
}
