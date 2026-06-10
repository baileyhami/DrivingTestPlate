package com.jiakao.ydt.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class AiChatRequest {
    @NotBlank(message = "消息不能为空")
    private String message;
    @Valid
    private List<AiMessage> history;
}

