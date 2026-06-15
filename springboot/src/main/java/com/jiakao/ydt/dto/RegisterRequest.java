package com.jiakao.ydt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
    @NotBlank(message = "手机号不能为空")
    private String phone;
    private String nickname;
    @NotNull(message = "请选择驾校")
    private Long schoolId;
}
