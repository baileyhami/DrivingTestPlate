package com.jiakao.ydt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterCoachRequest {
    @NotBlank private String username;
    @NotBlank private String password;
    @NotNull private Long schoolId;
}
