package com.jiakao.ydt.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiMessage {
    @NotBlank(message = "角色不能为空")
    private String role;
    @NotBlank(message = "内容不能为空")
    private String content;
}

