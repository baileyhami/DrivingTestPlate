package com.jiakao.ydt.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiakao.ydt.common.exception.BusinessException;
import com.jiakao.ydt.config.OpenAiProperties;
import com.jiakao.ydt.dto.AiChatRequest;
import com.jiakao.ydt.dto.AiChatResponse;
import com.jiakao.ydt.dto.AiMessage;
import com.jiakao.ydt.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenAiChatService {

    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public AiChatResponse chat(SecurityUser user, AiChatRequest req) {
        String apiKey = properties.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessException("AI 服务未配置，请联系管理员");
        }
        String message = req.getMessage();
        if (message == null || message.isBlank()) {
            throw new BusinessException("消息不能为空");
        }
        List<Map<String, String>> messages = new ArrayList<>();
        if (properties.getSystemPrompt() != null && !properties.getSystemPrompt().isBlank()) {
            messages.add(Map.of("role", "system", "content", properties.getSystemPrompt()));
        }
        List<AiMessage> history = req.getHistory();
        if (history != null && !history.isEmpty()) {
            int start = Math.max(history.size() - 6, 0);
            for (int i = start; i < history.size(); i++) {
                AiMessage item = history.get(i);
                if (item == null || item.getContent() == null || item.getContent().isBlank()) {
                    continue;
                }
                String role = item.getRole() == null ? "" : item.getRole().toLowerCase(Locale.ROOT);
                if (!"user".equals(role) && !"assistant".equals(role)) {
                    continue;
                }
                messages.add(Map.of("role", role, "content", item.getContent()));
            }
        }
        messages.add(Map.of("role", "user", "content", message));

        Map<String, Object> body = new HashMap<>();
        body.put("model", properties.getChatModel());
        body.put("messages", messages);
        body.put("temperature", properties.getTemperature());
        body.put("max_tokens", 600);
        body.put("user", String.valueOf(user.getId()));

        String payload;
        try {
            payload = objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new BusinessException("AI 请求序列化失败");
        }

        String baseUrl = properties.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://api.openai.com/v1";
        }
        baseUrl = baseUrl.trim();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/chat/completions"))
                .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new BusinessException("AI 服务调用失败，请稍后重试");
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new BusinessException("AI 服务返回异常");
        }

        try {
            JsonNode root = objectMapper.readTree(response.body());
            String reply = root.path("choices").path(0).path("message").path("content").asText("").trim();
            if (reply.isBlank()) {
                throw new BusinessException("AI 返回内容为空");
            }
            return new AiChatResponse(reply, properties.getChatModel());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("AI 响应解析失败");
        }
    }
}


