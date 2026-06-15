package com.jiakao.ydt.controller;

import com.jiakao.ydt.common.R;
import com.jiakao.ydt.dto.ReviewPublicVO;
import com.jiakao.ydt.service.ReviewQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 评价公开查询（首页展示）
 */
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewQueryService reviewQueryService;

    @GetMapping("/public/recent")
    public R<List<ReviewPublicVO>> recent(@RequestParam(defaultValue = "20") int limit) {
        return R.ok(reviewQueryService.recentPublic(limit));
    }
}
