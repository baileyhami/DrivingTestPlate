package com.jiakao.ydt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AiChatResponse {
    private String reply;
    private String model;
}

