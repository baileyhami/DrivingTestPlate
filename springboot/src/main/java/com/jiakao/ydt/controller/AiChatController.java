package com.jiakao.ydt.controller;

import com.jiakao.ydt.common.R;
import com.jiakao.ydt.dto.AiChatRequest;
import com.jiakao.ydt.dto.AiChatResponse;
import com.jiakao.ydt.security.SecurityUser;
import com.jiakao.ydt.security.SecurityUtils;
import com.jiakao.ydt.service.OpenAiChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 对话（登录用户可用）
 */
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final OpenAiChatService openAiChatService;

    @PostMapping("/chat")
    public R<AiChatResponse> chat(@Valid @RequestBody AiChatRequest req) {
        SecurityUser user = SecurityUtils.requireUser();
        return R.ok(openAiChatService.chat(user, req));
    }
}

