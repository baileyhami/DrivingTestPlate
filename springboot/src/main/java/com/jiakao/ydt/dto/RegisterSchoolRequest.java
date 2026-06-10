package com.jiakao.ydt.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterSchoolRequest {
    @NotBlank private String city;
    /** 与营业执照相同的驾校全称 */
    @NotBlank private String name;
    @NotBlank private String shortName;
    @NotBlank private String username;
    @NotBlank private String password;
}
