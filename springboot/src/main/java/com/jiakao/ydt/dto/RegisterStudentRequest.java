package com.jiakao.ydt.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterStudentRequest {
    @NotBlank private String username;
    @NotBlank private String password;
}
