package com.jiakao.ydt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "openai")
public class OpenAiProperties {
    private String apiKey;
    private String baseUrl = "https://api.openai.com/v1";
    private String chatModel = "gpt-4o-mini";
    private int timeoutSeconds = 25;
    private double temperature = 0.4;
    private String systemPrompt = "你是驾考一点通的智能助手，请用简洁、实用的中文回答用户问题。";
}

